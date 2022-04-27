/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.agent;

import edu.kit.kastel.mcse.ardoco.core.api.data.IData;

public interface IFilter<D extends IData> {

    /**
     * Execute the filter and apply it on a data object.
     *
     * @param data the data to operate on
     */
    void exec(D data);

    default String getId() {
        return this.getClass().getSimpleName();
    }
}
