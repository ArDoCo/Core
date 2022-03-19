/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.agent;

import edu.kit.kastel.mcse.ardoco.core.api.data.IData;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.common.ILoadable;

/**
 * The Interface IExtractor defines executable units that run at the beginning of each stage. They operate on word
 * level.
 */
public interface IExtractor<D extends IData> extends ILoadable {

    /**
     * Execute the extractor and apply it on a word.
     *
     * @param word the word
     */
    void exec(D data, IWord word);

    default String getId() {
        return this.getClass().getSimpleName();
    }
}
