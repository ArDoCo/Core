/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.mod;

public final class ModifiedElement<A, E> {
    private final E element;
    private final Modifications type;
    private A artifact;

    public ModifiedElement(A artifact, E element, Modifications type) {
        this.artifact = artifact;
        this.element = element;
        this.type = type;
    }

    public A getArtifact() {
        return artifact;
    }

    public E getElement() {
        return element;
    }

    public Modifications getType() {
        return type;
    }

    public static <A, E> ModifiedElement<A, E> of(A artifact, E element, Modifications type) {
        return new ModifiedElement<>(artifact, element, type);
    }
}
