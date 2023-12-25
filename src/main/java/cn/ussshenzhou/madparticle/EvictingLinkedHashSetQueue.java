package cn.ussshenzhou.madparticle;

import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * @author USS_Shenzhou
 * @see com.google.common.collect.EvictingQueue
 */
public class EvictingLinkedHashSetQueue<E> extends LinkedHashSet<E> implements Queue<E> {

    final int maxSize;

    public EvictingLinkedHashSetQueue(int initialCapacity, int maxSize) {
        super(initialCapacity);
        this.maxSize = maxSize;
    }

    public EvictingLinkedHashSetQueue(int maxSize) {
        super();
        this.maxSize = maxSize;
    }

    public int remainingCapacity() {
        return maxSize - this.size();
    }

    @Override
    public boolean add(E e) {
        if (this.size() >= maxSize) {
            this.poll();
        }
        return super.add(e);
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
        var i = this.iterator();
        if (i.hasNext()) {
            var r = i.next();
            this.remove(r);
            return r;
        }
        return null;
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
        var i = this.iterator();
        if (i.hasNext()) {
            return i.next();
        }
        return null;
    }
}
