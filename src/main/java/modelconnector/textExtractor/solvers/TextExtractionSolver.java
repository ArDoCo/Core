package modelconnector.textExtractor.solvers;

import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.IGraph;
import modelconnector.DependencyType;
import modelconnector.Solver;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * A solver that creates noun mappings or relations.
 *
 * @author Sophie
 *
 */
public abstract class TextExtractionSolver extends Solver {

	protected IGraph graph;
	protected IArcType depArcType;
	protected IArcType relArcType;
	protected TextExtractionState textExtractionState;

	/**
	 * Creates a new solver.
	 *
	 * @param dependencyType      the dependencies of the analyzer
	 * @param graph               the PARSE graph to look up
	 * @param textExtractionState the text extraction state to work with
	 */
	public TextExtractionSolver(DependencyType dependencyType, IGraph graph, TextExtractionState textExtractionState) {
		super(dependencyType);
		this.graph = graph;
		depArcType = graph.getArcType("typedDependency");
		relArcType = graph.getArcType("relation");
		this.textExtractionState = textExtractionState;
	}

	@Override
	public abstract void exec();
}
