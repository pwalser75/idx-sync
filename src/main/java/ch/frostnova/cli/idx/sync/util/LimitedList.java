package ch.frostnova.cli.idx.sync.util;

import java.util.Collection;
import java.util.LinkedList;

/**
 * A list which holds a limited number of elements (last n added).
 *
 * @param <E> item type
 */
public class LimitedList<E> extends LinkedList<E> {

    private final int limit;

    public LimitedList(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("List limit must not be negative");
        }
        this.limit = limit;
    }

    @Override
    public boolean add(E item) {
        super.addLast(item);
        while (size() > limit) {
            super.removeFirst();
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        if (collection != null) {
            collection.forEach(this::add);
        }
        return true;
    }
}