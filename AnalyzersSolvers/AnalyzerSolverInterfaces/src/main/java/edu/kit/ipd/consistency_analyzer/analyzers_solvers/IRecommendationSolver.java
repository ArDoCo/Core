package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import edu.kit.ipd.consistency_analyzer.datastructures.IModelExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendationState;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextExtractionState;

public interface IRecommendationSolver extends ISolver {

	IRecommendationSolver create(//
			ITextExtractionState textExtractionState, IModelExtractionState modelExtractionState, //
			IRecommendationState recommendationState);
}
