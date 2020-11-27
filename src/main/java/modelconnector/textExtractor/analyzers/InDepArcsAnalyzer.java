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
 * The analyzer examines the incoming dependency arcs of the current node.
 *
 * @author Sophie
 *
 */
public class InDepArcsAnalyzer extends TextExtractionAnalyzer {

    private double probability = ModelConnectorConfiguration.IN_DEP_ARCS_ANALYZER_PROBABILITY;

    /**
     * Creates a new InDepArcsAnalyzer
     *
     * @param graph
     *            the PARSE graph
     * @param textExtractionState
     *            the text extraction state
     */
    public InDepArcsAnalyzer(IGraph graph, TextExtractionState textExtractionState) {
        super(DependencyType.TEXT, graph, textExtractionState);
    }

    @Override
    public void exec(INode n) {
        String nodeValue = GraphUtils.getNodeValue(n);
        if (nodeValue.length() == 1 && !Character.isLetter(nodeValue.charAt(0))) {
            return;
        }

        examineIncomingDepArcs(n);
    }

    /**
     * Examines the incoming dependency arcs from the PARSE graph.
     *
     * @param n
     *            the node to check
     */
    private void examineIncomingDepArcs(INode n) {

        List<? extends IArc> incomingDepArcs = n.getIncomingArcsOfType(depArcType);

        for (IArc arc : incomingDepArcs) {
            String shortDepTag = arc.getAttributeValue("relationShort")
                                    .toString();

            if (shortDepTag.equals("appos") || shortDepTag.equals("nsubj") || shortDepTag.equals("poss")) {
                textExtractionState.addNort(n, GraphUtils.getNodeValue(n), probability);
            } else if (shortDepTag.equals("dobj") || shortDepTag.equals("iobj") || shortDepTag.equals("nmod")
                    || shortDepTag.equals("nsubjpass") || shortDepTag.equals("pobj")) {
                if (GraphUtils.checkForIndirectPrevDt(n, relArcType)) {
                    textExtractionState.addType(n, GraphUtils.getNodeValue(n), probability);
                }

                textExtractionState.addNort(n, GraphUtils.getNodeValue(n), probability);
            }
        }
    }

}
