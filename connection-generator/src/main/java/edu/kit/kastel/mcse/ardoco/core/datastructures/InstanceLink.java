package edu.kit.kastel.mcse.ardoco.core.datastructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstanceLink;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedInstance;

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
		return Objects.hash(modelInstance, textualInstance);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		InstanceLink other = (InstanceLink) obj;
		return Objects.equals(modelInstance, other.modelInstance) && Objects.equals(textualInstance, other.textualInstance);
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
