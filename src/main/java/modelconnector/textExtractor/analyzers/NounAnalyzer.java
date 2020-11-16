package modelconnector.textExtractor.analyzers;

import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import modelconnector.DependencyType;
import modelconnector.helpers.GraphUtils;
import modelconnector.helpers.ModelConnectorConfiguration;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * The analyzer classifies nouns.
 *
 * @author Sophie
 *
 */
public class NounAnalyzer extends TextExtractionAnalyzer {

	double probability = ModelConnectorConfiguration.nounAnalyzer_Probability;

	/**
	 * Creates a new NounAnalyzer
	 *
	 * @param graph               PARSE graph to run on
	 * @param textExtractionState the text extraction state
	 */
	public NounAnalyzer(IGraph graph, TextExtractionState textExtractionState) {
		super(DependencyType.TEXT, graph, textExtractionState);
	}

	@Override
	public void exec(INode n) {

		String nodeValue = GraphUtils.getNodeValue(n);
		if (nodeValue.length() == 1 && !Character.isLetter(nodeValue.charAt(0))) {
			return;
		}

		this.findSingleNouns(n);

	}

	/**
	 * Finds all nouns and adds them as name-or-type mappings (and types) to the
	 * text extraction state.
	 *
	 * @param n node to check
	 */
	private void findSingleNouns(INode n) {
		String pos = n.getAttributeValue("pos").toString();
		if (pos.equals("NNP") || //
				pos.equals("NN") || //
				pos.equals("NNPS")) {

			textExtractionState.addNort(n, GraphUtils.getNodeValue(n), probability);
		}
		if (pos.equals("NNS")) {
			textExtractionState.addType(n, GraphUtils.getNodeValue(n), probability);
		}
	}

}
