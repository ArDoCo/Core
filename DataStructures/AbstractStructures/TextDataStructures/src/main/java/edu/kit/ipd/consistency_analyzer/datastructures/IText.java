package edu.kit.ipd.consistency_analyzer.datastructures;

import java.util.List;

public interface IText {

	IWord getStartNode();

	default int getLength() {
		return getWords().size();
	}

	List<IWord> getWords();
}
