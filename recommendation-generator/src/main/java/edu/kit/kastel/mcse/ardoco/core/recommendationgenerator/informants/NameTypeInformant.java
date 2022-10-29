/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.informants;

import java.util.Map;

import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendationStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;

/**
 * This analyzer searches for name type patterns. If these patterns occur recommendations are created.
 *
 */

public class NameTypeInformant extends Informant {

    @Configurable
    private double probability = 1.0;
    @Configurable
    private double nortProbability = 1.0;

    /**
     * Creates a new NameTypeAnalyzer
     */
    public NameTypeInformant(DataRepository dataRepository) {
        super(NameTypeInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void run() {
        DataRepository dataRepository = getDataRepository();
        var text = DataRepositoryHelper.getAnnotatedText(dataRepository);
        var textState = DataRepositoryHelper.getTextState(dataRepository);
        var modelStatesData = DataRepositoryHelper.getModelStatesData(dataRepository);
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);

        for (var word : text.words()) {
            exec(textState, modelStatesData, recommendationStates, word);
        }
    }

    private void exec(TextState textState, ModelStates modelStates, RecommendationStates recommendationStates, Word word) {
        for (var model : modelStates.modelIds()) {
            var modelState = modelStates.getModelState(model);
            var recommendationState = recommendationStates.getRecommendationState(modelState.getMetamodel());

            addRecommendedInstanceIfNortAfterType(textState, word, modelState, recommendationState);
            addRecommendedInstanceIfNortBeforeType(textState, word, modelState, recommendationState);
            addRecommendedInstanceIfNameAfterType(textState, word, modelState, recommendationState);
            addRecommendedInstanceIfNameBeforeType(textState, word, modelState, recommendationState);

        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the names of the text extraction state
     * contain the previous node. If that's the case a recommendation for the combination of both is created.
     *
     * @param textExtractionState text extraction state
     * @param word                the current word
     */
    private void addRecommendedInstanceIfNameBeforeType(TextState textExtractionState, Word word, ModelExtractionState modelState,
            RecommendationState recommendationState) {

        var sameLemmaTypes = CommonUtilities.getSimilarTypes(word, modelState);

        if (!sameLemmaTypes.isEmpty()) {

            var typeMappings = Lists.mutable.withAll(textExtractionState.getNounMappingsByWord(word))
                    .select(nm -> nm.getKind().equals(MappingKind.TYPE))
                    .toImmutable();
            var nameMappings = Lists.mutable.withAll(textExtractionState.getNounMappingsByWord(word.getPreWord()))
                    .select(nm -> nm.getKind().equals(MappingKind.NAME))
                    .toImmutable();

            if (typeMappings.isEmpty() || nameMappings.isEmpty()) {
                return;
            }

            CommonUtilities.addRecommendedInstancesFromNounMappings(sameLemmaTypes, nameMappings, typeMappings, recommendationState, this, probability);
        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the names of the text extraction state
     * contain the following node. If that's the case a recommendation for the combination of both is created.
     *
     * @param textExtractionState text extraction state
     * @param word                the current word
     */
    private void addRecommendedInstanceIfNameAfterType(TextState textExtractionState, Word word, ModelExtractionState modelState,
            RecommendationState recommendationState) {

        var sameLemmaTypes = CommonUtilities.getSimilarTypes(word, modelState);
        if (!sameLemmaTypes.isEmpty()) {

            var typeMappings = Lists.mutable.withAll(textExtractionState.getNounMappingsByWord(word))
                    .select(nm -> nm.getKind().equals(MappingKind.TYPE))
                    .toImmutable();
            var nameMappings = Lists.mutable.withAll(textExtractionState.getNounMappingsByWord(word.getNextWord()))
                    .select(nm -> nm.getKind().equals(MappingKind.NAME))
                    .toImmutable();

            if (typeMappings.isEmpty() || nameMappings.isEmpty()) {
                return;
            }

            CommonUtilities.addRecommendedInstancesFromNounMappings(sameLemmaTypes, nameMappings, typeMappings, recommendationState, this, probability);
        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the names of the text extraction state
     * contain the previous node. If that's the case a recommendation for the combination of both is created.
     *
     * @param textExtractionState text extraction state
     * @param word                the current word
     */
    private void addRecommendedInstanceIfNortBeforeType(TextState textExtractionState, Word word, ModelExtractionState modelState,
            RecommendationState recommendationState) {

        var sameLemmaTypes = CommonUtilities.getSimilarTypes(word, modelState);

        if (!sameLemmaTypes.isEmpty()) {

            var nameMappings = textExtractionState.getMappingsThatCouldBeOfKind(word.getPreWord(), MappingKind.NAME);
            var typeMappings = Lists.mutable.withAll(textExtractionState.getNounMappingsByWord(word))
                    .select(nm -> nm.getKind().equals(MappingKind.TYPE))
                    .toImmutable();
            if (typeMappings.isEmpty()) {
                return;
            }

            CommonUtilities.addRecommendedInstancesFromNounMappings(sameLemmaTypes, nameMappings, typeMappings, recommendationState, this, nortProbability);
        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the names of the text extraction state
     * contain the following node. If that's the case a recommendation for the combination of both is created.
     *
     * @param textExtractionState text extraction state
     * @param word                the current word
     */
    private void addRecommendedInstanceIfNortAfterType(TextState textExtractionState, Word word, ModelExtractionState modelState,
            RecommendationState recommendationState) {

        var sameLemmaTypes = CommonUtilities.getSimilarTypes(word, modelState);
        if (!sameLemmaTypes.isEmpty()) {

            var typeMappings = Lists.mutable.withAll(textExtractionState.getNounMappingsByWord(word))
                    .select(nm -> nm.getKind().equals(MappingKind.TYPE))
                    .toImmutable();
            if (typeMappings.isEmpty()) {
                return;
            }
            var nameMappings = textExtractionState.getMappingsThatCouldBeOfKind(word.getNextWord(), MappingKind.NAME);

            CommonUtilities.addRecommendedInstancesFromNounMappings(sameLemmaTypes, nameMappings, typeMappings, recommendationState, this, nortProbability);
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // handle additional config
    }

}
