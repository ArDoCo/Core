/* Licensed under MIT 2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models;

public non-sealed class CodeEntity extends Entity {
    protected CodeEntity(String name) {
        super(name);
    }

    protected CodeEntity(String name, String id) {
        super(name, id);
    }
}
