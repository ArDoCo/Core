package modelconnector.recommendationGenerator.analyzers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import modelconnector.DependencyType;
import modelconnector.helpers.GraphUtils;
import modelconnector.helpers.ModelConnectorConfiguration;
import modelconnector.helpers.SimilarityUtils;
import modelconnector.modelExtractor.state.Instance;
import modelconnector.modelExtractor.state.ModelExtractionState;
import modelconnector.recommendationGenerator.state.RecommendationState;
import modelconnector.textExtractor.state.NounMapping;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * This analyzer searches for name type patterns. If these patterns occure
 * recommendations are created.
 *
 * @author Sophie
 *
 */

public class NameTypeAnalyzer extends RecommendationAnalyzer {

	/**
	 * Creates a new NameTypeAnalyzer.
	 *
	 * @param graph                the PARSE graph
	 * @param textExtractionState  the text extraction state
	 * @param modelExtractionState the model extraction state
	 * @param recommendationState  the recommendation state
	 */
	public NameTypeAnalyzer(IGraph graph, TextExtractionState textExtractionState, ModelExtractionState modelExtractionState, RecommendationState recommendationState) {
		super(DependencyType.TEXT_MODEL_RECOMMENDATION, graph, textExtractionState, modelExtractionState, recommendationState);
	}

	private double probability = ModelConnectorConfiguration.nameTypeAnalyzerProbability;

	@Override
	public void exec(INode n) {
		checkForNameAfterType(textExtractionState, n);
		checkForNameBeforeType(textExtractionState, n);
		checkForNortBeforeType(textExtractionState, n);
		checkForNortAfterType(textExtractionState, n);
	}

