/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code.java.JavaExtractor;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code.shell.ShellExtractor;

public final class AllLanguagesExtractor extends CodeExtractor {

    private final Map<ProgrammingLanguage, CodeExtractor> codeExtractors;

    private CodeModel extractedModel = null;

    public AllLanguagesExtractor(CodeItemRepository codeItemRepository, String path) {
        super(codeItemRepository, path);
        codeExtractors = Map.of(ProgrammingLanguage.JAVA, new JavaExtractor(codeItemRepository, path), ProgrammingLanguage.SHELL, new ShellExtractor(
                codeItemRepository, path));
    }

    @Override
    public synchronized CodeModel extractModel() {
        if (extractedModel == null) {
            List<CodeModel> models = new ArrayList<>();
            for (CodeExtractor extractor : codeExtractors.values()) {
                var model = extractor.extractModel();
                models.add(model);
            }
            Set<CodeItem> codeEndpoints = new HashSet<>();
            for (CodeModel model : models) {
                codeEndpoints.addAll(model.getContent());
            }
            this.extractedModel = new CodeModel(codeItemRepository, codeEndpoints);
        }
        return extractedModel;
    }

}
