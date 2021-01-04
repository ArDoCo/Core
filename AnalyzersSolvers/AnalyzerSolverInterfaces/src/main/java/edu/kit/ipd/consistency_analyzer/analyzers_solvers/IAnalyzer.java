package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import edu.kit.ipd.consistency_analyzer.datastructures.IWord;

public interface IAnalyzer extends ILoadable {

	void exec(IWord word);

}
