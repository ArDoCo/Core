package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import edu.kit.ipd.consistency_analyzer.datastructures.ITextExtractionState;

public interface ITextAnalyzer extends IAnalyzer {

	ITextAnalyzer create(ITextExtractionState textExtractionState);

}
