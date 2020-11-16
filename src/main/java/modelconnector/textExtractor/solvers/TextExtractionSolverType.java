package modelconnector.textExtractor.solvers;

import edu.kit.ipd.parse.luna.graph.IGraph;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * Factory class for the creation of text extraction solvers.
 *
 * @author Sophie
 *
 */
public enum TextExtractionSolverType {

	/**
	 * Redirection of the creation of a multiple part solver.
	 */
	MULTIPLE_PART_SOLVER(MultiplePartSolver::new);

	private Crea c;

	TextExtractionSolverType(Crea c) {
		this.c = c;
	}

	/**
	 * Creates a new solver.
	 *
	 * @param graph               the PARSE graph to look up
	 * @param textExtractionState the text extraction state to work with
	 * @return the created solver
	 */
	public TextExtractionSolver create(IGraph graph, TextExtractionState textExtractionState) {
		return this.c.crea(graph, textExtractionState);
	}

	private interface Crea {
		TextExtractionSolver crea(IGraph graph, TextExtractionState textExtractionState);
	}

}
