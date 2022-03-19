/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.extractors;

import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractExtractor;
import edu.kit.kastel.mcse.ardoco.core.api.agent.RecommendationAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.common.Configurable;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;

/**
 * This analyzer searches for name type patterns. If these patterns occur recommendations are created.
 *
 * @author Sophie Schulz, Jan Keim
 */
public class NameTypeExtractor extends AbstractExtractor<RecommendationAgentData> {

    @Configurable
    private double probability = 1.0;

    /**
     * Creates a new NameTypeAnalyzer
     */
    public NameTypeExtractor() {
    }

    @Override
    public void exec(RecommendationAgentData data, IWord word) {
        var textState = data.getTextState();
        for (var model : data.getModelIds()) {
            var modelState = data.getModelState(model);
            var recommendationState = data.getRecommendationState(modelState.getMetamodel());
            addRecommendedInstanceIfNameAfterType(textState, word, modelState, recommendationState);
            addRecommendedInstanceIfNameBeforeType(textState, word, modelState, recommendationState);
            addRecommendedInstanceIfNortBeforeType(textState, word, modelState, recommendationState);
            addRecommendedInstanceIfNortAfterType(textState, word, modelState, recommendationState);
        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the names of the text extraction state
     * contain the previous node. If that's the case a recommendation for the combination of both is created.
     *
     * @param textExtractionState text extraction state
     * @param word                the current word
     */
    private void addRecommendedInstanceIfNameBeforeType(ITextState textExtractionState, IWord word, IModelState modelState,
            IRecommendationState recommendationState) {
        if (textExtractionState == null || word == null) {
            return;
        }

        ImmutableList<String> similarTypes = CommonUtilities.getSimilarTypes(word, modelState);

        if (!similarTypes.isEmpty()) {
            textExtractionState.addType(word, probability);

            ImmutableList<INounMapping> nameMappings = textExtractionState.getMappingsThatCouldBeAName(word.getPreWord());
            ImmutableList<INounMapping> typeMappings = textExtractionState.getMappingsThatCouldBeAType(word);

            CommonUtilities.addRecommendedInstancesFromNounMappings(similarTypes, nameMappings, typeMappings, recommendationState, probability);
        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the names of the text extraction state
     * contain the following node. If that's the case a recommendation for the combination of both is created.
     *
     * @param textExtractionState text extraction state
     * @param word                the current word
     */
    private void addRecommendedInstanceIfNameAfterType(ITextState textExtractionState, IWord word, IModelState modelState,
            IRecommendationState recommendationState) {
        if (textExtractionState == null || word == null) {
            return;
        }

        ImmutableList<String> sameLemmaTypes = CommonUtilities.getSimilarTypes(word, modelState);
        if (!sameLemmaTypes.isEmpty()) {
            textExtractionState.addType(word, probability);

            ImmutableList<INounMapping> typeMappings = textExtractionState.getMappingsThatCouldBeAType(word);
            ImmutableList<INounMapping> nameMappings = textExtractionState.getMappingsThatCouldBeAName(word.getNextWord());

            CommonUtilities.addRecommendedInstancesFromNounMappings(sameLemmaTypes, nameMappings, typeMappings, recommendationState, probability);
        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the name_or_types of the text extraction
     * state contain the previous node. If that's the case a recommendation for the combination of both is created.
     *
     * @param textExtractionState text extraction state
     * @param word                the current word
     */
    private void addRecommendedInstanceIfNortBeforeType(ITextState textExtractionState, IWord word, IModelState modelState,
            IRecommendationState recommendationState) {
        if (textExtractionState == null || word == null) {
            return;
        }

        ImmutableList<String> sameLemmaTypes = CommonUtilities.getSimilarTypes(word, modelState);

        if (!sameLemmaTypes.isEmpty()) {
            textExtractionState.addType(word, probability);

            ImmutableList<INounMapping> typeMappings = textExtractionState.getMappingsThatCouldBeAType(word);
            ImmutableList<INounMapping> nortMappings = textExtractionState.getMappingsThatCouldBeANort(word.getPreWord());

            CommonUtilities.addRecommendedInstancesFromNounMappings(sameLemmaTypes, nortMappings, typeMappings, recommendationState, probability);
        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the name_or_types of the text extraction
     * state contain the afterwards node. If that's the case a recommendation for the combination of both is created.
     *
     * @param textExtractionState text extraction state
     * @param word                the current word
     */
    private void addRecommendedInstanceIfNortAfterType(ITextState textExtractionState, IWord word, IModelState modelState,
            IRecommendationState recommendationState) {
        if (textExtractionState == null || word == null) {
            return;
        }

        ImmutableList<String> sameLemmaTypes = CommonUtilities.getSimilarTypes(word, modelState);
        if (!sameLemmaTypes.isEmpty()) {
            textExtractionState.addType(word, probability);

            ImmutableList<INounMapping> typeMappings = textExtractionState.getMappingsThatCouldBeAType(word);
            ImmutableList<INounMapping> nortMappings = textExtractionState.getMappingsThatCouldBeANort(word.getNextWord());

            CommonUtilities.addRecommendedInstancesFromNounMappings(sameLemmaTypes, nortMappings, typeMappings, recommendationState, probability);
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
    }
}
