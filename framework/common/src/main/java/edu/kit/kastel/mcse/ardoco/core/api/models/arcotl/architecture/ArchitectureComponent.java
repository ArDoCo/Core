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

    private Set<ArchitectureComponent> subcomponents;
    private Set<ArchitectureInterface> providedInterfaces;
    private Set<ArchitectureInterface> requiredInterfaces;

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
}
