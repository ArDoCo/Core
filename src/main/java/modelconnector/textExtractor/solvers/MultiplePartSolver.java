package modelconnector.textExtractor.solvers;

import java.util.ArrayList;
import java.util.List;

import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import modelconnector.DependencyType;
import modelconnector.helpers.GraphUtils;
import modelconnector.helpers.ModelConnectorConfiguration;
import modelconnector.helpers.Utilis;
import modelconnector.textExtractor.state.MappingKind;
import modelconnector.textExtractor.state.NounMapping;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * WORK IN PROGRESS
 *
 * @author Sophie
 *
 */
public class MultiplePartSolver extends TextExtractionSolver {

	private double probability = ModelConnectorConfiguration.multiplePartSolver_Probability;

	/**
	 * Creates a new multiple part solver.
	 *
	 * @param graph               the PARSE graph
	 * @param textExtractionState the text extraction state
	 */
	public MultiplePartSolver(IGraph graph, TextExtractionState textExtractionState) {
		super(DependencyType.TEXT, graph, textExtractionState);
	}

	@Override
	public void exec() {

		searchForName();
		searchForType();
	}

	private void searchForName() {
		for (NounMapping nameMap : textExtractionState.getNames()) {
			List<INode> nameNodes = new ArrayList<>(nameMap.getNodes());
			for (INode n : nameNodes) {
				INode pre = GraphUtils.getPreviousNode(n, relArcType);
				if (pre != null && textExtractionState.isNodeContainedByNounMappings(pre) && !textExtractionState.isNodeContainedByTypeNodes(pre)) {
					String ref = GraphUtils.getNodeValue(pre) + " " + GraphUtils.getNodeValue(n);
					addTerm(ref, pre, n, MappingKind.NAME);
				}
			}
		}
	}

	private void searchForType() {
		for (NounMapping typeMap : textExtractionState.getTypes()) {
			List<INode> typeNodes = new ArrayList<>(typeMap.getNodes());
			for (INode n : typeNodes) {
				INode pre = GraphUtils.getPreviousNode(n, relArcType);
				if (pre != null && textExtractionState.isNodeContainedByNounMappings(pre) && !textExtractionState.isNodeContainedByNameNodes(pre)) {
					String ref = GraphUtils.getNodeValue(pre) + " " + GraphUtils.getNodeValue(n);
					addTerm(ref, pre, n, MappingKind.TYPE);
				}
			}
		}
	}

	private void addTerm(String ref, INode pre, INode n, MappingKind kind) {

		List<NounMapping> preMappings = textExtractionState.getNounMappingsByNode(pre);
		List<NounMapping> nMappings = textExtractionState.getNounMappingsByNode(n);

		List<List<NounMapping>> cartesianProduct = Utilis.cartesianProduct(preMappings, List.of(nMappings));

		for (List<NounMapping> possibleCombination : cartesianProduct) {
			textExtractionState.addTerm(ref, possibleCombination.get(0), possibleCombination.get(1), kind, probability);
		}

	}
}
