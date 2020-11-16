package modelconnector.textExtractor.analyzers;

import java.util.List;

import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import modelconnector.DependencyType;
import modelconnector.helpers.GraphUtils;
import modelconnector.helpers.ModelConnectorConfiguration;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * The analyzer examines the outgoing arcs of the current node.
 *
 * @author Sophie
 *
 */
public class OutDepArcsAnalyzer extends TextExtractionAnalyzer {

	private double probability = ModelConnectorConfiguration.outDepArcsAnalyzer_Probability;

	/**
	 * Creates a new OutDepArcsAnalyzer
	 *
	 * @param graph               the current PARSE graph
	 * @param textExtractionState the text extraction state
	 */
	public OutDepArcsAnalyzer(IGraph graph, TextExtractionState textExtractionState) {
		super(DependencyType.TEXT, graph, textExtractionState);
	}

	@Override
	public void exec(INode n) {

		String nodeValue = GraphUtils.getNodeValue(n);
		if (nodeValue.length() == 1 && !Character.isLetter(nodeValue.charAt(0))) {
			return;
		}
		examineOutgoingDepArcs(n);
	}

	/**
	 * Examines the outgoing dependencies of a node.
	 *
	 * @param n the node to check
	 */
	private void examineOutgoingDepArcs(INode n) {

		List<? extends IArc> outgoingDepArcs = n.getOutgoingArcsOfType(depArcType);

		for (IArc arc : outgoingDepArcs) {
			String shortDepTag = arc.getAttributeValue("relationShort").toString();

			if (shortDepTag.equals("agent")) {
				textExtractionState.addNort(n, GraphUtils.getNodeValue(n), probability);

			} else if (shortDepTag.equals("num")) {
				textExtractionState.addType(n, GraphUtils.getNodeValue(n), probability);

			} else if (shortDepTag.equals("predet")) {
				textExtractionState.addType(n, GraphUtils.getNodeValue(n), probability);

			} else if (shortDepTag.equals("rcmod")) {
				// plural ? Best./ unbest.?
				textExtractionState.addNort(n, GraphUtils.getNodeValue(n), probability);
			}
		}
	}
}
