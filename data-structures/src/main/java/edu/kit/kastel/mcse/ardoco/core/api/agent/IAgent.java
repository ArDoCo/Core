/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.agent;

import edu.kit.kastel.mcse.ardoco.core.api.data.IData;
import edu.kit.kastel.mcse.ardoco.core.common.ILoadable;

public interface IAgent<D extends IData> extends ILoadable {
    void execute(D data);

    @Override
    default String getId() {
        return this.getClass().getSimpleName();
    }
}
