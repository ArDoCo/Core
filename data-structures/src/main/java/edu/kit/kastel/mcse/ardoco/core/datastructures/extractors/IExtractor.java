package edu.kit.kastel.mcse.ardoco.core.datastructures.extractors;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.ILoadable;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;

public interface IExtractor extends ILoadable {

	void exec(IWord word);

	void setProbability(List<Double> probabilities);

}
