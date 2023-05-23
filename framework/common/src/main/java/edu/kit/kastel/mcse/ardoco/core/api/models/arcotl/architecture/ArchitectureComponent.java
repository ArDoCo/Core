/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture;

import java.util.Set;

/**
 * A representation of the model object <i>Component</i> from AMTL. Components
 * are building blocks of a software architecture. A component can contain
 * subcomponents but doesn't have to. A component can provide and require
 * interfaces. Provided interfaces are implemented by the component. Required
 * interfaces specify some functionality that is needed by the component.
 */
public class ArchitectureComponent extends ArchitectureItem {

    private final Set<ArchitectureComponent> subcomponents;
    private final Set<ArchitectureInterface> providedInterfaces;

    private final Set<ArchitectureInterface> requiredInterfaces;

    public ArchitectureComponent(String name, String id, Set<ArchitectureComponent> subcomponents, Set<ArchitectureInterface> providedInterfaces,
            Set<ArchitectureInterface> requiredInterfaces) {
        super(name, id);
        this.subcomponents = subcomponents;
        this.providedInterfaces = providedInterfaces;
        this.requiredInterfaces = requiredInterfaces;
    }

    /**
     * Returns the subcomponents of this component.
     *
     * @return the subcomponents of this component
     */
    public Set<ArchitectureComponent> getSubcomponents() {
        return subcomponents;
    }

    /**
     * Returns the provided interfaces of this component. Provided interfaces are
     * implemented by this component.
     *
     * @return the provided interfaces of this component
     */
    public Set<ArchitectureInterface> getProvidedInterfaces() {
        return providedInterfaces;
    }

    /**
     * Returns the required interfaces of this component. Required interfaces
     * specify some functionality that is needed by this component.
     *
     * @return the required interfaces of this component
     */
    public Set<ArchitectureInterface> getRequiredInterfaces() {
        return requiredInterfaces;
    }

    @Override
    public String toString() {
        return "Component: " + getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ArchitectureComponent that))
            return false;
        if (!super.equals(o))
            return false;

        if (!subcomponents.equals(that.subcomponents))
            return false;
        if (!providedInterfaces.equals(that.providedInterfaces))
            return false;
        return requiredInterfaces.equals(that.requiredInterfaces);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + subcomponents.hashCode();
        result = 31 * result + providedInterfaces.hashCode();
        result = 31 * result + requiredInterfaces.hashCode();
        return result;
    }
}
