package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.extractors;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.common.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.RecommendationExtractor;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.GenericRecommendationConfig;

/**
 * This analyzer searches for name type patterns. If these patterns occur recommendations are created.
 *
 * @author Sophie
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
    public void exec(IWord n) {
        checkForNameAfterType(textState, n);
        checkForNameBeforeType(textState, n);
        checkForNortBeforeType(textState, n);
        checkForNortAfterType(textState, n);
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the names of the text extraction state
     * contain the previous node. If that's the case a recommendation for the combination of both is created.
     *
     * @param textExtractionState text extraction state
     * @param n                   the current node
     */
    private void checkForNameBeforeType(ITextState textExtractionState, IWord n) {
        if (textExtractionState == null || n == null) {
            return;
        }

        IWord pre = n.getPreWord();

        Set<String> identifiers = modelState.getInstanceTypes().stream().map(type -> type.split(" ")).flatMap(Arrays::stream).collect(Collectors.toSet());
        identifiers.addAll(modelState.getInstanceTypes());

        ImmutableList<String> similarTypes = Lists.immutable
                .fromStream(identifiers.stream().filter(typeId -> SimilarityUtils.areWordsSimilar(typeId, n.getText())));

        if (!similarTypes.isEmpty()) {
            textExtractionState.addType(n, similarTypes.get(0), probability);
            IModelInstance instance = tryToIdentify(textExtractionState, similarTypes, pre);

            ImmutableList<INounMapping> nameMappings = textExtractionState.getMappingsThatCouldBeAName(pre);
            ImmutableList<INounMapping> typeMappings = textExtractionState.getMappingsThatCouldBeAType(n);

            addRecommendedInstanceIfNodeNotNull(n, textExtractionState, instance, nameMappings, typeMappings);

        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the names of the text extraction state
     * contain the following node. If that's the case a recommendation for the combination of both is created.
     *
     * @param textExtractionState text extraction state
     * @param n                   the current node
     */
    private void checkForNameAfterType(ITextState textExtractionState, IWord n) {
        if (textExtractionState == null || n == null) {
            return;
        }

        IWord after = n.getNextWord();

        Set<String> identifiers = modelState.getInstanceTypes().stream().map(type -> type.split(" ")).flatMap(Arrays::stream).collect(Collectors.toSet());
        identifiers.addAll(modelState.getInstanceTypes());

        ImmutableList<String> sameLemmaTypes = Lists.immutable
                .fromStream(identifiers.stream().filter(typeId -> SimilarityUtils.areWordsSimilar(typeId, n.getText())));
        if (!sameLemmaTypes.isEmpty()) {
            textExtractionState.addType(n, sameLemmaTypes.get(0), probability);
            IModelInstance instance = tryToIdentify(textExtractionState, sameLemmaTypes, after);

            ImmutableList<INounMapping> typeMappings = textExtractionState.getMappingsThatCouldBeAType(n);
            ImmutableList<INounMapping> nameMappings = textExtractionState.getMappingsThatCouldBeAName(after);

            addRecommendedInstanceIfNodeNotNull(n, textExtractionState, instance, nameMappings, typeMappings);

        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the name_or_types of the text extraction
     * state contain the previous node. If that's the case a recommendation for the combination of both is created.
     *
     * @param textExtractionState text extraction state
     * @param n                   the current node
     */
    private void checkForNortBeforeType(ITextState textExtractionState, IWord n) {
        if (textExtractionState == null || n == null) {
            return;
        }

        IWord pre = n.getPreWord();

        Set<String> identifiers = modelState.getInstanceTypes().stream().map(type -> type.split(" ")).flatMap(Arrays::stream).collect(Collectors.toSet());
        identifiers.addAll(modelState.getInstanceTypes());

        ImmutableList<String> sameLemmaTypes = Lists.immutable
                .fromStream(identifiers.stream().filter(typeId -> SimilarityUtils.areWordsSimilar(typeId, n.getText())));

        if (!sameLemmaTypes.isEmpty()) {
            textExtractionState.addType(n, sameLemmaTypes.get(0), probability);
            IModelInstance instance = tryToIdentify(textExtractionState, sameLemmaTypes, pre);

            ImmutableList<INounMapping> typeMappings = textExtractionState.getMappingsThatCouldBeAType(n);
            ImmutableList<INounMapping> nortMappings = textExtractionState.getMappingsThatCouldBeANort(pre);

            addRecommendedInstanceIfNodeNotNull(n, textExtractionState, instance, nortMappings, typeMappings);
        }
    }

    /**
     * Adds a RecommendedInstance to the recommendation state if the mapping of the current node exists. Otherwise a
     * recommendation is added for each existing mapping.
     *
     * @param currentNode         the current node
     * @param textExtractionState the text extraction state
     * @param instance            the instance
     * @param nameMappings        the name mappings
     * @param typeMappings        the type mappings
     */
    private void addRecommendedInstanceIfNodeNotNull(//
            IWord currentNode, ITextState textExtractionState, IModelInstance instance, ImmutableList<INounMapping> nameMappings,
            ImmutableList<INounMapping> typeMappings) {
        if (textExtractionState.getNounMappingsByNode(currentNode) != null && instance != null) {
            ImmutableList<INounMapping> nmappings = textExtractionState.getNounMappingsByNode(currentNode);
            for (INounMapping nmapping : nmappings) {
                recommendationState.addRecommendedInstance(instance.getLongestName(), nmapping.getReference(), probability, nameMappings, typeMappings);
            }
        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the name_or_types of the text extraction
     * state contain the afterwards node. If that's the case a recommendation for the combination of both is created.
     *
     * @param textExtractionState text extraction state
     * @param n                   the current node
     */
    private void checkForNortAfterType(ITextState textExtractionState, IWord n) {
        if (textExtractionState == null || n == null) {
            return;
        }

        IWord after = n.getNextWord();

        Set<String> identifiers = modelState.getInstanceTypes().stream().map(type -> type.split(" ")).flatMap(Arrays::stream).collect(Collectors.toSet());
        identifiers.addAll(modelState.getInstanceTypes());

        ImmutableList<String> sameLemmaTypes = Lists.immutable
                .fromStream(identifiers.stream().filter(typeId -> SimilarityUtils.areWordsSimilar(typeId, n.getText())));
        if (!sameLemmaTypes.isEmpty()) {
            textExtractionState.addType(n, sameLemmaTypes.get(0), probability);
            IModelInstance instance = tryToIdentify(textExtractionState, sameLemmaTypes, after);

            ImmutableList<INounMapping> typeMappings = textExtractionState.getMappingsThatCouldBeAType(n);
            ImmutableList<INounMapping> nortMappings = textExtractionState.getMappingsThatCouldBeANort(after);

            addRecommendedInstanceIfNodeNotNull(n, textExtractionState, instance, nortMappings, typeMappings);
        }
    }

    /**
     * Tries to identify instances by the given similar types and the name of a given node. If an unambiguous instance
     * can be found it is returned and the name is added to the text extraction state.
     *
     * @param textExtractioinState the next extraction state to work with
     * @param similarTypes         the given similar types
     * @param n                    the node for name identification
     * @return the unique matching instance
     */
    private IModelInstance tryToIdentify(ITextState textExtractioinState, ImmutableList<String> similarTypes, IWord n) {
        if (textExtractioinState == null || similarTypes == null || n == null) {
            return null;
        }
        MutableList<IModelInstance> matchingInstances = Lists.mutable.empty();

        for (String type : similarTypes) {
            matchingInstances.addAll(modelState.getInstancesOfType(type).castToCollection());
        }

        var text = n.getText();
        matchingInstances = matchingInstances.select(i -> SimilarityUtils.areWordsOfListsSimilar(i.getNames(), Lists.immutable.with(text)));

        if (matchingInstances.size() == 1) {
            textExtractioinState.addName(n, matchingInstances.get(0).getLongestName(), probability);
            return matchingInstances.get(0);
        }
        return null;
    }

}
