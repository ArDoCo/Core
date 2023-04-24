package edu.kit.kastel.mcse.ardoco.core.models.amtl;

import java.util.Set;

public class ArchitectureInterface extends ArchitectureItem {

    private Set<ArchitectureMethod> signatures;

    public ArchitectureInterface(String name, String id, Set<ArchitectureMethod> signatures) {
        super(name, id);
        this.signatures = signatures;
    }

    public Set<ArchitectureMethod> getSignatures() {
        return signatures;
    }

    @Override
    public String toString() {
        return "Interface: " + getName();
    }
}