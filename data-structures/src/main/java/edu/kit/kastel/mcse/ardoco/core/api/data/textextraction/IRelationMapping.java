/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.textextraction;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.common.ICopyable;

/**
 * The Interface IRelationMapping defines a marker in the text that represents a possible relation.
 */
public interface IRelationMapping extends ICopyable<IRelationMapping> {

    /**
     * Adds more NounMappings to the relation.
     *
     * @param mappings more noun mappings to add.
     */
    void addMappingsToRelation(ImmutableList<INounMapping> mappings);

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
    ImmutableList<INounMapping> getOccurrenceNodes();

    /**
     * Returns the preposition of the relation.
     *
     * @return preposition of the relation or null if it has not been set.
     */
    IWord getPreposition();

}
