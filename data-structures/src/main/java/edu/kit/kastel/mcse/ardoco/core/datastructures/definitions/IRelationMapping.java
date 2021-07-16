package edu.kit.kastel.mcse.ardoco.core.datastructures.definitions;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.datastructures.ICopyable;

/**
 * The Interface IRelationMapping defines a marker in the text that represents a possible relation.
 */
public interface IRelationMapping extends ICopyable<IRelationMapping> {

    /**
     * Adds more NounMappings to the relation.
     *
     * @param mappings more noun mappings to add.
     */
    void addMappingsToRelation(List<? extends INounMapping> mappings);

    /**
     * Sets the preposition of the node.
     *
     * @param prep the graph node representing the preposition
     */
    void setPreposition(IWord prep);

    /**
     * Returns the probability.
     *
     * @return probability of being a relation
     */
    double getProbability();

    /**
     * Returns all relation nodes.
     *
     * @return a list of all ends points of the relation
     */
    List<? extends INounMapping> getOccurrenceNodes();

    /**
     * Returns the preposition of the relation.
     *
     * @return preposition of the relation or null if it has not been set.
     */
    IWord getPreposition();

}
