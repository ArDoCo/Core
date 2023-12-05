/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture;

import java.util.Objects;
import java.util.SortedSet;

public final class ArchitectureInterface extends ArchitectureItem {

    private final SortedSet<ArchitectureMethod> signatures;

    public ArchitectureInterface(String name, String id, SortedSet<ArchitectureMethod> signatures) {
        super(name, id);
        this.signatures = signatures;
    }

    public SortedSet<ArchitectureMethod> getSignatures() {
        return signatures;
    }

    @Override
    public String toString() {
        return "Interface: " + getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ArchitectureInterface that))
            return false;
        if (!super.equals(o))
            return false;
        return Objects.equals(signatures, that.signatures);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), signatures);
    }
}
