/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The role a diagram element can have in displaying a model element.
 */
public enum ElementRole {
    /**
     * The element displays a component in the architecture.
     */
    @JsonProperty("architecture:component") ARCHITECTURE_COMPONENT,
    /**
     * The element displays an interface in the architecture.
     */
    @JsonProperty("architecture:interface") ARCHITECTURE_INTERFACE,
    /**
     * The element displays a class in the code.
     */
    @JsonProperty("code:class") CODE_CLASS,
    /**
     * The element displays an interface in the code.
     */
    @JsonProperty("code:interface") CODE_INTERFACE,
    /**
     * The element displays a package in the code.
     */
    @JsonProperty("code:package") CODE_PACKAGE,
}
