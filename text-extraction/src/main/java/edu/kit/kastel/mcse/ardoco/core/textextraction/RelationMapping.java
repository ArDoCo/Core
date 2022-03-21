/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.IRelationMapping;

/**
 * This class represents relations found in the graph. A relation mapping has relation mappings, representing the
 * relation ends. It has a probability for being a relation and an assignable preposition.
 *
 * @author Sophie
 */
public class RelationMapping implements IRelationMapping {

    private final MutableList<INounMapping> relationNodes;
    private final double probability;
    private IWord preposition;

    @Override
    public IRelationMapping createCopy() {
        return new RelationMapping(relationNodes.collect(INounMapping::createCopy).toImmutable(), probability, preposition);
    }

    private RelationMapping(ImmutableList<INounMapping> relationNodes, double probability, IWord preposition) {
        this.relationNodes = relationNodes.toList();
        this.preposition = preposition;
        this.probability = probability;
    }

    /**
     * Creates a new relation mapping. Other mappings, as well as a preposition can be added afterwards.
     *
     * @param node1 the first node of the relation
     * @param node2 another node in the relation
     * @param prob  probability for being a relation
     */
    public RelationMapping(INounMapping node1, INounMapping node2, double prob) {
        relationNodes = Lists.mutable.with(node1, node2);
        probability = prob;
    }

    /**
     * Adds more NounMappings to the relation.
     *
     * @param mappings more noun mappings to add.
     */
    @Override
    public void addMappingsToRelation(ImmutableList<INounMapping> mappings) {
        for (INounMapping n : mappings) {
            if (!relationNodes.contains(n)) {
                relationNodes.add(n);
            }
        }
    }

    /**
     * Sets the preposition of the node.
     *
     * @param prep the graph node representing the preposition
     */
    @Override
    public void setPreposition(IWord prep) {
        preposition = prep;
    }

    /**
     * Returns the probability.
     *
     * @return probability of being a relation
     */
    @Override
    public double getProbability() {
        return probability;
    }

    /**
     * Returns all relation nodes.
     *
     * @return a list of all ends points of the relation
     */
    @Override
    public ImmutableList<INounMapping> getOccurrenceNodes() {
        return relationNodes.toImmutable();
    }

    /**
     * Returns the preposition of the relation.
     *
     * @return preposition of the relation or null if it has not been set.
     */
    @Override
    public IWord getPreposition() {
        return preposition;
    }

    @Override
    public String toString() {
        return "RelationNode [relationNodes=" + relationNodes + ", probability=" + probability + ", preposition=" + preposition + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(relationNodes);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        RelationMapping other = (RelationMapping) obj;
        return Objects.equals(relationNodes, other.relationNodes);
    }

}
