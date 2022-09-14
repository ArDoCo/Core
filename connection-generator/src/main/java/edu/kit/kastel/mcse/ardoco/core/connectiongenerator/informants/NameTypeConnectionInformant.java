/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.informants;

import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendationStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

/**
 * This analyzer searches for name type patterns. If these patterns occur recommendations are created.
 *
 */
public class NameTypeConnectionInformant extends Informant {

    @Configurable
    private double probability = 1.0;

    public NameTypeConnectionInformant(DataRepository dataRepository) {
        super(NameTypeConnectionInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void run() {
        DataRepository dataRepository = getDataRepository();
        var text = DataRepositoryHelper.getAnnotatedText(dataRepository);
        var textState = DataRepositoryHelper.getTextState(dataRepository);
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);
        for (var word : text.words()) {
            exec(textState, modelStates, recommendationStates, word);
        }
    }

    private void exec(TextState textState, ModelStates modelStates, RecommendationStates recommendationStates, Word word) {
        for (var model : modelStates.modelIds()) {
            var modelState = modelStates.getModelState(model);
            var recommendationState = recommendationStates.getRecommendationState(modelState.getMetamodel());
            checkForNameAfterType(textState, word, modelState, recommendationState);
            checkForNameBeforeType(textState, word, modelState, recommendationState);
            checkForNortBeforeType(textState, word, modelState, recommendationState);
            checkForNortAfterType(textState, word, modelState, recommendationState);
        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the names of the text extraction state
     * contain the previous node. If that's the case a recommendation for the combination of both is created.
     */
    private void checkForNameBeforeType(TextState textExtractionState, Word word, ModelExtractionState modelState, RecommendationState recommendationState) {
        if (textExtractionState == null || word == null) {
            return;
        }

        var preWord = word.getPreWord();

        var similarTypes = CommonUtilities.getSimilarTypes(word, modelState);

        if (!similarTypes.isEmpty()) {
            textExtractionState.addNounMapping(word, MappingKind.TYPE, this, probability);

            var nameMappings = textExtractionState.getMappingsThatCouldBeOfKind(preWord, MappingKind.NAME);
            var typeMappings = textExtractionState.getMappingsThatCouldBeOfKind(word, MappingKind.TYPE);

            var instance = tryToIdentify(textExtractionState, similarTypes, preWord, modelState);
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
    private void checkForNameAfterType(TextState textExtractionState, Word word, ModelExtractionState modelState, RecommendationState recommendationState) {
        if (textExtractionState == null || word == null) {
            return;
        }

        var after = word.getNextWord();

        var sameLemmaTypes = CommonUtilities.getSimilarTypes(word, modelState);
        if (!sameLemmaTypes.isEmpty()) {
            textExtractionState.addNounMapping(word, MappingKind.TYPE, this, probability);

            var typeMappings = textExtractionState.getMappingsThatCouldBeOfKind(word, MappingKind.TYPE);
            var nameMappings = textExtractionState.getMappingsThatCouldBeOfKind(after, MappingKind.NAME);

            var instance = tryToIdentify(textExtractionState, sameLemmaTypes, after, modelState);
            addRecommendedInstanceIfNodeNotNull(word, textExtractionState, instance, nameMappings, typeMappings, recommendationState);
        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the name_or_types of the text extraction
     * state contain the previous node. If that's the case a recommendation for the combination of both is created.
     */
    private void checkForNortBeforeType(TextState textExtractionState, Word word, ModelExtractionState modelState, RecommendationState recommendationState) {
        if (textExtractionState == null || word == null) {
            return;
        }

        var preWord = word.getPreWord();

        var sameLemmaTypes = CommonUtilities.getSimilarTypes(word, modelState);

        if (!sameLemmaTypes.isEmpty()) {
            textExtractionState.addNounMapping(word, MappingKind.TYPE, this, probability);

            var typeMappings = textExtractionState.getMappingsThatCouldBeOfKind(word, MappingKind.TYPE);
            var nortMappings = textExtractionState.getMappingsThatCouldBeMultipleKinds(preWord, MappingKind.NAME, MappingKind.TYPE);

            var instance = tryToIdentify(textExtractionState, sameLemmaTypes, preWord, modelState);
            addRecommendedInstanceIfNodeNotNull(word, textExtractionState, instance, nortMappings, typeMappings, recommendationState);
        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the name_or_types of the text extraction
     * state contain the afterwards node. If that's the case a recommendation for the combination of both is created.
     */
    private void checkForNortAfterType(TextState textExtractionState, Word word, ModelExtractionState modelState, RecommendationState recommendationState) {
        if (textExtractionState == null || word == null) {
            return;
        }

        var after = word.getNextWord();

        var sameLemmaTypes = CommonUtilities.getSimilarTypes(word, modelState);
        if (!sameLemmaTypes.isEmpty()) {
            textExtractionState.addNounMapping(word, MappingKind.TYPE, this, probability);

            var typeMappings = textExtractionState.getMappingsThatCouldBeOfKind(word, MappingKind.TYPE);
            var nortMappings = textExtractionState.getMappingsThatCouldBeMultipleKinds(after, MappingKind.NAME, MappingKind.TYPE);

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
            Word currentWord, TextState textExtractionState, ModelInstance instance, ImmutableList<NounMapping> nameMappings,
            ImmutableList<NounMapping> typeMappings, RecommendationState recommendationState) {
        var nounMappingsByCurrentWord = textExtractionState.getNounMappingsByWord(currentWord);
        if (instance != null && nounMappingsByCurrentWord != null) {
            for (NounMapping nmapping : nounMappingsByCurrentWord) {
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
    private ModelInstance tryToIdentify(TextState textExtractionState, ImmutableList<String> similarTypes, Word word, ModelExtractionState modelState) {
        if (textExtractionState == null || similarTypes == null || word == null) {
            return null;
        }
        MutableList<ModelInstance> matchingInstances = Lists.mutable.empty();

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
