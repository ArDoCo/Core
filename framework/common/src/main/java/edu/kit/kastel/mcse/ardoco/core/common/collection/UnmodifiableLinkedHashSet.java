/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.common.collection;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

/**
 * Unmodifiable view of {@link java.util.LinkedHashSet}.
 *
 * @param <E> the type of elements maintained by this set
 */
@Deterministic
public class UnmodifiableLinkedHashSet<E> implements Set<E>, Serializable {
    private Set<? extends Serializable> test;
    private final Set<E> set;

    public UnmodifiableLinkedHashSet(LinkedHashSet<E> set) {
        //This does not affect iteration order
        this.set = Collections.unmodifiableSet(set);
    }

    public UnmodifiableLinkedHashSet(List<E> list) {
        this.set = Collections.unmodifiableSet(new LinkedHashSet<>(list));
    }

    /**
     * Do not use this unless the provided set guarantees iteration order.
     *
     * @param set a set with a guaranteed iteration order
     */
    UnmodifiableLinkedHashSet(Set<E> set) {
        this.set = set;
    }

    public static <E> UnmodifiableLinkedHashSet<E> of(LinkedHashSet<E> set) {
        return new UnmodifiableLinkedHashSet<>(set);
    }

    public static <E> UnmodifiableLinkedHashSet<E> of(List<E> list) {
        return new UnmodifiableLinkedHashSet<>(list);
    }

    @SafeVarargs
    public static <E> UnmodifiableLinkedHashSet<E> of(E... items) {
        //List.of performs safety checks for the varargs, so we can just ignore the parameterized varargs
        return new UnmodifiableLinkedHashSet<>(List.of(items));
    }

    public static <E> UnmodifiableLinkedHashSet<E> of(Stream<E> stream) {
        return of(stream.toList());
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return set.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return set.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return set.toArray(a);
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return set.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
