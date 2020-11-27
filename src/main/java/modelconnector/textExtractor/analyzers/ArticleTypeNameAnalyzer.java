package modelconnector.textExtractor.analyzers;

import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import modelconnector.DependencyType;
import modelconnector.helpers.GraphUtils;
import modelconnector.helpers.ModelConnectorConfiguration;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * This analyzer finds patterns like article type name or article name type.
 *
 * @author Sophie
 *
 */
public class ArticleTypeNameAnalyzer extends TextExtractionAnalyzer {

	private double probability = ModelConnectorConfiguration.ARTICLE_TYPE_NAME_ANALYZER_PROBABILITY;

	/**
	 * Creates a new article type name analyzer.
	 *
	 * @param graph
	 * @param textExtractionState the text extraction state
	 */
	public ArticleTypeNameAnalyzer(IGraph graph, TextExtractionState textExtractionState) {
		super(DependencyType.TEXT, graph, textExtractionState);
	}

	@Override
	public void exec(INode n) {

		if (checkIfNodeIsName(n)) {
			return;
		} else {
			checkIfNodeIsType(n);
		}
	}

	/**
	 * If the current node is contained by name-or-type mappings, the previous node
	 * is contained by type nodes and the preprevious an article the node is added
	 * as a name mapping.
	 *
	 * @param n node to check
	 */
	private boolean checkIfNodeIsName(INode n) {
		if (textExtractionState.isNodeContainedByNameOrTypeNodes(n)) {

			INode prevNode = GraphUtils.getPreviousNode(n, relArcType);
			if (prevNode != null && textExtractionState.isNodeContainedByTypeNodes(prevNode)) {

				if (GraphUtils.checkIfPrevNodeIsDt(prevNode, relArcType)) {
					textExtractionState.addName(n, GraphUtils.getNodeValue(n), probability);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * If the current node is contained by name-or-type mappings, the previous node
	 * is contained by name nodes and the preprevious an article the node is added
	 * as a type mapping.
	 *
	 * @param n node to check
	 */
	private boolean checkIfNodeIsType(INode n) {
		if (textExtractionState.isNodeContainedByNameOrTypeNodes(n)) {

			INode prevNode = GraphUtils.getPreviousNode(n, relArcType);
			if (prevNode != null && textExtractionState.isNodeContainedByNameNodes(prevNode)) {

				if (GraphUtils.checkIfPrevNodeIsDt(prevNode, relArcType)) {
					textExtractionState.addType(n, GraphUtils.getNodeValue(n), probability);
					return true;
				}

			}

		}
		return false;

	}

}
