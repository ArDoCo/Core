package edu.kit.kastel.mcse.ardoco.core.models;

/**
 * An entity with a name. Is a model element.
 */
public abstract class Entity extends ModelElement {

    private String name;

    /**
     * Creates a new entity with the specified name.
     *
     * @param name the name of the entity to be created
     */
    public Entity(String name) {
        this.name = name;
    }

    public Entity(String name, String id) {
        super(id);
        this.name = name;
    }

    /**
     * Returns the entity's name.
     *
     * @return the entity's name
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
