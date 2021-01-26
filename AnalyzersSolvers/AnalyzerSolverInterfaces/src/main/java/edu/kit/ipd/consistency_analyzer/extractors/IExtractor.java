package edu.kit.ipd.consistency_analyzer.extractors;

import java.util.List;

import edu.kit.ipd.consistency_analyzer.agents.ILoadable;
import edu.kit.ipd.consistency_analyzer.datastructures.IWord;

public interface IExtractor extends ILoadable {

	void exec(IWord word);

	void setProbability(List<Double> probabilities);

}
