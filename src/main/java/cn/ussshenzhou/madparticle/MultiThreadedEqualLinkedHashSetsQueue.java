package cn.ussshenzhou.madparticle;

import cn.ussshenzhou.t88.config.ConfigHelper;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.errorprone.annotations.DoNotCall;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author USS_Shenzhou
 * @see com.google.common.collect.EvictingQueue
 */
public class MultiThreadedEqualLinkedHashSetsQueue<E> implements Queue<E> {
    private final LinkedHashSet<E>[] linkedHashSets;
    private final int maxSize;
    private final Executor fixedThreadPool;

    public MultiThreadedEqualLinkedHashSetsQueue(int initialCapacityOfEachThread, int maxSize) {
        this.maxSize = maxSize;
        int threads = ConfigHelper.getConfigRead(MadParticleConfig.class).getBufferFillerThreads();
        //noinspection unchecked
        linkedHashSets = Stream.generate(() -> Sets.newLinkedHashSetWithExpectedSize(initialCapacityOfEachThread))
                .limit(threads)
                .toArray(LinkedHashSet[]::new);
        fixedThreadPool = Executors.newFixedThreadPool(threads, new ThreadFactoryBuilder().setNameFormat("MadParticle-MultiThreadedEqualLinkedHashSetsQueue-Thread-%d").build());
    }

    public MultiThreadedEqualLinkedHashSetsQueue(int maxSize) {
        this(128, maxSize);
    }

    public int remainingCapacity() {
        return maxSize - this.size();
    }

    @Override
    public int size() {
        int size = 0;
        for (var hashset : linkedHashSets) {
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
        for (var hashset : linkedHashSets) {
            if (hashset.contains(o)) {
                return true;
            }
        }
        return false;
    }

    @DoNotCall
    @Override
    public @NotNull Iterator<E> iterator() {
        return Iterators.concat(Arrays.stream(linkedHashSets)
                .map(Set::iterator)
                .iterator());
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public @NotNull Object[] toArray() {
        return Arrays.stream(linkedHashSets).flatMap(Set::stream).toArray();
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
        for (var hashset : linkedHashSets) {
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
            this.poll();
        }
        LinkedHashSet<E> target = linkedHashSets[0];
        for (var hashset : linkedHashSets) {
            if (hashset.size() < target.size()) {
                target = hashset;
            }
        }
        return target.add(e);
    }

    @Override
    public boolean remove(Object o) {
        for (var hashset : linkedHashSets) {
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
        boolean changed = false;
        for (var o : c) {
            changed |= linkedHashSets[index % linkedHashSets.length].add(o);
        }
        return changed;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        CompletableFuture<?>[] futures = new CompletableFuture[linkedHashSets.length];
        for (int i = 0; i < linkedHashSets.length; i++) {
            int finalI = i;
            futures[i] = CompletableFuture.runAsync(
                    () -> linkedHashSets[finalI].removeAll(c),
                    fixedThreadPool
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
        for (var hashset : linkedHashSets) {
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
        LinkedHashSet<E> target = linkedHashSets[0];
        for (var hashset : linkedHashSets) {
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
        LinkedHashSet<E> target = linkedHashSets[0];
        for (var hashset : linkedHashSets) {
            if (hashset.size() > target.size()) {
                target = hashset;
            }
        }
        return target.getFirst();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        CompletableFuture<?>[] futures = new CompletableFuture[linkedHashSets.length];
        for (int i = 0; i < linkedHashSets.length; i++) {
            int finalI = i;
            futures[i] = CompletableFuture.runAsync(
                    () -> linkedHashSets[finalI].forEach(action),
                    fixedThreadPool
            );
        }
        CompletableFuture.allOf(futures).join();
    }
}
