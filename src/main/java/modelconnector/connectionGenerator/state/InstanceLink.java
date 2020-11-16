package modelconnector.connectionGenerator.state;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import modelconnector.modelExtractor.state.Instance;
import modelconnector.recommendationGenerator.state.RecommendedInstance;
import modelconnector.textExtractor.state.NounMapping;

/**
 * Represents a trace link between an instance of the extracted model and a
 * recommended instance.
 *
 * @author Sophie
 *
 */
public class InstanceLink {

	private RecommendedInstance textualInstance;
	private Instance modelInstance;
	private double probability;

	/**
	 * Creates a new instance link.
	 *
	 * @param textualInstance the recommended instance
	 * @param modelInstance   the extracted instance
	 * @param probability     the probability of this link
	 */
	public InstanceLink(RecommendedInstance textualInstance, Instance modelInstance, double probability) {
		this.textualInstance = textualInstance;
		this.modelInstance = modelInstance;
		this.probability = probability;
	}

	/**
	 * Returns the probability of the correctness of this link.
	 *
	 * @return the probability of this link
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
	 * Returns the recommended instance.
	 *
	 * @return the textual instance
	 */
	public RecommendedInstance getTextualInstance() {
		return textualInstance;
	}

	/**
	 * Returns the model instance.
	 *
	 * @return the extracted instance
	 */
	public Instance getModelInstance() {
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
		InstanceLink other = (InstanceLink) obj;
		if (modelInstance == null) {
			if (other.modelInstance != null) {
				return false;
			}
		} else if (!modelInstance.equals(other.modelInstance)) {
			return false;
		}
		if (textualInstance == null) {
			if (other.textualInstance != null) {
				return false;
			}
		} else if (!textualInstance.equals(other.textualInstance)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns all occurrences of all recommended instance names as string.
	 *
	 * @return all names of the recommended instances
	 */
	public String getNameOccurencesAsString() {
		Set<String> names = new HashSet<>();
		List<Integer> namePositions = new ArrayList<>();
		for (NounMapping nameMapping : textualInstance.getNameMappings()) {
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

		for (NounMapping nameMapping : textualInstance.getNameMappings()) {
			names.addAll(nameMapping.getOccurrences());
			namePositions.addAll(nameMapping.getMappingSentenceNo());
		}
		for (NounMapping typeMapping : textualInstance.getTypeMappings()) {
			types.addAll(typeMapping.getOccurrences());
			typePositions.addAll(typeMapping.getMappingSentenceNo());
		}
		return "InstanceMapping [ uid=" + modelInstance.getUid() + ", name=" + modelInstance.getLongestName() + //
				", as=" + String.join(", ", modelInstance.getLongestType()) + ", probability=" + probability + ", FOUND: " + //
				this.textualInstance.getName() + " : " + this.getTextualInstance().getType() + ", occurrences= " + //
				"NameVariants: " + names.size() + ": " + names.toString() + " sentences{" + Arrays.toString(namePositions.toArray()) + "}" + //
				", TypeVariants: " + types.size() + ": " + types.toString() + "sentences{" + Arrays.toString(typePositions.toArray()) + "}" + "]";
	}
}
