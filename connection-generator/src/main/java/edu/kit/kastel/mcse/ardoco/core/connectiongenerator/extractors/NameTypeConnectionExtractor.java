/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.extractors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionExtractor;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.GenericConnectionConfig;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;

/**
 * This analyzer searches for name type patterns. If these patterns occur recommendations are created.
 *
 * @author Sophie Schulz, Jan Keim
 *
 */
@MetaInfServices(ConnectionExtractor.class)
public class NameTypeConnectionExtractor extends ConnectionExtractor {

    private double probability;

    /**
     * Creates a new NameTypeAnalyzer.
     *
     * @param textExtractionState  the text extraction state
     * @param modelExtractionState the model extraction state
     * @param connectionState      the recommendation state
     */
    public NameTypeConnectionExtractor(ITextState textExtractionState, IModelState modelExtractionState, IRecommendationState recommendationState,
            IConnectionState connectionState) {
        this(textExtractionState, modelExtractionState, recommendationState, connectionState, GenericConnectionConfig.DEFAULT_CONFIG);
    }

    /**
     * Instantiates a new name type extractor.
     *
     * @param textExtractionState  the text extraction state
     * @param modelExtractionState the model extraction state
     * @param recommendationState  the recommendation state
     * @param config               the config
     */
    public NameTypeConnectionExtractor(ITextState textExtractionState, IModelState modelExtractionState, IRecommendationState recommendationState,
            IConnectionState connectionState, GenericConnectionConfig config) {
        super(textExtractionState, modelExtractionState, recommendationState, connectionState);
        probability = config.nameTypeAnalyzerProbability;
    }

    /**
     * Prototype constructor.
     */
    public NameTypeConnectionExtractor() {
        this(null, null, null, null);
    }

    @Override
    public ConnectionExtractor create(ITextState textState, IModelState modelExtractionState, IRecommendationState recommendationState,
            IConnectionState connectionState, Configuration config) {
        return new NameTypeConnectionExtractor(textState, modelExtractionState, recommendationState, connectionState, (GenericConnectionConfig) config);
    }

    @Override
    public void exec(IWord word) {
        checkForNameAfterType(textState, word);
        checkForNameBeforeType(textState, word);
        checkForNortBeforeType(textState, word);
        checkForNortAfterType(textState, word);
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the names of the text extraction state
     * contain the previous node. If that's the case a recommendation for the combination of both is created.
     *
     * @param textExtractionState text extraction state
     * @param word                the current word
     */
    private void checkForNameBeforeType(ITextState textExtractionState, IWord word) {
        if (textExtractionState == null || word == null) {
            return;
        }

        IWord pre = word.getPreWord();

        ImmutableList<String> similarTypes = CommonUtilities.getSimilarTypes(word, modelState);

        if (!similarTypes.isEmpty()) {
            textExtractionState.addType(word, probability);

            ImmutableList<INounMapping> nameMappings = textExtractionState.getMappingsThatCouldBeAName(pre);
            ImmutableList<INounMapping> typeMappings = textExtractionState.getMappingsThatCouldBeAType(word);

            IModelInstance instance = tryToIdentify(textExtractionState, similarTypes, pre);
            addRecommendedInstanceIfNodeNotNull(word, textExtractionState, instance, nameMappings, typeMappings);
        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the names of the text extraction state
     * contain the following node. If that's the case a recommendation for the combination of both is created.
     *
     * @param textExtractionState text extraction state
     * @param word                the current word
     */
    private void checkForNameAfterType(ITextState textExtractionState, IWord word) {
        if (textExtractionState == null || word == null) {
            return;
        }

        IWord after = word.getNextWord();

        ImmutableList<String> sameLemmaTypes = CommonUtilities.getSimilarTypes(word, modelState);
        if (!sameLemmaTypes.isEmpty()) {
            textExtractionState.addType(word, probability);

            ImmutableList<INounMapping> typeMappings = textExtractionState.getMappingsThatCouldBeAType(word);
            ImmutableList<INounMapping> nameMappings = textExtractionState.getMappingsThatCouldBeAName(after);

            IModelInstance instance = tryToIdentify(textExtractionState, sameLemmaTypes, after);
            addRecommendedInstanceIfNodeNotNull(word, textExtractionState, instance, nameMappings, typeMappings);
        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the name_or_types of the text extraction
     * state contain the previous node. If that's the case a recommendation for the combination of both is created.
     *
     * @param textExtractionState text extraction state
     * @param word                the current word
     */
    private void checkForNortBeforeType(ITextState textExtractionState, IWord word) {
        if (textExtractionState == null || word == null) {
            return;
        }

        IWord pre = word.getPreWord();

        ImmutableList<String> sameLemmaTypes = CommonUtilities.getSimilarTypes(word, modelState);

        if (!sameLemmaTypes.isEmpty()) {
            textExtractionState.addType(word, probability);

            ImmutableList<INounMapping> typeMappings = textExtractionState.getMappingsThatCouldBeAType(word);
            ImmutableList<INounMapping> nortMappings = textExtractionState.getMappingsThatCouldBeANort(pre);

            IModelInstance instance = tryToIdentify(textExtractionState, sameLemmaTypes, pre);
            addRecommendedInstanceIfNodeNotNull(word, textExtractionState, instance, nortMappings, typeMappings);
        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the name_or_types of the text extraction
     * state contain the afterwards node. If that's the case a recommendation for the combination of both is created.
     *
     * @param textExtractionState text extraction state
     * @param word                the current word
     */
    private void checkForNortAfterType(ITextState textExtractionState, IWord word) {
        if (textExtractionState == null || word == null) {
            return;
        }

        IWord after = word.getNextWord();

        ImmutableList<String> sameLemmaTypes = CommonUtilities.getSimilarTypes(word, modelState);
        if (!sameLemmaTypes.isEmpty()) {
            textExtractionState.addType(word, probability);

            ImmutableList<INounMapping> typeMappings = textExtractionState.getMappingsThatCouldBeAType(word);
            ImmutableList<INounMapping> nortMappings = textExtractionState.getMappingsThatCouldBeANort(after);

            IModelInstance instance = tryToIdentify(textExtractionState, sameLemmaTypes, after);
            addRecommendedInstanceIfNodeNotNull(word, textExtractionState, instance, nortMappings, typeMappings);
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
    private boolean addRecommendedInstanceIfNodeNotNull(//
            IWord currentWord, ITextState textExtractionState, IModelInstance instance, ImmutableList<INounMapping> nameMappings,
            ImmutableList<INounMapping> typeMappings) {
        if (textExtractionState.getNounMappingsByWord(currentWord) != null && instance != null) {
            ImmutableList<INounMapping> nmappings = textExtractionState.getNounMappingsByWord(currentWord);
            for (INounMapping nmapping : nmappings) {
                String name = instance.getFullName();
                String type = nmapping.getReference();
                recommendationState.addRecommendedInstance(name, type, probability, nameMappings, typeMappings);
            }
            return true;
        }
        return false;
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
    private IModelInstance tryToIdentify(ITextState textExtractionState, ImmutableList<String> similarTypes, IWord word) {
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

}
