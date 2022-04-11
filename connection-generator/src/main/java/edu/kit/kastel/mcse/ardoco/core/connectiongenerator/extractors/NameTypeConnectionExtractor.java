/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.extractors;

import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractExtractor;
import edu.kit.kastel.mcse.ardoco.core.api.agent.ConnectionAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.common.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

/**
 * This analyzer searches for name type patterns. If these patterns occur recommendations are created.
 *
 * @author Sophie Schulz, Jan Keim
 */
public class NameTypeConnectionExtractor extends AbstractExtractor<ConnectionAgentData> {

    @Configurable
    private double probability = 1.0;

    public NameTypeConnectionExtractor() {
        // empty
    }

    @Override
    public void exec(ConnectionAgentData data, IWord word) {
        for (var model : data.getModelIds()) {
            var modelState = data.getModelState(model);
            var recommendationState = data.getRecommendationState(modelState.getMetamodel());
            checkForNameAfterType(data.getTextState(), word, modelState, recommendationState);
            checkForNameBeforeType(data.getTextState(), word, modelState, recommendationState);
            checkForNortBeforeType(data.getTextState(), word, modelState, recommendationState);
            checkForNortAfterType(data.getTextState(), word, modelState, recommendationState);
        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the names of the text extraction state
     * contain the previous node. If that's the case a recommendation for the combination of both is created.
     */
    private void checkForNameBeforeType(ITextState textExtractionState, IWord word, IModelState modelState, IRecommendationState recommendationState) {
        if (textExtractionState == null || word == null) {
            return;
        }

        var pre = word.getPreWord();

        var similarTypes = CommonUtilities.getSimilarTypes(word, modelState);

        if (!similarTypes.isEmpty()) {
            textExtractionState.addType(word, this, probability);

            var nameMappings = textExtractionState.getMappingsThatCouldBeAName(pre);
            var typeMappings = textExtractionState.getMappingsThatCouldBeAType(word);

            var instance = tryToIdentify(textExtractionState, similarTypes, pre, modelState);
            addRecommendedInstanceIfNodeNotNull(word, textExtractionState, instance, nameMappings, typeMappings, recommendationState);
        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the names of the text extraction state
     * contain the following node. If that's the case a recommendation for the combination of both is created.
     *
     * @param textExtractionState text extraction state
     * @param word                the current word
     * @param modelState          the current model state
     * @param recommendationState the current recommendation state
     */
    private void checkForNameAfterType(ITextState textExtractionState, IWord word, IModelState modelState, IRecommendationState recommendationState) {
        if (textExtractionState == null || word == null) {
            return;
        }

        var after = word.getNextWord();

        var sameLemmaTypes = CommonUtilities.getSimilarTypes(word, modelState);
        if (!sameLemmaTypes.isEmpty()) {
            textExtractionState.addType(word, this, probability);

            var typeMappings = textExtractionState.getMappingsThatCouldBeAType(word);
            var nameMappings = textExtractionState.getMappingsThatCouldBeAName(after);

            var instance = tryToIdentify(textExtractionState, sameLemmaTypes, after, modelState);
            addRecommendedInstanceIfNodeNotNull(word, textExtractionState, instance, nameMappings, typeMappings, recommendationState);
        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the name_or_types of the text extraction
     * state contain the previous node. If that's the case a recommendation for the combination of both is created.
     */
    private void checkForNortBeforeType(ITextState textExtractionState, IWord word, IModelState modelState, IRecommendationState recommendationState) {
        if (textExtractionState == null || word == null) {
            return;
        }

        var pre = word.getPreWord();

        var sameLemmaTypes = CommonUtilities.getSimilarTypes(word, modelState);

        if (!sameLemmaTypes.isEmpty()) {
            textExtractionState.addType(word, this, probability);

            var typeMappings = textExtractionState.getMappingsThatCouldBeAType(word);
            var nortMappings = textExtractionState.getMappingsThatCouldBeNameOrType(pre);

            var instance = tryToIdentify(textExtractionState, sameLemmaTypes, pre, modelState);
            addRecommendedInstanceIfNodeNotNull(word, textExtractionState, instance, nortMappings, typeMappings, recommendationState);
        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the name_or_types of the text extraction
     * state contain the afterwards node. If that's the case a recommendation for the combination of both is created.
     */
    private void checkForNortAfterType(ITextState textExtractionState, IWord word, IModelState modelState, IRecommendationState recommendationState) {
        if (textExtractionState == null || word == null) {
            return;
        }

        var after = word.getNextWord();

        var sameLemmaTypes = CommonUtilities.getSimilarTypes(word, modelState);
        if (!sameLemmaTypes.isEmpty()) {
            textExtractionState.addType(word, this, probability);

            var typeMappings = textExtractionState.getMappingsThatCouldBeAType(word);
            var nortMappings = textExtractionState.getMappingsThatCouldBeNameOrType(after);

            var instance = tryToIdentify(textExtractionState, sameLemmaTypes, after, modelState);
            addRecommendedInstanceIfNodeNotNull(word, textExtractionState, instance, nortMappings, typeMappings, recommendationState);
        }
    }

    /**
     * Adds a RecommendedInstance to the recommendation state if the mapping of the current node exists. Otherwise a
     * recommendation is added for each existing mapping.
     *
     * @param currentWord         the current node
     * @param textExtractionState the text extraction state
     * @param instance            the instance
     * @param nameMappings        the name mappings
     * @param typeMappings        the type mappings
     */
    private void addRecommendedInstanceIfNodeNotNull(//
            IWord currentWord, ITextState textExtractionState, IModelInstance instance, ImmutableList<INounMapping> nameMappings,
            ImmutableList<INounMapping> typeMappings, IRecommendationState recommendationState) {
        var nounMappingsByCurrentWord = textExtractionState.getNounMappingsByWord(currentWord);
        if (instance != null && nounMappingsByCurrentWord != null) {
            for (INounMapping nmapping : nounMappingsByCurrentWord) {
                var name = instance.getFullName();
                var type = nmapping.getReference();
                recommendationState.addRecommendedInstance(name, type, this, probability, nameMappings, typeMappings);
            }
        }
    }

    /**
     * Tries to identify instances by the given similar types and the name of a given node. If an unambiguous instance
     * can be found it is returned and the name is added to the text extraction state.
     *
     * @param textExtractionState the next extraction state to work with
     * @param similarTypes        the given similar types
     * @param word                the node for name identification
     * @return the unique matching instance
     */
    private IModelInstance tryToIdentify(ITextState textExtractionState, ImmutableList<String> similarTypes, IWord word, IModelState modelState) {
        if (textExtractionState == null || similarTypes == null || word == null) {
            return null;
        }
        MutableList<IModelInstance> matchingInstances = Lists.mutable.empty();

        for (String type : similarTypes) {
            matchingInstances.addAll(modelState.getInstancesOfType(type).castToCollection());
        }

        var text = word.getText();
        matchingInstances = matchingInstances.select(i -> SimilarityUtils.areWordsOfListsSimilar(i.getNameParts(), Lists.immutable.with(text)));

        if (!matchingInstances.isEmpty()) {
            return matchingInstances.get(0);
        }
        return null;
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // handle additional configuration
    }
}
