/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

public final class ElementWrapper<E> {

    private final Class<E> elementType;

    private final E element;

    private final Function<E, Integer> elementToHash;

    private final BiPredicate<E, E> elementEquals;

    public ElementWrapper(Class<E> elementType, E element, Function<E, Integer> elementToHash, BiPredicate<E, E> elementEquals) {

        this.elementType = Objects.requireNonNull(elementType);
        this.element = element;
        this.elementEquals = Objects.requireNonNull(elementEquals);
        this.elementToHash = Objects.requireNonNull(elementToHash);

    }

    @Override
    public int hashCode() {
        return elementToHash.apply(element);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ElementWrapper<?> that = (ElementWrapper<?>) o;
        return elementType.equals(that.elementType) && elementEquals.test(element, elementType.cast(that.element));
    }

    public E getElement() {
        return element;
    }
}