	/**
	 * Checks if the current node is a type in the text extraction state. If the
	 * names of the text extraction state contain the previous node. If that's the
	 * case a recommendation for the combination of both is created.
	 *
	 * @param textExtractionState text extraction state
	 * @param n                   the current node
	 */
	private void checkForNameBeforeType(TextExtractionState textExtractionState, INode n) {
		INode pre = GraphUtils.getPreviousNode(n, relArcType);

		Set<String> identifiers = modelExtractionState.getInstanceTypes().stream().map(type -> type.split(" ")).flatMap(Arrays::stream).collect(Collectors.toSet());
		identifiers.addAll(modelExtractionState.getInstanceTypes());

		List<String> similarTypes = identifiers.stream().filter(typeId -> SimilarityUtils.areWordsSimilar(typeId, GraphUtils.getNodeValue(n))).collect(Collectors.toList());

		if (!similarTypes.isEmpty()) {
			textExtractionState.addType(n, similarTypes.get(0), probability);
			Instance instance = tryToIdentify(textExtractionState, similarTypes, pre);

			List<NounMapping> typeMappings = textExtractionState.getTypeNodesByNode(n);
			List<NounMapping> nameMappings = textExtractionState.getNameNodesByNode(pre);

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
	private void checkForNameAfterType(TextExtractionState textExtractionState, INode n) {
		INode after = GraphUtils.getNextNode(n, relArcType);

		Set<String> identifiers = modelExtractionState.getInstanceTypes().stream().map(type -> type.split(" ")).flatMap(Arrays::stream).collect(Collectors.toSet());
		identifiers.addAll(modelExtractionState.getInstanceTypes());

		List<String> sameLemmaTypes = identifiers.stream().filter(typeId -> SimilarityUtils.areWordsSimilar(typeId, GraphUtils.getNodeValue(n))).collect(Collectors.toList());
		if (!sameLemmaTypes.isEmpty()) {
			textExtractionState.addType(n, sameLemmaTypes.get(0), probability);
			Instance instance = tryToIdentify(textExtractionState, sameLemmaTypes, after);

			List<NounMapping> typeMappings = textExtractionState.getTypeNodesByNode(n);
			List<NounMapping> nameMappings = textExtractionState.getNameNodesByNode(after);

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
	private void checkForNortBeforeType(TextExtractionState textExtractionState, INode n) {

		INode pre = GraphUtils.getPreviousNode(n, relArcType);

		Set<String> identifiers = modelExtractionState.getInstanceTypes().stream().map(type -> type.split(" ")).flatMap(Arrays::stream).collect(Collectors.toSet());
		identifiers.addAll(modelExtractionState.getInstanceTypes());

		List<String> sameLemmaTypes = identifiers.stream().filter(typeId -> SimilarityUtils.areWordsSimilar(typeId, GraphUtils.getNodeValue(n))).collect(Collectors.toList());

		if (!sameLemmaTypes.isEmpty()) {
			textExtractionState.addType(n, sameLemmaTypes.get(0), probability);
			Instance instance = tryToIdentify(textExtractionState, sameLemmaTypes, pre);

			List<NounMapping> typeMappings = textExtractionState.getTypeNodesByNode(n);
			List<NounMapping> nortMappings = textExtractionState.getNortNodesByNode(pre);

			addRecommendedInstanceIfNodeNotNull(n, textExtractionState, instance, nortMappings, typeMappings);
		}
	}

	/**
	 * Adds a RecommendedInstance to the recommendation state if the mapping of the
	 * current node exists. Otherwise a recommendation is added foreach existing
	 * mapping.
	 *
	 * @param currentNode         the current node
	 * @param textExtractionState the text extraction state
	 * @param instance            the instance
	 * @param nameMappings        the name mappings
	 * @param typeMappings        the type mappings
	 */
	private void addRecommendedInstanceIfNodeNotNull(//
			INode currentNode, TextExtractionState textExtractionState, Instance instance, List<NounMapping> nameMappings, List<NounMapping> typeMappings) {
		if (textExtractionState.getNounMappingsByNode(currentNode) != null && instance != null) {
			List<NounMapping> nmappings = textExtractionState.getNounMappingsByNode(currentNode);
			for (NounMapping nmapping : nmappings) {
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
	private void checkForNortAfterType(TextExtractionState textExtractionState, INode n) {
		INode after = GraphUtils.getNextNode(n, relArcType);

		Set<String> identifiers = modelExtractionState.getInstanceTypes().stream().map(type -> type.split(" ")).flatMap(Arrays::stream).collect(Collectors.toSet());
		identifiers.addAll(modelExtractionState.getInstanceTypes());

		List<String> sameLemmaTypes = identifiers.stream().filter(typeId -> SimilarityUtils.areWordsSimilar(typeId, GraphUtils.getNodeValue(n))).collect(Collectors.toList());
		if (!sameLemmaTypes.isEmpty()) {
			textExtractionState.addType(n, sameLemmaTypes.get(0), probability);
			Instance instance = tryToIdentify(textExtractionState, sameLemmaTypes, after);

			List<NounMapping> typeMappings = textExtractionState.getTypeNodesByNode(n);
			List<NounMapping> nortMappings = textExtractionState.getNortNodesByNode(after);

			addRecommendedInstanceIfNodeNotNull(n, textExtractionState, instance, nortMappings, typeMappings);
		}
	}

	/**
	 * Tries to identify instances by the given similar types and the name of a
	 * given node. If an unambiguous instance can be found it is returned and the
	 * name is added to the text extraction state.
	 *
	 * @param textExtractioinState the ntext extraction state to work with
	 * @param similarTypes         the given similar types
	 * @param n                    the node for name identification
	 * @return the unique matching instance
	 */
	private Instance tryToIdentify(TextExtractionState textExtractioinState, List<String> similarTypes, INode n) {
		List<Instance> matchingInstances = new ArrayList<>();

		for (String type : similarTypes) {
			matchingInstances.addAll(modelExtractionState.getInstancesOfType(type));
		}

		matchingInstances = matchingInstances.stream().filter(i -> SimilarityUtils.areWordsOfListsSimilar(i.getNames(), List.of(GraphUtils.getNodeValue(n)))).collect(Collectors.toList());

		if (matchingInstances.size() == 1) {

			textExtractioinState.addName(n, matchingInstances.get(0).getLongestName(), probability);
			return matchingInstances.get(0);
		}
		return null;
	}

}
