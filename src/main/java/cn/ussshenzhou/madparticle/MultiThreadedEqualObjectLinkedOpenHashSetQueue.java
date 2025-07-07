package cn.ussshenzhou.madparticle;

import cn.ussshenzhou.t88.config.ConfigHelper;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.google.errorprone.annotations.DoNotCall;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author USS_Shenzhou
 * @see com.google.common.collect.EvictingQueue
 * @see it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
 */
public class MultiThreadedEqualObjectLinkedOpenHashSetQueue<E> implements Queue<E> {
    private static final AtomicInteger ID = new AtomicInteger();
    private static final AtomicInteger THREAD_ID = new AtomicInteger();
    private final ObjectLinkedOpenHashSet<E>[] sets;
    private final int maxSize;
    private final ForkJoinPool threadPool;

    public MultiThreadedEqualObjectLinkedOpenHashSetQueue(int initialCapacityOfEachThread, int maxSize) {
        this.maxSize = maxSize;
        ID.getAndIncrement();
        int threads = ConfigHelper.getConfigRead(MadParticleConfig.class).getBufferFillerThreads();
        //noinspection unchecked,rawtypes
        sets = Stream.generate(()->new ObjectLinkedOpenHashSet())
                .limit(threads)
                .toArray(ObjectLinkedOpenHashSet[]::new);
        threadPool = new ForkJoinPool(threads, pool -> {
            ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            thread.setName("MadParticle-MultiThreadedEqualObjectLinkedOpenHashSetQueue-" + ID.get() + "-Thread-" + THREAD_ID.getAndIncrement());
            return thread;
        }, null, false);
    }

    public MultiThreadedEqualObjectLinkedOpenHashSetQueue(int maxSize) {
        this(128, maxSize);
    }

    public int remainingCapacity() {
        return maxSize - this.size();
    }

    public ObjectLinkedOpenHashSet<E> get(int i) {
        return sets[i];
    }

    @Override
    public int size() {
        int size = 0;
        for (var hashset : sets) {
            size += hashset.size();
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (o == null) {
            return false;
        }
        for (var hashset : sets) {
            if (hashset.contains(o)) {
                return true;
            }
        }
        return false;
    }

    @DoNotCall
    @Override
    public @NotNull Iterator<E> iterator() {
        return Iterators.concat(Arrays.stream(sets)
                .map(Set::iterator)
                .iterator());
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public @NotNull Object[] toArray() {
        return Arrays.stream(sets).flatMap(Set::stream).toArray();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public @NotNull <T> T[] toArray(@NotNull T[] a) {
        if (a.length < this.size()) {
            @SuppressWarnings("unchecked")
            T[] newArray = (T[]) Array.newInstance(a.getClass().getComponentType(), this.size());
            a = newArray;
        }
        int index = 0;
        for (var hashset : sets) {
            for (E e : hashset) {
                //noinspection unchecked
                a[index] = (T) e;
                index++;
            }
        }
        return a;
    }

    @Override
    public boolean add(E e) {
        if (this.size() >= maxSize) {
            return false;
        }
        var target = sets[0];
        for (var hashset : sets) {
            if (hashset.size() < target.size()) {
                target = hashset;
            }
        }
        return target.add(e);
    }

    @Override
    public boolean remove(Object o) {
        for (var hashset : sets) {
            if (hashset.remove(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for (var o : c) {
            if (!this.contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        int index = 0;
        int max = maxSize - this.size();
        boolean changed = false;
        for (var o : c) {
            if (index >= max) {
                return changed;
            }
            changed |= sets[index % sets.length].add(o);
            index++;
        }
        return changed;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        CompletableFuture<?>[] futures = new CompletableFuture[sets.length];
        for (int i = 0; i < sets.length; i++) {
            int finalI = i;
            futures[i] = CompletableFuture.runAsync(
                    () -> sets[finalI].removeAll(c),
                    threadPool
            );
        }
        CompletableFuture.allOf(futures).join();
        return true;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        for (var hashset : sets) {
            hashset.clear();
        }
    }

    @Override
    public boolean offer(E e) {
        return add(e);
    }

    @Override
    public E remove() {
        var r = poll();
        if (r == null) {
            throw new NoSuchElementException();
        }
        return r;
    }

    @Override
    public E poll() {
        var target = sets[0];
        for (var hashset : sets) {
            if (hashset.size() > target.size()) {
                target = hashset;
            }
        }
        return target.removeFirst();
    }

    @Override
    public E element() {
        var r = peek();
        if (r == null) {
            throw new NoSuchElementException();
        }
        return r;
    }

    @Override
    public E peek() {
        var target = sets[0];
        for (var hashset : sets) {
            if (hashset.size() > target.size()) {
                target = hashset;
            }
        }
        return target.getFirst();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        CompletableFuture<?>[] futures = new CompletableFuture[sets.length];
        for (int i = 0; i < sets.length; i++) {
            int finalI = i;
            futures[i] = CompletableFuture.runAsync(
                    () -> sets[finalI].parallelStream().forEach(action),
                    threadPool
            );
        }
        CompletableFuture.allOf(futures).join();
    }
}
