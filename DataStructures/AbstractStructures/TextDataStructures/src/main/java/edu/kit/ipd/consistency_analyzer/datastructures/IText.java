package edu.kit.ipd.consistency_analyzer.datastructures;

import java.util.List;

public interface IText {

	public IWord getStartNode();

	public default int getLength() {
		return getNodes().size();
	}

	public List<IWord> getNodes();
}
