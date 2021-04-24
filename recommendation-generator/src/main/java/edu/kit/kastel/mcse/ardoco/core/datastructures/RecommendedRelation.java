package edu.kit.kastel.mcse.ardoco.core.datastructures;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedRelation;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;

/**
 * This class represents recommended relations. These relations should be
 * contained by the model. The likelihood is measured by the probability.
 *
 * @author Sophie
 *
 */
public class RecommendedRelation implements IRecommendedRelation {

	private Set<IRecommendedInstance> relationInstances;
	private double probability;
	private List<IWord> nodes;
	private String name;

	@Override
	public IRecommendedRelation createCopy() {

		return new RecommendedRelation(relationInstances.stream().map(IRecommendedInstance::createCopy).collect(Collectors.toSet()), probability,
				new ArrayList<>(nodes), name);

	}

	private RecommendedRelation(Set<IRecommendedInstance> relationInstances, double probability, List<IWord> nodes, String name) {
		this.relationInstances = relationInstances;
		this.probability = probability;
		this.nodes = nodes;
		this.name = name;

	}

	/**
	 * Creates a new recommended relation.
	 *
	 * @param name           the name of the relation
	 * @param instance0      the first end point of the relation, as recommended
	 *                       instance
	 * @param instance1      the second end point of the relation, as recommended
	 *                       instance
	 * @param otherInstances possible other instances, that are involved
	 * @param probability    the probability that this is a relation
	 * @param nodes          the nodes representing this relation. These are not the
	 *                       end points of the instances!
	 */
	public RecommendedRelation(String name, IRecommendedInstance instance0, IRecommendedInstance instance1, List<IRecommendedInstance> otherInstances,
			double probability, List<IWord> nodes) {
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
	@Override
	public List<IWord> getNodes() {
		return nodes;
	}

	/**
	 * Adds more occurrences as a list of nodes.
	 *
	 * @param occs list of nodes; every node represents the relation
	 */
	@Override
	public void addOccurrences(List<IWord> occs) {
		for (IWord occ : occs) {
			if (!nodes.contains(occ)) {
				nodes.add(occ);
			}
		}
	}

	/**
	 * updates the probability of the recommended relation.
	 *
	 * @param probability2 the probability to update with
	 */
	@Override
	public void updateProbability(double probability2) {
		if (probability == 1.0) {
			return;
		}
		if (probability2 == 1.0) {
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
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Returns the probability of this relation.
	 *
	 * @return the probability of the relation
	 */
	@Override
	public double getProbability() {
		return probability;
	}

	/**
	 * Sets the probability to the given probability.
	 *
	 * @param probability the new probability
	 */
	@Override
	public void setProbability(double probability) {
		this.probability = probability;
	}

	/**
	 * Returns the end points as instances of this relation.
	 *
	 * @return the involved recommended instances of the relation as list
	 */
	@Override
	public List<IRecommendedInstance> getRelationInstances() {
		return new ArrayList<>(relationInstances);
	}

	private String getInstanceNameTypePairs() {
		StringBuilder result = new StringBuilder("");
		for (IRecommendedInstance ri : relationInstances) {
			result.append(ri.getName()).append(":").append(ri.getType()).append(", ");
		}

		return result.substring(0, result.length() - 2);
	}

	@Override
	public String toString() {

		return "RecommendedRelation [" + " name=" + name + ", recommendedInstances= " + getInstanceNameTypePairs() + ", probability=" + probability + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, relationInstances);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		RecommendedRelation other = (RecommendedRelation) obj;
		return Objects.equals(name, other.name) && Objects.equals(relationInstances, other.relationInstances);
	}

}
