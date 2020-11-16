package modelconnector.recommendationGenerator.state;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.kit.ipd.parse.luna.graph.INode;

/**
 * This class represents recommended relations. These relations should be
 * contained by the model. The likelihood is measured by the probability.
 *
 * @author Sophie
 *
 */
public class RecommendedRelation {

	private Set<RecommendedInstance> relationInstances;
	private double probability;
	private List<INode> nodes;
	private String name;

	/**
	 * Creates a new recommended relation.
	 *
	 * @param name           the name of the relation
	 * @param instance0      the first end point of the relation, as recommended
	 *                       instance
	 * @param instance1      the second end point of the relation, as recommended
	 *                       instance
	 * @param otherInstances possible other instances, thate are involved
	 * @param probability    the probability that this is a relation
	 * @param nodes          the nodes representing this relation. These are not the
	 *                       end points of the instances!
	 */
	public RecommendedRelation(String name, RecommendedInstance instance0, RecommendedInstance instance1, List<RecommendedInstance> otherInstances, double probability, List<INode> nodes) {
		relationInstances = new HashSet<>();
		relationInstances.add(instance0);
		relationInstances.add(instance1);
		relationInstances.addAll(otherInstances);
		this.probability = probability;
		this.nodes = new ArrayList<>(nodes);
		this.name = name;
	}

	/**
	 * Returns the nodes representing the relation.
	 *
	 * @return nodes representing the relation
	 */
	public List<INode> getNodes() {
		return nodes;
	}

	/**
	 * Adds more occurrences as a list of nodes.
	 *
	 * @param occs list of nodes; every node represents the relation
	 */
	public void addOccurrences(List<INode> occs) {
		for (INode occ : occs) {
			if (!this.nodes.contains(occ)) {
				this.nodes.add(occ);
			}
		}
	}

	/**
	 * updates the probability of the recommended relation.
	 *
	 * @param probability2 the probability to update with
	 */
	public void updateProbability(double probability2) {
		if (probability == 1.0) {
			return;
		} else if (probability2 == 1.0) {
			probability = 1.0;
		} else if (probability >= probability2) {
			probability += probability2 * (1 - probability);
		} else {
			probability += probability2;
			probability = probability * 0.5;
		}
	}

	/**
	 * Returns the name of this relation.
	 *
	 * @return the name of the relation
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the probability of this relation.
	 *
	 * @return the probability of the relation
	 */
	public double getProbability() {
		return probability;
	}

	/**
	 * Sets the probability to the given probability.
	 *
	 * @param probability the new probability
	 */
	public void setProbability(double probability) {
		this.probability = probability;
	}

	/**
	 * Returns the end points as instances of this relation.
	 *
	 * @return the involved recommended instances of the relation as list
	 */
	public List<RecommendedInstance> getRelationInstances() {
		return new ArrayList<>(relationInstances);
	}

	private String getInstanceNameTypePairs() {
		String result = "";
		for (RecommendedInstance ri : this.relationInstances) {
			result += ri.getName() + ":" + ri.getType() + ", ";
		}

		return result.substring(0, result.length() - 2);
	}

	@Override
	public String toString() {

		return "RecommendedRelation [" + " name=" + name + ", recommendedInstances= " + getInstanceNameTypePairs() + ", probability=" + probability + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((relationInstances == null) ? 0 : relationInstances.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RecommendedRelation other = (RecommendedRelation) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (relationInstances == null) {
			if (other.relationInstances != null) {
				return false;
			}
		} else if (!relationInstances.equals(other.relationInstances)) {
			return false;
		}
		return true;
	}

}
