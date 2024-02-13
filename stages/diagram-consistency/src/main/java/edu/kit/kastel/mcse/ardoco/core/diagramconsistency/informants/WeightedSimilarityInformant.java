/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.informants;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramMatchingModelSelectionState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.ElementRole;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Extractions;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.WeightedTextSimilarity;
import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

/**
 * Provides a weighted similarity function based on the word count in element names.
 */
public class WeightedSimilarityInformant extends Informant {
    @Configurable
    private double minWeight = 0.2;
    @Configurable
    private boolean skip = false;

    /**
     * Creates a new WeightedSimilarityInformant.
     *
     * @param dataRepository
     *                       The DataRepository.
     */
    public WeightedSimilarityInformant(DataRepository dataRepository) {
        super(WeightedSimilarityInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void process() {
        if (this.skip) {
            return;
        }

        DataRepository data = this.getDataRepository();

        ModelStates models = data.getData(ModelStates.ID, ModelStates.class).orElse(null);
        DiagramMatchingModelSelectionState selection = data.getData(DiagramMatchingModelSelectionState.ID, DiagramMatchingModelSelectionState.class)
                .orElse(null);

        if (models == null || selection == null) {
            this.logger.error("WeightedSimilarityInformant: Could not find all required data.");
            return;
        }

        Map<String, Integer> wordCounts = this.countWordsInModels(models, selection);

        WeightedTextSimilarity similarity = new WeightedTextSimilarity(this.calculateWeights(wordCounts));
        selection.setSimilarityFunction(similarity);
    }

    private Map<String, Integer> countWordsInModels(ModelStates models, DiagramMatchingModelSelectionState selection) {
        Map<String, Integer> wordCounts = new TreeMap<>();

        for (var modelType : selection.getAvailableModelTypes()) {
            Model model = models.getModel(modelType.getModelId());
            if (model instanceof ArchitectureModel architectureModel) {
                this.countWords(Extractions.extractItemsFromModel(architectureModel), wordCounts);
            } else if (model instanceof CodeModel codeModel) {
                this.countWords(Extractions.extractItemsFromModel(codeModel), wordCounts);
            } else {
                throw new IllegalArgumentException("Unexpected model type: " + modelType);
            }
        }

        return wordCounts;
    }

    private <E extends Entity> void countWords(Map<ElementRole, Set<E>> elements, Map<String, Integer> wordCounts) {
        for (var entry : elements.entrySet()) {
            for (Entity element : entry.getValue()) {
                WeightedTextSimilarity.getWords(element.getName()).forEach(word -> wordCounts.merge(word, 1, Integer::sum));
            }
        }
    }

    private Map<String, Double> calculateWeights(Map<String, Integer> wordCounts) {
        double max = wordCounts.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        Map<String, Double> weights = new TreeMap<>();

        double weightRange = 1.0 - this.minWeight;

        for (var entry : wordCounts.entrySet()) {
            double commonness = 1.0 - entry.getValue() / max;
            double weight = this.minWeight + weightRange * commonness;
            weights.put(entry.getKey(), weight);
        }

        return weights;
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        // Intentionally left empty.
    }
}
