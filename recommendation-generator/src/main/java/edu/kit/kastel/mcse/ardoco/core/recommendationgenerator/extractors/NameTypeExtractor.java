package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.extractors;

import org.eclipse.collections.api.list.ImmutableList;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.RecommendationExtractor;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.GenericRecommendationConfig;
import edu.kit.kastel.mcse.ardoco.core.util.CommonUtilities;

/**
 * This analyzer searches for name type patterns. If these patterns occur recommendations are created.
 *
 * @author Sophie Schulz, Jan Keim
 *
 */
@MetaInfServices(RecommendationExtractor.class)
public class NameTypeExtractor extends RecommendationExtractor {

    private double probability;

    /**
     * Creates a new NameTypeAnalyzer.
     *
     * @param textExtractionState  the text extraction state
     * @param modelExtractionState the model extraction state
     * @param recommendationState  the recommendation state
     */
    public NameTypeExtractor(ITextState textExtractionState, IModelState modelExtractionState, IRecommendationState recommendationState) {
        this(textExtractionState, modelExtractionState, recommendationState, GenericRecommendationConfig.DEFAULT_CONFIG);
    }

    /**
     * Instantiates a new name type extractor.
     *
     * @param textExtractionState  the text extraction state
     * @param modelExtractionState the model extraction state
     * @param recommendationState  the recommendation state
     * @param config               the config
     */
    public NameTypeExtractor(ITextState textExtractionState, IModelState modelExtractionState, IRecommendationState recommendationState,
            GenericRecommendationConfig config) {
        super(textExtractionState, modelExtractionState, recommendationState);
        probability = config.nameTypeAnalyzerProbability;
    }

    /**
     * Prototype constructor.
     */
    public NameTypeExtractor() {
        this(null, null, null);
    }

    @Override
    public RecommendationExtractor create(ITextState textState, IModelState modelExtractionState, IRecommendationState recommendationState,
            Configuration config) {
        return new NameTypeExtractor(textState, modelExtractionState, recommendationState, (GenericRecommendationConfig) config);
    }

    @Override
    public void exec(IWord word) {
        addRecommendedInstanceIfNameAfterType(textState, word);
        addRecommendedInstanceIfNameBeforeType(textState, word);
        addRecommendedInstanceIfNortBeforeType(textState, word);
        addRecommendedInstanceIfNortAfterType(textState, word);
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the names of the text extraction state
     * contain the previous node. If that's the case a recommendation for the combination of both is created.
     *
     * @param textExtractionState text extraction state
     * @param word                the current word
     */
    private void addRecommendedInstanceIfNameBeforeType(ITextState textExtractionState, IWord word) {
        if (textExtractionState == null || word == null) {
            return;
        }

        ImmutableList<String> similarTypes = CommonUtilities.getSimilarTypes(word, modelState);

        if (!similarTypes.isEmpty()) {
            textExtractionState.addType(word, similarTypes.get(0), probability);

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
    public void addRecommendedInstanceIfNameAfterType(ITextState textExtractionState, IWord word) {
        if (textExtractionState == null || word == null) {
            return;
        }

        ImmutableList<String> sameLemmaTypes = CommonUtilities.getSimilarTypes(word, modelState);
        if (!sameLemmaTypes.isEmpty()) {
            textExtractionState.addType(word, sameLemmaTypes.get(0), probability);

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
    private void addRecommendedInstanceIfNortBeforeType(ITextState textExtractionState, IWord word) {
        if (textExtractionState == null || word == null) {
            return;
        }

        ImmutableList<String> sameLemmaTypes = CommonUtilities.getSimilarTypes(word, modelState);

        if (!sameLemmaTypes.isEmpty()) {
            textExtractionState.addType(word, sameLemmaTypes.get(0), probability);

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
    private void addRecommendedInstanceIfNortAfterType(ITextState textExtractionState, IWord word) {
        if (textExtractionState == null || word == null) {
            return;
        }

        ImmutableList<String> sameLemmaTypes = CommonUtilities.getSimilarTypes(word, modelState);
        if (!sameLemmaTypes.isEmpty()) {
            textExtractionState.addType(word, sameLemmaTypes.get(0), probability);

            ImmutableList<INounMapping> typeMappings = textExtractionState.getMappingsThatCouldBeAType(word);
            ImmutableList<INounMapping> nortMappings = textExtractionState.getMappingsThatCouldBeANort(word.getNextWord());

            CommonUtilities.addRecommendedInstancesFromNounMappings(sameLemmaTypes, nortMappings, typeMappings, recommendationState, probability);
        }
    }
}
