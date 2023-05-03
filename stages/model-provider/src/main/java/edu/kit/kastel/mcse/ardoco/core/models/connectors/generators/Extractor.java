/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.models.connectors.generators;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;

public abstract class Extractor {
    protected String path;

    protected Extractor(String path) {
        this.path = path;
    }

    public final Model extractModel(String path) {
        this.path = path;
        return extractModel();
    }

    public abstract Model extractModel();

    public abstract String getModelId();
}
