package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import edu.kit.ipd.consistency_analyzer.datastructures.ITextExtractionState;

public interface ITextSolver extends ISolver {

	ITextSolver create(ITextExtractionState textExtractionState);

}
