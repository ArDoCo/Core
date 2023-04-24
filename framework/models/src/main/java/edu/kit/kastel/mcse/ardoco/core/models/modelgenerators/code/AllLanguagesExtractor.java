package edu.kit.kastel.mcse.ardoco.core.models.modelgenerators.code;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.models.cmtl.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.models.cmtl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.models.cmtl.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.core.models.modelgenerators.code.java.JavaExtractor;
import edu.kit.kastel.mcse.ardoco.core.models.modelgenerators.code.shell.ShellExtractor;

public final class AllLanguagesExtractor extends CodeExtractor {

    private static final AllLanguagesExtractor extractor = new AllLanguagesExtractor();
    private static final Map<ProgrammingLanguage, CodeExtractor> codeExtractors = Map.of(ProgrammingLanguage.JAVA, JavaExtractor.getExtractor(),
            ProgrammingLanguage.SHELL, ShellExtractor.getExtractor());

    private AllLanguagesExtractor() {
    }

    public static AllLanguagesExtractor getExtractor() {
        return extractor;
    }

    @Override
    public CodeModel extractModel(String path) {
        List<CodeModel> models = new ArrayList<>();
        for (CodeExtractor extractor : codeExtractors.values()) {
            CodeModel model = extractor.extractModel(path);
            models.add(model);
        }
        Set<CodeItem> codeEndpoints = new HashSet<>();
        for (CodeModel model : models) {
            codeEndpoints.addAll(model.getContent());
        }
        CodeModel finalModel = new CodeModel(codeEndpoints);
        return finalModel;
    }
}