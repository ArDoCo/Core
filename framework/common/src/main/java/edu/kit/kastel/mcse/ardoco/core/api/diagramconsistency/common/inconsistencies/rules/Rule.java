/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.rules;

import java.util.List;

import org.eclipse.collections.api.bimap.MutableBiMap;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram;
import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;

/**
 * Base class for all rules. A rule defines a condition that must be met for diagram and model elements to be considered
 * consistent.
 */
public abstract class Rule {
    private Diagram diagram;
    private Model model;
    private MutableBiMap<Box, Entity> links;
    private Runnable tearDown;

    /**
     * Sets the rule up for a given diagram and model and the links between them.
     *
     * @param diagram
     *                The diagram.
     * @param model
     *                The model.
     * @param links
     *                All found trace links between boxes and entities.
     */
    public final void setup(Diagram diagram, Model model, MutableBiMap<Box, Entity> links) {
        this.diagram = diagram;
        this.model = model;
        this.links = links;
        this.tearDown = this.setup();
    }

    /**
     * Cleans up the rule after it has been run.
     */
    public final void tearDown() {
        this.model = null;
        this.diagram = null;
        this.links = null;
        this.tearDown.run();
    }

    /**
     * Checks the consistency for a given box or entity. If a box or entity is part of a link, both should be passed to
     * the rule. For boxes and entities that are not part of a link, the other parameter should be null.
     *
     * @param box
     *               The box to check, or null if just the entity should be checked.
     * @param entity
     *               The entity to check, or null if just the box should be checked.
     * @return A list of inconsistencies, or an empty list if there are no inconsistencies.
     */
    public abstract List<Inconsistency<Box, Entity>> check(Box box, Entity entity);

    /**
     * This can be overridden to create data used in the check method.
     *
     * @return A runnable that contains all teardown actions.
     */
    protected Runnable setup() {
        return () -> {
        };
    }

    protected final Diagram getDiagram() {
        return this.diagram;
    }

    protected final Model getModel() {
        return this.model;
    }

    protected final MutableBiMap<Box, Entity> getLinks() {
        return this.links;
    }
}
