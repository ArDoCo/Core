package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import edu.kit.ipd.consistency_analyzer.datastructures.IModelExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendationState;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextExtractionState;

public interface IRecommendationAnalyzer extends IAnalyzer {

	IRecommendationAnalyzer create(//
			ITextExtractionState textExtractionState, IModelExtractionState modelExtractionState, //
			IRecommendationState recommendationState);
}
