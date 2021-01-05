package edu.kit.ipd.consistency_analyzer.datastructures;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents relations found in the graph. A relation mapping has
 * relation mappings, representing the relation ends. It has a probability for
 * being a relation and an assignable preposition.
 *
 * @author Sophie
 *
 */
public class RelationMapping implements IRelationMapping {

	private List<INounMapping> relationNodes;
	private double probability;
	private IWord preposition;

	@Override
	public IRelationMapping createCopy() {
		return new RelationMapping(relationNodes.stream().map(INounMapping::createCopy).collect(Collectors.toList()), probability, preposition);

	}

	private RelationMapping(List<INounMapping> relationNodes, double probability, IWord preposition) {

		this.relationNodes = relationNodes;
		this.preposition = preposition;
		this.probability = probability;

	}

	/**
	 * Creates a new relation mapping. Other mappings, as well as a preposition can
	 * be added afterwards.
	 *
	 * @param node1 the first node of the relation
	 * @param node2 another node in the relation
	 * @param prob  probability for being a relation
	 */
	public RelationMapping(INounMapping node1, INounMapping node2, double prob) {
		relationNodes = new ArrayList<>();
		relationNodes.add(node1);
		relationNodes.add(node2);
		probability = prob;
	}

	/**
	 * Adds more NounMappings to the relation.
	 *
	 * @param mappings more noun mappings to add.
	 */
	@Override
	public void addMappingsToRelation(List<? extends INounMapping> mappings) {
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
	public List<? extends INounMapping> getOccurrenceNodes() {
		return relationNodes;
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
	/**
	 * Prints the relation mapping. Contains relation nodes, probability and
	 * preposition.
	 */
	public String toString() {
		return "RelationNode [relationNodes=" + relationNodes + ", probability=" + probability + ", preposition=" + preposition + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((relationNodes == null) ? 0 : relationNodes.hashCode());
		return result;
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
		if (relationNodes == null) {
			if (other.relationNodes != null) {
				return false;
			}
		} else if (!relationNodes.equals(other.relationNodes)) {
			return false;
		}
		return true;
	}

}
