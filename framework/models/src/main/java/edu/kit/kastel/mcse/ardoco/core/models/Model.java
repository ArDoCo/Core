package edu.kit.kastel.mcse.ardoco.core.models;

import java.util.Set;

public abstract class Model extends ModelElement {

    /**
     * Returns the content of this model.
     *
     * @return the content of this model
     */
    public abstract Set<? extends Entity> getContent();

    /**
     * Returns the endpoints of this model.
     *
     * @return the endpoints of this model
     */
    public abstract Set<? extends Entity> getEndpoints();
}
