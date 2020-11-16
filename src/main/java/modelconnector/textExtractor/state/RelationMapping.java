package modelconnector.textExtractor.state;

import java.util.ArrayList;
import java.util.List;

import edu.kit.ipd.parse.luna.graph.INode;

/**
 * This class represents relations found in the graph. A relation mapping has
 * relation mappings, representing the relation ends. It has a probability for
 * being a relation and an assignable preposition.
 *
 * @author Sophie
 *
 */
public class RelationMapping {

	private List<NounMapping> relationNodes;
	private double probability;
	private INode preposition;

	/**
	 * Creates a new relation mapping. Other mappings, as well as a preposition can
	 * be added afterwards.
	 *
	 * @param node1 the first node of the relation
	 * @param node2 another node in the relation
	 * @param prob  probability for being a relation
	 */
	public RelationMapping(NounMapping node1, NounMapping node2, double prob) {
		relationNodes = new ArrayList<>();
		relationNodes.add(node1);
		relationNodes.add(node2);
		probability = prob;
	}

	/**
	 * Adds more nodes to the relation.
	 *
	 * @param nodes more nodes to add.
	 */
	public void addNodesToRelation(List<NounMapping> nodes) {
		for (NounMapping n : nodes) {
			if (!this.relationNodes.contains(n)) {
				relationNodes.add(n);
			}
		}
	}

	/**
	 * Sets the preposition of the node.
	 *
	 * @param prep the graph node representing the preposition
	 */
	public void setPreposition(INode prep) {
		preposition = prep;
	}

	/**
	 * Returns the probability.
	 *
	 * @return probability of being a relation
	 */
	public double getProbability() {
		return probability;
	}

	/**
	 * Returns all relation nodes.
	 *
	 * @return a list of all ends points of the relation
	 */
	public List<NounMapping> getOccurrenceNodes() {
		return relationNodes;
	}

	/**
	 * Returns the preposition of the relation.
	 *
	 * @return preposition of the relation or null if it has not been set.
	 */
	public INode getPreposition() {
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
