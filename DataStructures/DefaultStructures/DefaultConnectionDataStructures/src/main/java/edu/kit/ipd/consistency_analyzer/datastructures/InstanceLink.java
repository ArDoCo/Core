package edu.kit.ipd.consistency_analyzer.datastructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a trace link between an instance of the extracted model and a
 * recommended instance.
 *
 * @author Sophie
 *
 */
public class InstanceLink implements IInstanceLink {

	private IRecommendedInstance textualInstance;
	private IInstance modelInstance;
	private double probability;

	@Override
	public IInstanceLink createCopy() {
		return new InstanceLink(textualInstance.createCopy(), modelInstance.createCopy(), probability);
	}

	/**
	 * Creates a new instance link.
	 *
	 * @param textualInstance the recommended instance
	 * @param modelInstance   the extracted instance
	 * @param probability     the probability of this link
	 */
	public InstanceLink(IRecommendedInstance textualInstance, IInstance modelInstance, double probability) {
		this.textualInstance = textualInstance;
		this.modelInstance = modelInstance;
		this.probability = probability;
	}

	/**
	 * Returns the probability of the correctness of this link.
	 *
	 * @return the probability of this link
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
	 * Returns the recommended instance.
	 *
	 * @return the textual instance
	 */
	@Override
	public IRecommendedInstance getTextualInstance() {
		return textualInstance;
	}

	/**
	 * Returns the model instance.
	 *
	 * @return the extracted instance
	 */
	@Override
	public IInstance getModelInstance() {
		return modelInstance;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((modelInstance == null) ? 0 : modelInstance.hashCode());
		result = prime * result + ((textualInstance == null) ? 0 : textualInstance.hashCode());
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
		IInstanceLink other = (IInstanceLink) obj;
		if (modelInstance == null) {
			if (other.getModelInstance() != null) {
				return false;
			}
		} else if (!modelInstance.equals(other.getModelInstance())) {
			return false;
		}
		if (textualInstance == null) {
			if (other.getTextualInstance() != null) {
				return false;
			}
		} else if (!textualInstance.equals(other.getTextualInstance())) {
			return false;
		}
		return true;
	}

	/**
	 * Returns all occurrences of all recommended instance names as string.
	 *
	 * @return all names of the recommended instances
	 */
	@Override
	public String getNameOccurencesAsString() {
		Set<String> names = new HashSet<>();
		List<Integer> namePositions = new ArrayList<>();
		for (INounMapping nameMapping : textualInstance.getNameMappings()) {
			names.addAll(nameMapping.getOccurrences());
			namePositions.addAll(nameMapping.getMappingSentenceNo());
		}

		return "name=" + textualInstance.getName() + "occurrences= " + "NameVariants: " + names.size() + ": " + names.toString() + //
				" sentences{" + Arrays.toString(namePositions.toArray()) + "}";
	}

	@Override
	public String toString() {
		Set<String> names = new HashSet<>();
		List<Integer> namePositions = new ArrayList<>();
		Set<String> types = new HashSet<>();
		List<Integer> typePositions = new ArrayList<>();

		for (INounMapping nameMapping : textualInstance.getNameMappings()) {
			names.addAll(nameMapping.getOccurrences());
			namePositions.addAll(nameMapping.getMappingSentenceNo());
		}
		for (INounMapping typeMapping : textualInstance.getTypeMappings()) {
			types.addAll(typeMapping.getOccurrences());
			typePositions.addAll(typeMapping.getMappingSentenceNo());
		}
		return "InstanceMapping [ uid=" + modelInstance.getUid() + ", name=" + modelInstance.getLongestName() + //
				", as=" + String.join(", ", modelInstance.getLongestType()) + ", probability=" + probability + ", FOUND: " + //
				textualInstance.getName() + " : " + getTextualInstance().getType() + ", occurrences= " + //
				"NameVariants: " + names.size() + ": " + names.toString() + " sentences{" + Arrays.toString(namePositions.toArray()) + "}" + //
				", TypeVariants: " + types.size() + ": " + types.toString() + "sentences{" + Arrays.toString(typePositions.toArray()) + "}" + "]";
	}
}
