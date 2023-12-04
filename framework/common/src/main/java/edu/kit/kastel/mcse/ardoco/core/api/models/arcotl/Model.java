/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelElement;

public abstract sealed class Model extends ModelElement permits ArchitectureModel, CodeModel {

    /**
     * Returns the content of this model.
     *
     * @return the content of this model
     */
    public abstract List<? extends Entity> getContent();

    /**
     * Returns the endpoints of this model.
     *
     * @return the endpoints of this model
     */
    public abstract List<? extends Entity> getEndpoints();
}
