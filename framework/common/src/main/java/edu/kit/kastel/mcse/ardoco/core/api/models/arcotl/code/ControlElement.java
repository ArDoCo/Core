/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("ControlElement")
public class ControlElement extends ComputationalObject {

    private ControlElement() {
        // Jackson
    }

    public ControlElement(CodeItemRepository codeItemRepository, String name) {
        super(codeItemRepository, name);
    }
}
