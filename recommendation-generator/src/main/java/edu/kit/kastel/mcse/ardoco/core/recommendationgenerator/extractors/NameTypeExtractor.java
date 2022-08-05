/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.extractors;

import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendationStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;

/**
 * This analyzer searches for name type patterns. If these patterns occur recommendations are created.
 *
 */
public class NameTypeExtractor extends Informant {

    @Configurable
    private double probability = 1.0;

    /**
     * Creates a new NameTypeAnalyzer
     */
    public NameTypeExtractor(DataRepository dataRepository) {
        super("NameTypeExtractor", dataRepository);
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

            addRecommendedInstanceIfNameAfterType(textState, word, modelState, recommendationState);
            addRecommendedInstanceIfNameBeforeType(textState, word, modelState, recommendationState);
            addRecommendedInstanceIfNameOrTypeBeforeType(textState, word, modelState, recommendationState);
            addRecommendedInstanceIfNameOrTypeAfterType(textState, word, modelState, recommendationState);
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
        if (textExtractionState == null || word == null) {
            return;
        }

        var similarTypes = CommonUtilities.getSimilarTypes(word, modelState);

        if (!similarTypes.isEmpty()) {
            textExtractionState.addNounMapping(word, MappingKind.TYPE, this, probability);

            NounMapping preMapping = textExtractionState.getNounMappingByWord(word.getPreWord());
            NounMapping wordMapping = textExtractionState.getNounMappingByWord(word);

            MutableList<NounMapping> nameMappings = Lists.mutable.empty();
            MutableList<NounMapping> typeMappings = Lists.mutable.empty();

            if (wordMapping.couldBeOfKind(MappingKind.TYPE)) {
                typeMappings.add(wordMapping);
            }

            if (preMapping != null && preMapping.couldBeOfKind(MappingKind.NAME)) {
                nameMappings.add(preMapping);
            }
            CommonUtilities.addRecommendedInstancesFromNounMappings(similarTypes, nameMappings.toImmutable(), typeMappings.toImmutable(), recommendationState,
                    this, probability);
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
        if (textExtractionState == null || word == null) {
            return;
        }

        var sameLemmaTypes = CommonUtilities.getSimilarTypes(word, modelState);
        if (!sameLemmaTypes.isEmpty()) {
            textExtractionState.addNounMapping(word, MappingKind.TYPE, this, probability);

            NounMapping afterMapping = textExtractionState.getNounMappingByWord(word.getNextWord());
            NounMapping wordMapping = textExtractionState.getNounMappingByWord(word);

            MutableList<NounMapping> nameMappings = Lists.mutable.empty();
            MutableList<NounMapping> typeMappings = Lists.mutable.empty();

            if (wordMapping.couldBeOfKind(MappingKind.TYPE)) {
                typeMappings.add(wordMapping);
            }

            if (afterMapping != null && afterMapping.couldBeOfKind(MappingKind.NAME)) {
                typeMappings.add(afterMapping);
            }

            CommonUtilities.addRecommendedInstancesFromNounMappings(sameLemmaTypes, nameMappings.toImmutable(), typeMappings.toImmutable(), recommendationState,
                    this, probability);
        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the name_or_types of the text extraction
     * state contain the previous node. If that's the case a recommendation for the combination of both is created.
     *
     * @param textExtractionState text extraction state
     * @param word                the current word
     */
    private void addRecommendedInstanceIfNameOrTypeBeforeType(TextState textExtractionState, Word word, ModelExtractionState modelState,
            RecommendationState recommendationState) {
        if (textExtractionState == null || word == null) {
            return;
        }

        var sameLemmaTypes = CommonUtilities.getSimilarTypes(word, modelState);

        if (!sameLemmaTypes.isEmpty()) {
            textExtractionState.addNounMapping(word, MappingKind.TYPE, this, probability);

            NounMapping wordMapping = textExtractionState.getNounMappingByWord(word);
            NounMapping preMapping = textExtractionState.getNounMappingByWord(word.getPreWord());

            MutableList<NounMapping> nortMappings = Lists.mutable.empty();
            MutableList<NounMapping> typeMappings = Lists.mutable.empty();

            if (wordMapping.couldBeOfKind(MappingKind.TYPE)) {
                typeMappings.add(wordMapping);
            }

            if (preMapping != null && preMapping.couldBeMultipleKinds(MappingKind.NAME, MappingKind.TYPE)) {
                nortMappings.add(preMapping);
            }

            CommonUtilities.addRecommendedInstancesFromNounMappings(sameLemmaTypes, nortMappings.toImmutable(), typeMappings.toImmutable(), recommendationState,
                    this, probability);
        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the name_or_types of the text extraction
     * state contain the afterwards node. If that's the case a recommendation for the combination of both is created.
     *
     * @param textExtractionState text extraction state
     * @param word                the current word
     */
    private void addRecommendedInstanceIfNameOrTypeAfterType(TextState textExtractionState, Word word, ModelExtractionState modelState,
            RecommendationState recommendationState) {
        if (textExtractionState == null || word == null) {
            return;
        }

        var sameLemmaTypes = CommonUtilities.getSimilarTypes(word, modelState);
        if (!sameLemmaTypes.isEmpty()) {
            textExtractionState.addNounMapping(word, MappingKind.TYPE, this, probability);

            NounMapping wordMapping = textExtractionState.getNounMappingByWord(word);
            NounMapping afterMapping = textExtractionState.getNounMappingByWord(word.getNextWord());

            MutableList<NounMapping> nortMappings = Lists.mutable.empty();
            MutableList<NounMapping> typeMappings = Lists.mutable.empty();

            if (wordMapping.couldBeOfKind(MappingKind.TYPE)) {
                typeMappings.add(wordMapping);
            }

            if (afterMapping != null && afterMapping.couldBeMultipleKinds(MappingKind.NAME, MappingKind.TYPE)) {
                nortMappings.add(afterMapping);
            }

            CommonUtilities.addRecommendedInstancesFromNounMappings(sameLemmaTypes, nortMappings.toImmutable(), typeMappings.toImmutable(), recommendationState,
                    this, probability);
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // handle additional config
    }

}
