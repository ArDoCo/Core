/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.informalin.framework.common;

import java.util.Objects;

public final class Pointer<P> {
    private P p;

    public Pointer() {
        this(null);
    }

    public Pointer(P p) {
        this.p = p;
    }

    public P getP() {
        return p;
    }

    public void setP(P p) {
        this.p = p;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Pointer<?> pointer))
            return false;
        return Objects.equals(getP(), pointer.getP());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getP());
    }
}
