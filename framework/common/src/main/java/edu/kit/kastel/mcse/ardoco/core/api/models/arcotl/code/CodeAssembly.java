/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("CodeAssembly")
public class CodeAssembly extends CodeModule {

    private CodeAssembly() {
        // Jackson
    }

    public CodeAssembly(CodeItemRepository codeItemRepository, String name, Set<? extends CodeItem> content) {
        super(codeItemRepository, name, content);
    }
}
