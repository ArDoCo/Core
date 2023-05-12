/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;

/**
 * An architecture model that is an AMTL instance.
 */
public class ArchitectureModel extends Model {

    private List<? extends ArchitectureItem> content;

    /**
     * Creates a new architecture model that is an AMTL instance. The model has the
     * specified architecture items as content.
     *
     * @param content the content of the architecture model
     */
    public ArchitectureModel(List<? extends ArchitectureItem> content) {
        this.content = content;
    }

    @Override
    public List<? extends ArchitectureItem> getContent() {
        return content;
    }

    @Override
    public List<? extends ArchitectureItem> getEndpoints() {
        return content;
    }
}
