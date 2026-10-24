package cn.ussshenzhou.madparticle.util;

import cn.ussshenzhou.madparticle.MadParticleConfig;
import cn.ussshenzhou.t88.config.ConfigHelper;
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.errorprone.annotations.DoNotCall;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Cleaner;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author USS_Shenzhou
 * @see com.google.common.collect.EvictingQueue
 * @see it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
 */
@SuppressWarnings("unchecked")
public class MultiThreadedObjectArrayListQueue<E> implements Queue<E> {

    private static final AtomicInteger ID = new AtomicInteger();
    private static final Cleaner CLEANER = Cleaner.create();

    private final ObjectArrayList<E>[] arrays;
    private final int maxSize;
    private final DefaultEventExecutorGroup threadPool;

    public MultiThreadedObjectArrayListQueue(int initialCapacityOfEachThread, int maxSize) {
        this.maxSize = maxSize;
        ID.getAndIncrement();
        int threads = ConfigHelper.getConfigRead(MadParticleConfig.class).getBufferFillerThreads();
        //noinspection unchecked,rawtypes
        arrays = Stream.generate(() -> new ObjectArrayList())
                .limit(threads)
                .toArray(ObjectArrayList[]::new);
        threadPool = new DefaultEventExecutorGroup(threads, new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("MadParticle-MultiThreadedObjectArrayQueue-" + ID.get() + "-Thread-%d")
                .build()
        );

        //noinspection UnnecessaryLocalVariable
        var pool = this.threadPool;
        CLEANER.register(this, pool::shutdownGracefully);
    }

    public MultiThreadedObjectArrayListQueue(int maxSize) {
        this(16384, maxSize);
    }

    public int threads() {
        return arrays.length;
    }

    public int remainingCapacity() {
        return maxSize - this.size();
    }

    public ObjectArrayList<E> get(int i) {
        return arrays[i];
    }

    public DefaultEventExecutorGroup getThreadPool() {
        return threadPool;
    }

    @Override
    public int size() {
        int size = 0;
        for (var array : arrays) {
            size += array.size();
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @DoNotCall
    @Override
    public boolean contains(Object o) {
        if (o == null) {
            return false;
        }
        for (var array : arrays) {
            if (array.contains(o)) {
                return true;
            }
        }
        return false;
    }

    @DoNotCall
    @Override
    public @NotNull Iterator<E> iterator() {
        return Iterators.concat(Arrays.stream(arrays)
                .map(ObjectArrayList::iterator)
                .iterator());
    }

    @SuppressWarnings("NullableProblems")
    @DoNotCall
    @Override
    public @NotNull Object[] toArray() {
        return Arrays.stream(arrays).flatMap(ObjectArrayList::stream).toArray();
    }

    @SuppressWarnings("NullableProblems")
    @DoNotCall
    @Override
    public @NotNull <T> T[] toArray(@NotNull T[] a) {
        if (a.length < this.size()) {
            @SuppressWarnings("unchecked")
            T[] newArray = (T[]) Array.newInstance(a.getClass().getComponentType(), this.size());
            a = newArray;
        }
        int index = 0;
        for (var hashset : arrays) {
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
        var target = arrays[0];
        for (var hashset : arrays) {
            if (hashset.size() < target.size()) {
                target = hashset;
            }
        }
        return target.add(e);
    }

    @DoNotCall
    @Override
    public boolean remove(Object o) {
        for (var hashset : arrays) {
            if (hashset.remove(o)) {
                return true;
            }
        }
        return false;
    }

    @DoNotCall
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
        int maxAdd = maxSize - this.size();
        if (maxAdd == 0 || c.isEmpty()) {
            return false;
        }
        int toAdd = Math.min(maxAdd, c.size());
        int threads = threads();
        int eachAdd = toAdd / threads + 1;
        var cArray = (E[]) c.toArray();
        for (int i = 0; i < toAdd; ) {
            var array = arrays[i / eachAdd];
            array.addElements(array.size(), cArray, i, Math.min(eachAdd, toAdd - i));
            i += eachAdd;
        }
        return true;
    }

    @DoNotCall
    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        if (c.isEmpty()) {
            return false;
        }
        CompletableFuture<?>[] futures = new CompletableFuture[arrays.length];
        for (int i = 0; i < arrays.length; i++) {
            int finalI = i;
            futures[i] = CompletableFuture.runAsync(
                    () -> {
                        var set = arrays[finalI];
                        for (var key : c) {
                            set.remove(key);
                        }
                    },
                    threadPool
            );
        }
        CompletableFuture.allOf(futures).join();
        return true;
    }

    @DoNotCall
    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        for (var array : arrays) {
            array.clear();
        }
    }

    @Override
    public boolean offer(E e) {
        return add(e);
    }

    @Override
    public E remove() {
        var e = poll();
        if (e == null) {
            throw new NoSuchElementException();
        }
        return e;
    }


    @Override
    public E poll() {
        if (size() == 0) {
            return null;
        }
        for (var array : arrays) {
            if (!array.isEmpty()) {
                return array.removeFirst();
            }
        }
        throw new IllegalStateException();
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
        if (size() == 0) {
            return null;
        }
        for (var array : arrays) {
            if (!array.isEmpty()) {
                return array.getFirst();
            }
        }
        throw new IllegalStateException();
    }

    @DoNotCall
    @Override
    public void forEach(Consumer<? super E> action) {
        CompletableFuture<?>[] futures = new CompletableFuture[arrays.length];
        for (int i = 0; i < threads(); i++) {
            int finalI = i;
            futures[i] = CompletableFuture.runAsync(
                    () -> arrays[finalI].forEach(action),
                    threadPool
            );
        }
        CompletableFuture.allOf(futures).join();
    }

    public int forEachAllowRemove(ActionAndPredicate<E> action) {
        CompletableFuture<?>[] futures = new CompletableFuture[arrays.length];
        int arrayBaseIndex = 0;
        for (int i = 0; i < threads(); i++) {
            int finalI = i;
            int finalBaseIndex = arrayBaseIndex;
            int sizeBeforeRemove = arrays[i].size();
            futures[i] = CompletableFuture.supplyAsync(
                    () -> {
                        var list = arrays[finalI];
                        var array = list.elements();
                        int size = list.size();
                        int removed = 0;
                        int lastToRemove = -1;
                        for (int index = 0; index < size; index++) {
                            var e = array[index];
                            if (action.accept(e, finalI, finalBaseIndex + index)) {
                                if (lastToRemove != -1) {
                                    System.arraycopy(array, lastToRemove + 1, array, lastToRemove - removed, index - lastToRemove - 1);
                                    removed++;
                                }
                                lastToRemove = index;
                            }
                        }
                        if (lastToRemove != -1) {
                            System.arraycopy(array, lastToRemove + 1, array, lastToRemove - removed, size - lastToRemove - 1);
                            removed++;
                        }
                        list.size(size - removed);
                        return removed;
                    },
                    threadPool
            );
            arrayBaseIndex += sizeBeforeRemove;
        }
        CompletableFuture.allOf(futures).join();
        return Stream.of(futures).mapToInt(f -> (int) f.join()).sum();
    }

    @FunctionalInterface
    public interface ActionAndPredicate<E> {
        /**
         * @return true if removing
         */
        boolean accept(E e, int threadId, int overallIndex);
    }
}
