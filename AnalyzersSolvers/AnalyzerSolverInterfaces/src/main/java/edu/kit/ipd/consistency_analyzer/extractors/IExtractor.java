package edu.kit.ipd.consistency_analyzer.extractors;

import edu.kit.ipd.consistency_analyzer.agents.ILoadable;
import edu.kit.ipd.consistency_analyzer.datastructures.IWord;

public interface IExtractor extends ILoadable {

	void exec(IWord word);

}
