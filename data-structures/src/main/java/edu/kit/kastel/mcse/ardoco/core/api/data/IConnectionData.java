/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data;

import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.IConnectionState;

public interface IConnectionData extends IData {
    void setConnectionState(String model, IConnectionState state);

    IConnectionState getConnectionState(String model);
}
