/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.informants;

import java.util.Set;
import java.util.SortedMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramMatchingModelSelectionState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.DiagramUtility;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.ElementRole;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Extractions;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.TextSimilarity;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram;
import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.DiagramStateImpl;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

/**
 * Finds occurrences of diagram elements in the models.
 */
public class DiagramElementOccurrenceFinderInformant extends Informant {
    /**
     * The default threshold for the levenshtein similarity used for architecture models.
     */
    /*package-private*/ static final double DEFAULT_SIMILARITY_THRESHOLD_ARCHITECTURE = 0.6;
    /**
     * The default threshold for the levenshtein similarity used for code models.
     */
    /*package-private*/ static final double DEFAULT_SIMILARITY_THRESHOLD_CODE = 0.8;

    @Configurable
    private double similarityThresholdArchitecture = DEFAULT_SIMILARITY_THRESHOLD_ARCHITECTURE;
    @Configurable
    private double similarityThresholdCode = DEFAULT_SIMILARITY_THRESHOLD_CODE;

    @Configurable
    private boolean skip = false;

    @Configurable
    private TextSimilarityFunction similarityFunction = TextSimilarityFunction.ADAPTED_JACCARD;

    private <E extends Entity> void findOccurrences(Input<E> input, ElementRole role, double threshold, DiagramMatchingModelSelectionState selection) {
        BiFunction<String, String, Double> similarity = this.getSimilarityFunction(selection);
        for (var box : input.diagram().getBoxes()) {
            for (var element : input.elements()) {
                if (similarity.apply(DiagramUtility.getBoxText(box), element.getName()) > threshold) {
                    selection.addOccurrence(box.getUUID(), input.modelType(), input.idProvider().apply(element), role);
                }
            }
        }
    }

    /**
     * Creates a new DiagramElementOccurrenceFinderInformant.
     *
     * @param dataRepository
     *                       The DataRepository.
     */
    public DiagramElementOccurrenceFinderInformant(DataRepository dataRepository) {
        super(DiagramElementOccurrenceFinderInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void process() {
        if (this.skip) {
            return;
        }

        DataRepository data = this.getDataRepository();

        ModelStates models = data.getData(ModelStates.ID, ModelStates.class).orElse(null);
        DiagramState diagram = data.getData(DiagramState.ID, DiagramStateImpl.class).orElse(null);
        DiagramMatchingModelSelectionState selection = data.getData(DiagramMatchingModelSelectionState.ID, DiagramMatchingModelSelectionState.class)
                .orElse(null);

        if (models == null || diagram == null || selection == null) {
            this.logger.error("DiagramElementOccurrenceFinderInformant: Could not find all required data.");
            return;
        }

        this.findOccurrencesInModels(diagram, models, selection);
    }

    private void findOccurrencesInModels(DiagramState diagramState, ModelStates models, DiagramMatchingModelSelectionState selection) {
        for (var modelType : selection.getAvailableModelTypes()) {
            Model model = models.getModel(modelType.getModelId());
            if (model instanceof ArchitectureModel architectureModel) {
                for (var entry : Extractions.extractItemsFromModel(architectureModel).entrySet()) {
                    this.findOccurrencesInArchitecture(architectureModel, modelType, diagramState.getDiagram(), entry.getKey(), entry.getValue(), selection);
                }
            } else if (model instanceof CodeModel codeModel) {
                for (var entry : Extractions.extractItemsFromModel(codeModel).entrySet()) {
                    this.findOccurrencesInCode(codeModel, modelType, diagramState.getDiagram(), entry.getKey(), entry.getValue(), selection);
                }
            } else {
                throw new IllegalArgumentException("Unexpected model type: " + modelType);
            }
        }
    }

    private void findOccurrencesInArchitecture(Model model, ModelType modelType, Diagram diagram, ElementRole role, Set<ArchitectureItem> elements,
            DiagramMatchingModelSelectionState selection) {
        this.findOccurrences(new Input<>(model, modelType, diagram, elements, ArchitectureItem::getId), role, this.similarityThresholdArchitecture, selection);
    }

    private void findOccurrencesInCode(Model model, ModelType modelType, Diagram diagram, ElementRole role, Set<CodeItem> elements,
            DiagramMatchingModelSelectionState selection) {
        this.findOccurrences(new Input<>(model, modelType, diagram, elements, Extractions::getPath), role, this.similarityThresholdCode, selection);
    }

    private BiFunction<String, String, Double> getSimilarityFunction(DiagramMatchingModelSelectionState selection) {
        return switch (this.similarityFunction) {
        case LEVENSHTEIN -> TextSimilarity::byLevenshteinCaseInsensitive;
        case JARO_WINKLER -> TextSimilarity::byJaroWinkler;
        case JACCARD -> TextSimilarity::byJaccard;
        case ADAPTED_JACCARD -> (a, b) -> selection.getSimilarityFunction().apply(a, b);
        };
    }

    /**
     * The available text similarity functions.
     */
    public enum TextSimilarityFunction {
        /**
         * Uses the levenshtein distance to calculate the similarity.
         */
        LEVENSHTEIN,
        /**
         * Uses the jaro winkler distance to calculate the similarity.
         */
        JARO_WINKLER,
        /**
         * Uses the jaccard similarity to calculate the similarity.
         */
        JACCARD,
        /**
         * Uses the custom and adapted jaccard similarity to calculate the similarity.
         */
        ADAPTED_JACCARD
    }

    private record Input<E extends Entity>(Model model, ModelType modelType, Diagram diagram, Set<E> elements, Function<E, String> idProvider) {

    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        // Intentionally left empty.
    }
}
