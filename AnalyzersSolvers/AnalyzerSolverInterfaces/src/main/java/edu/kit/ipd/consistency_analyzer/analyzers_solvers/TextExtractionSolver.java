package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import edu.kit.ipd.consistency_analyzer.datastructures.ITextExtractionState;

/**
 * A solver that creates noun mappings or relations.
 *
 * @author Sophie
 *
 */
public abstract class TextExtractionSolver extends Solver implements ITextSolver {

	protected ITextExtractionState textExtractionState;

	/**
	 * Creates a new solver.
	 *
	 * @param dependencyType      the dependencies of the analyzer
	 * @param graph               the PARSE graph to look up
	 * @param textExtractionState the text extraction state to work with
	 */
	protected TextExtractionSolver(DependencyType dependencyType, ITextExtractionState textExtractionState) {
		super(dependencyType);
		this.textExtractionState = textExtractionState;
	}
}
