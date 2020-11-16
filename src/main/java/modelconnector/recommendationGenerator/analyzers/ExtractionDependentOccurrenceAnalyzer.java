package modelconnector.recommendationGenerator.analyzers;

import java.util.List;
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
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * This analyzer searches for the occurrence of instance names and types of the
 * extraction state and adds them as names and types to the text extraction
 * state.
 *
 * @author Sophie
 *
 */
public class ExtractionDependentOccurrenceAnalyzer extends RecommendationAnalyzer {

	private double probability = ModelConnectorConfiguration.extractionDependentOccurrenceAnalyzer_Probability;

	/**
	 * Creates a new extraction dependent occurrence marker.
	 *
	 * @param graph                the PARSE graph to run on
	 * @param textExtractionState  the text extraction state
	 * @param modelExtractionState the model extraction state to work with
	 * @param recommendationState  the state with the recommendations
	 */
	public ExtractionDependentOccurrenceAnalyzer(//
			IGraph graph, TextExtractionState textExtractionState, ModelExtractionState modelExtractionState, RecommendationState recommendationState) {
		super(DependencyType.TEXT_MODEL, graph, textExtractionState, modelExtractionState, recommendationState);
	}

	@Override
	public void exec(INode n) {

		searchForName(n);
		searchForType(n);
	}

	/**
	 * This method checks whether a given node is a name of an instance given in the
	 * model extraction state. If it appears to be a name this is stored in the text
	 * extraction state. If multiple options are available the node value is taken
	 * as reference.
	 *
	 * @param n the node to check
	 */
	private void searchForName(INode n) {
		List<Instance> instances = modelExtractionState.getInstances().stream().filter(//
				i -> SimilarityUtils.areWordsOfListsSimilar(i.getNames(), List.of(GraphUtils.getNodeValue(n)))).collect(Collectors.toList());
		if (instances.size() == 1) {
			textExtractionState.addName(n, instances.get(0).getLongestName(), probability);

		} else if (instances.size() > 1) {
			textExtractionState.addName(n, GraphUtils.getNodeValue(n), probability);
		}
	}

	/**
	 * This method checks whether a given node is a type of an instance given in the
	 * model extraction state. If it appears to be a type this is stored in the text
	 * extraction state. If multiple options are available the node value is taken
	 * as reference.
	 *
	 * @param n the node to check
	 */
	private void searchForType(INode n) {
		List<Instance> instances = modelExtractionState.getInstances().stream().filter(//
				i -> SimilarityUtils.areWordsOfListsSimilar(i.getTypes(), List.of(GraphUtils.getNodeValue(n)))).collect(Collectors.toList());
		if (instances.size() == 1) {
			textExtractionState.addType(n, instances.get(0).getLongestType(), probability);

		} else if (instances.size() > 1) {
			textExtractionState.addType(n, GraphUtils.getNodeValue(n), probability);
		}
	}

}
