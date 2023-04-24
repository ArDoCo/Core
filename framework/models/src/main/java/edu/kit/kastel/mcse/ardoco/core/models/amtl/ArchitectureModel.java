package edu.kit.kastel.mcse.ardoco.core.models.amtl;

import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.models.Model;

/**
 * An architecture model that is an AMTL instance.
 */
public class ArchitectureModel extends Model {

    private Set<? extends ArchitectureItem> content;

    /**
     * Creates a new architecture model that is an AMTL instance. The model has the
     * specified architecture items as content.
     *
     * @param content the content of the architecture model
     */
    public ArchitectureModel(Set<? extends ArchitectureItem> content) {
        this.content = content;
    }

    @Override
    public Set<? extends ArchitectureItem> getContent() {
        return content;
    }

    @Override
    public Set<? extends ArchitectureItem> getEndpoints() {
        return content;
    }
}
