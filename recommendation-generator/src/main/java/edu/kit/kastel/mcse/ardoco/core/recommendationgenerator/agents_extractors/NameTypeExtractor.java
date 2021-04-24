package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents_extractors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyType;
import edu.kit.kastel.mcse.ardoco.core.datastructures.common.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.RecommendationExtractor;

/**
 * This analyzer searches for name type patterns. If these patterns occur
 * recommendations are created.
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
	 * @param graph                the PARSE graph
	 * @param textExtractionState  the text extraction state
	 * @param modelExtractionState the model extraction state
	 * @param recommendationState  the recommendation state
	 */
	public NameTypeExtractor(ITextState textExtractionState, IModelState modelExtractionState, IRecommendationState recommendationState) {
		this(textExtractionState, modelExtractionState, recommendationState, GenericRecommendationConfig.DEFAULT_CONFIG);
	}

	public NameTypeExtractor(ITextState textExtractionState, IModelState modelExtractionState, IRecommendationState recommendationState,
			GenericRecommendationConfig config) {
		super(DependencyType.TEXT_MODEL_RECOMMENDATION, textExtractionState, modelExtractionState, recommendationState);
		probability = config.nameTypeAnalyzerProbability;
	}

	public NameTypeExtractor() {
		this(null, null, null);
	}

	@Override
	public RecommendationExtractor create(ITextState textState, IModelState modelExtractionState, IRecommendationState recommendationState,
			Configuration config) {
		return new NameTypeExtractor(textState, modelExtractionState, recommendationState, (GenericRecommendationConfig) config);
	}

	@Override
	public void setProbability(List<Double> probabilities) {
		if (probabilities.size() > 1) {
			throw new IllegalArgumentException(getName() + ": The given probabilities are more than needed!");
		} else if (probabilities.isEmpty()) {
			throw new IllegalArgumentException(getName() + ": The given probabilities are empty!");
		} else {
			probability = probabilities.get(0);
		}
	}

	@Override
	public void exec(IWord n) {
		checkForNameAfterType(textState, n);
		checkForNameBeforeType(textState, n);
		checkForNortBeforeType(textState, n);
		checkForNortAfterType(textState, n);
	}

	/**
	 * Checks if the current node is a type in the text extraction state. If the
	 * names of the text extraction state contain the previous node. If that's the
	 * case a recommendation for the combination of both is created.
	 *
	 * @param textExtractionState text extraction state
	 * @param n                   the current node
	 */
	private void checkForNameBeforeType(ITextState textExtractionState, IWord n) {
		IWord pre = n.getPreWord();

		Set<String> identifiers = modelState.getInstanceTypes().stream().map(type -> type.split(" ")).flatMap(Arrays::stream).collect(Collectors.toSet());
		identifiers.addAll(modelState.getInstanceTypes());

		List<String> similarTypes = identifiers.stream().filter(typeId -> SimilarityUtils.areWordsSimilar(typeId, n.getText())).collect(Collectors.toList());

		if (!similarTypes.isEmpty()) {
			textExtractionState.addType(n, similarTypes.get(0), probability);
			IInstance instance = tryToIdentify(textExtractionState, similarTypes, pre);

			List<INounMapping> nameMappings = textExtractionState.getMappingsThatCouldBeAName(pre);
			List<INounMapping> typeMappings = textExtractionState.getMappingsThatCouldBeAType(n);

			addRecommendedInstanceIfNodeNotNull(n, textExtractionState, instance, nameMappings, typeMappings);

		}
	}

	/**
	 * Checks if the current node is a type in the text extraction state. If the
	 * names of the text extraction state contain the following node. If that's the
	 * case a recommendation for the combination of both is created.
	 *
	 * @param textExtractionState text extraction state
	 * @param n                   the current node
	 */
	private void checkForNameAfterType(ITextState textExtractionState, IWord n) {
		IWord after = n.getNextWord();

		Set<String> identifiers = modelState.getInstanceTypes().stream().map(type -> type.split(" ")).flatMap(Arrays::stream).collect(Collectors.toSet());
		identifiers.addAll(modelState.getInstanceTypes());

		List<String> sameLemmaTypes = identifiers.stream().filter(typeId -> SimilarityUtils.areWordsSimilar(typeId, n.getText())).collect(Collectors.toList());
		if (!sameLemmaTypes.isEmpty()) {
			textExtractionState.addType(n, sameLemmaTypes.get(0), probability);
			IInstance instance = tryToIdentify(textExtractionState, sameLemmaTypes, after);

			List<INounMapping> typeMappings = textExtractionState.getMappingsThatCouldBeAType(n);
			List<INounMapping> nameMappings = textExtractionState.getMappingsThatCouldBeAName(after);

			addRecommendedInstanceIfNodeNotNull(n, textExtractionState, instance, nameMappings, typeMappings);

		}
	}

	/**
	 * Checks if the current node is a type in the text extraction state. If the
	 * name_or_types of the text extraction state contain the previous node. If
	 * that's the case a recommendation for the combination of both is created.
	 *
	 * @param textExtractionState text extraction state
	 * @param n                   the current node
	 */
	private void checkForNortBeforeType(ITextState textExtractionState, IWord n) {

		IWord pre = n.getPreWord();

		Set<String> identifiers = modelState.getInstanceTypes().stream().map(type -> type.split(" ")).flatMap(Arrays::stream).collect(Collectors.toSet());
		identifiers.addAll(modelState.getInstanceTypes());

		List<String> sameLemmaTypes = identifiers.stream().filter(typeId -> SimilarityUtils.areWordsSimilar(typeId, n.getText())).collect(Collectors.toList());

		if (!sameLemmaTypes.isEmpty()) {
			textExtractionState.addType(n, sameLemmaTypes.get(0), probability);
			IInstance instance = tryToIdentify(textExtractionState, sameLemmaTypes, pre);

			List<INounMapping> typeMappings = textExtractionState.getMappingsThatCouldBeAType(n);
			List<INounMapping> nortMappings = textExtractionState.getMappingsThatCouldBeANort(pre);

			addRecommendedInstanceIfNodeNotNull(n, textExtractionState, instance, nortMappings, typeMappings);
		}
	}

	/**
	 * Adds a RecommendedInstance to the recommendation state if the mapping of the
	 * current node exists. Otherwise a recommendation is added for each existing
	 * mapping.
	 *
	 * @param currentNode         the current node
	 * @param textExtractionState the text extraction state
	 * @param instance            the instance
	 * @param nameMappings        the name mappings
	 * @param typeMappings        the type mappings
	 */
	private void addRecommendedInstanceIfNodeNotNull(//
			IWord currentNode, ITextState textExtractionState, IInstance instance, List<INounMapping> nameMappings, List<INounMapping> typeMappings) {
		if (textExtractionState.getNounMappingsByNode(currentNode) != null && instance != null) {
			List<INounMapping> nmappings = textExtractionState.getNounMappingsByNode(currentNode);
			for (INounMapping nmapping : nmappings) {
				recommendationState.addRecommendedInstance(instance.getLongestName(), nmapping.getReference(), probability, nameMappings, typeMappings);
			}
		}
	}

	/**
	 * Checks if the current node is a type in the text extraction state. If the
	 * name_or_types of the text extraction state contain the afterwards node. If
	 * that's the case a recommendation for the combination of both is created.
	 *
	 * @param textExtractionState text extraction state
	 * @param n                   the current node
	 */
	private void checkForNortAfterType(ITextState textExtractionState, IWord n) {
		IWord after = n.getNextWord();

		Set<String> identifiers = modelState.getInstanceTypes().stream().map(type -> type.split(" ")).flatMap(Arrays::stream).collect(Collectors.toSet());
		identifiers.addAll(modelState.getInstanceTypes());

		List<String> sameLemmaTypes = identifiers.stream().filter(typeId -> SimilarityUtils.areWordsSimilar(typeId, n.getText())).collect(Collectors.toList());
		if (!sameLemmaTypes.isEmpty()) {
			textExtractionState.addType(n, sameLemmaTypes.get(0), probability);
			IInstance instance = tryToIdentify(textExtractionState, sameLemmaTypes, after);

			List<INounMapping> typeMappings = textExtractionState.getMappingsThatCouldBeAType(n);
			List<INounMapping> nortMappings = textExtractionState.getMappingsThatCouldBeANort(after);

			addRecommendedInstanceIfNodeNotNull(n, textExtractionState, instance, nortMappings, typeMappings);
		}
	}

	/**
	 * Tries to identify instances by the given similar types and the name of a
	 * given node. If an unambiguous instance can be found it is returned and the
	 * name is added to the text extraction state.
	 *
	 * @param textExtractioinState the next extraction state to work with
	 * @param similarTypes         the given similar types
	 * @param n                    the node for name identification
	 * @return the unique matching instance
	 */
	private IInstance tryToIdentify(ITextState textExtractioinState, List<String> similarTypes, IWord n) {
		List<IInstance> matchingInstances = new ArrayList<>();

		for (String type : similarTypes) {
			matchingInstances.addAll(modelState.getInstancesOfType(type));
		}

		matchingInstances = matchingInstances.stream().filter(i -> SimilarityUtils.areWordsOfListsSimilar(i.getNames(), List.of(n.getText())))
				.collect(Collectors.toList());

		if (matchingInstances.size() == 1) {

			textExtractioinState.addName(n, matchingInstances.get(0).getLongestName(), probability);
			return matchingInstances.get(0);
		}
		return null;
	}

}
