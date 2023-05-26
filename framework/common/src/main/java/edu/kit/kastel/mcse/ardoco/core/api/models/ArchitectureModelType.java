/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models;

/**
 * Enum for the different types of supported architecture (meta-) models
 */
public enum ArchitectureModelType implements ModelType {
    /**
     * Palladio Component Model
     */
    PCM,
    /**
     * Unified Modeling Language (UML)
     */
    UML;

    @Override
    public String getModelId() {
        return this.name();
    }
}
