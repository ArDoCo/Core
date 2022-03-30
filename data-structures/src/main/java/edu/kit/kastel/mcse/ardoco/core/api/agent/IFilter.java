package edu.kit.kastel.mcse.ardoco.core.api.agent;

import edu.kit.kastel.mcse.ardoco.core.api.data.IData;

public interface IFilter<D extends IData> {

    /**
     * Execute the extractor and apply it on a word.
     *
     * @param word the word
     */
    void exec(D data);

    default String getId() {
        return this.getClass().getSimpleName();
    }
}
