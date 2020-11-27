package modelconnector.textExtractor.analyzers;

import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import modelconnector.DependencyType;
import modelconnector.helpers.GraphUtils;
import modelconnector.helpers.ModelConnectorConfiguration;
import modelconnector.helpers.SimilarityUtils;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * This analyzer classifies all nodes, containing separators, as names and adds
 * them as mappings to the current text extraction state.
 *
 * @author Sophie
 *
 */
public class SeparatedNamesAnalyzer extends TextExtractionAnalyzer {

	double probability = ModelConnectorConfiguration.SEPARATED_NAMES_ANALYZER_PROBABILITY;

	/**
	 * Creates a new SeparatedNamesIdentifier.
	 *
	 * @param graph               current PARSE graph
	 * @param textExtractionState the text extraction state
	 */
	public SeparatedNamesAnalyzer(IGraph graph, TextExtractionState textExtractionState) {
		super(DependencyType.TEXT, graph, textExtractionState);
	}

	/***
	 * Checks if Node Value contains separator. If true, it is splitted and added
	 * separately to the names of the text extraction state.
	 */
	@Override
	public void exec(INode node) {
		checkForSeparatedNode(node);
	}

	/***
	 * Checks if Node Value contains separator. If true, it is splitted and added
	 * separately to the names of the text extraction state.
	 *
	 * @param n node to check
	 */
	private void checkForSeparatedNode(INode n) {
		if (SimilarityUtils.containsSeparator(GraphUtils.getNodeValue(n))) {
			textExtractionState.addName(n, GraphUtils.getNodeValue(n), probability);
		}
	}

}
