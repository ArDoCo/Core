package edu.kit.kastel.mcse.ardoco.core.api.models.code;

import java.util.Set;

public class CodeAssembly extends CodeModule {

    public CodeAssembly(String name, Set<? extends CodeItem> content) {
        super(name, content);
    }
}
