package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.extractors;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.RecommendationExtractor;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.GenericRecommendationConfig;
import edu.kit.kastel.mcse.ardoco.core.util.SimilarityUtils;

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

        Set<String> identifiers = modelState.getInstanceTypes().stream().map(type -> type.split(" ")).flatMap(Arrays::stream).collect(Collectors.toSet());
        identifiers.addAll(modelState.getInstanceTypes());

        ImmutableList<String> similarTypes = Lists.immutable
                .fromStream(identifiers.stream().filter(typeId -> SimilarityUtils.areWordsSimilar(typeId, word.getText())));

        if (!similarTypes.isEmpty()) {
            textExtractionState.addType(word, similarTypes.get(0), probability);

            ImmutableList<INounMapping> nameMappings = textExtractionState.getMappingsThatCouldBeAName(pre);
            ImmutableList<INounMapping> typeMappings = textExtractionState.getMappingsThatCouldBeAType(word);

            for (var nameMapping : nameMappings) {
                var name = nameMapping.getReference();
                for (var type : similarTypes) {
                    recommendationState.addRecommendedInstance(name, type, probability, nameMappings, typeMappings);
                }
            }

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

        Set<String> identifiers = modelState.getInstanceTypes().stream().map(type -> type.split(" ")).flatMap(Arrays::stream).collect(Collectors.toSet());
        identifiers.addAll(modelState.getInstanceTypes());

        ImmutableList<String> sameLemmaTypes = Lists.immutable
                .fromStream(identifiers.stream().filter(typeId -> SimilarityUtils.areWordsSimilar(typeId, word.getText())));
        if (!sameLemmaTypes.isEmpty()) {
            textExtractionState.addType(word, sameLemmaTypes.get(0), probability);

            ImmutableList<INounMapping> typeMappings = textExtractionState.getMappingsThatCouldBeAType(word);
            ImmutableList<INounMapping> nameMappings = textExtractionState.getMappingsThatCouldBeAName(after);

            for (var nameMapping : nameMappings) {
                var name = nameMapping.getReference();
                for (var type : sameLemmaTypes) {
                    recommendationState.addRecommendedInstance(name, type, probability, nameMappings, typeMappings);
                }
            }

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

        Set<String> identifiers = modelState.getInstanceTypes().stream().map(type -> type.split(" ")).flatMap(Arrays::stream).collect(Collectors.toSet());
        identifiers.addAll(modelState.getInstanceTypes());

        ImmutableList<String> sameLemmaTypes = Lists.immutable
                .fromStream(identifiers.stream().filter(typeId -> SimilarityUtils.areWordsSimilar(typeId, word.getText())));

        if (!sameLemmaTypes.isEmpty()) {
            textExtractionState.addType(word, sameLemmaTypes.get(0), probability);

            ImmutableList<INounMapping> typeMappings = textExtractionState.getMappingsThatCouldBeAType(word);
            ImmutableList<INounMapping> nortMappings = textExtractionState.getMappingsThatCouldBeANort(pre);

            for (var nameMapping : nortMappings) {
                var name = nameMapping.getReference();
                for (var type : sameLemmaTypes) {
                    recommendationState.addRecommendedInstance(name, type, probability, nortMappings, typeMappings);
                }
            }

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

        Set<String> identifiers = modelState.getInstanceTypes().stream().map(type -> type.split(" ")).flatMap(Arrays::stream).collect(Collectors.toSet());
        identifiers.addAll(modelState.getInstanceTypes());

        ImmutableList<String> sameLemmaTypes = Lists.immutable
                .fromStream(identifiers.stream().filter(typeId -> SimilarityUtils.areWordsSimilar(typeId, word.getText())));
        if (!sameLemmaTypes.isEmpty()) {
            textExtractionState.addType(word, sameLemmaTypes.get(0), probability);

            ImmutableList<INounMapping> typeMappings = textExtractionState.getMappingsThatCouldBeAType(word);
            ImmutableList<INounMapping> nortMappings = textExtractionState.getMappingsThatCouldBeANort(after);

            for (var nameMapping : nortMappings) {
                var name = nameMapping.getReference();
                for (var type : sameLemmaTypes) {
                    recommendationState.addRecommendedInstance(name, type, probability, nortMappings, typeMappings);
                }
            }

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
                String name = instance.getLongestName();
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
    // TODO: think about changing this. The main problem is that this extractor is used in the RecommendationGenerator
    // that should be independent from the model. Therefore, this violates this assumption when iterating over the
    // instances of a certain type
    private IModelInstance tryToIdentify(ITextState textExtractionState, ImmutableList<String> similarTypes, IWord word) {
        if (textExtractionState == null || similarTypes == null || word == null) {
            return null;
        }
        MutableList<IModelInstance> matchingInstances = Lists.mutable.empty();

        for (String type : similarTypes) {
            matchingInstances.addAll(modelState.getInstancesOfType(type).castToCollection());
        }

        var text = word.getText();
        matchingInstances = matchingInstances.select(i -> SimilarityUtils.areWordsOfListsSimilar(i.getNames(), Lists.immutable.with(text)));

        if (!matchingInstances.isEmpty()) {
            var modelInstance = matchingInstances.get(0);
            textExtractionState.addName(word, modelInstance.getLongestName(), probability);
            return modelInstance;
        }
        return null;
    }

}
