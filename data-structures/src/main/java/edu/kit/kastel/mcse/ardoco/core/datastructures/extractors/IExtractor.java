package edu.kit.kastel.mcse.ardoco.core.datastructures.extractors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.ILoadable;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;

/**
 * The Interface IExtractor defines executable units that run at the beginning of each stage. They operate on word
 * level.
 */
public interface IExtractor extends ILoadable {

    /**
     * Execute the extractor and apply it on a word.
     *
     * @param word the word
     */
    void exec(IWord word);

}
