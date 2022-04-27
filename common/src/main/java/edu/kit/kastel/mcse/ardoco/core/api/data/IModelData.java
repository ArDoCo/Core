/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelState;

public interface IModelData extends IData {
    List<String> getModelIds();

    IModelState getModelState(String model);
}
