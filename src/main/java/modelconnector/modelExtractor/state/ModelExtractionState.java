package modelconnector.modelExtractor.state;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import modelconnector.helpers.ModelConnectorConfiguration;

/**
 * This state contains all from the model extracted information. This are the
 * extracted instances and relations. For easier handling, the occurring types
 * and names are stored additionally.
 *
 * @author Sophie
 *
 */
public class ModelExtractionState {

	private Set<String> instanceTypes;
	private Set<String> relationTypes;
	private Set<String> names;
	private List<Instance> instances;
	private List<Relation> relations;

	/**
	 * Creates a new model extraction state.
	 *
	 * @param instances instances of this model extraction state
	 * @param relations relations of this model extraction state
	 */
	public ModelExtractionState(List<Instance> instances, List<Relation> relations) {
		this.instances = instances;
		this.relations = relations;
		instanceTypes = new HashSet<>();
		relationTypes = new HashSet<>();
		names = new HashSet<>();
		this.collectTypesAndNames();
	}

	/**
	 * Collects all occurring types and names from the instances and relations and
	 * stores them. The titles of relations are stored in types.
	 */
	private void collectTypesAndNames() {
		for (Relation r : relations) {
			relationTypes.add(r.getType());
			List<String> typeParts = List.of(r.getType().split(" "));
			if (typeParts.size() >= ModelConnectorConfiguration.EXTRACTION_STATE_MIN_TYPE_PARTS) {
				relationTypes.addAll(typeParts);
			}
		}
		for (Instance i : instances) {
			instanceTypes.addAll(i.getTypes());
			names.addAll(i.getNames());
		}
	}

	/**
	 * Returns the instances of a specific type.
	 *
	 * @param type the type to search for
	 * @return all instances that are from that type
	 */
	public List<Instance> getInstancesOfType(String type) {
		return this.instances.stream().filter(i -> i.getTypes().contains(type)).collect(Collectors.toList());
	}

	/**
	 * Returns the relations of a specific type.
	 *
	 * @param type the type to search for
	 * @return all relations that are from that type
	 */
	public List<Relation> getRelationsOfType(String type) {
		return this.relations.stream().filter(r -> r.getType().equals(type)).collect(Collectors.toList());
	}

	/**
	 * Returns all types that are contained by instances of this state.
	 *
	 * @return all instance types of this state
	 */
	public Set<String> getInstanceTypes() {
		return instanceTypes;
	}

	/**
	 * Returns all types that are contained by relations of this state.
	 *
	 * @return all relation types of this state
	 */
	public Set<String> getRelationTypes() {
		return relationTypes;
	}

	/**
	 * Returns all names that are contained by this state.
	 *
	 * @return all names of this state
	 */
	public Set<String> getNames() {
		return names;
	}

	/**
	 * Returns all instances that are contained by this state.
	 *
	 * @return all instances of this state
	 */
	public List<Instance> getInstances() {
		return instances;
	}

	/**
	 * Returns all relations that are contained by this state.
	 *
	 * @return all relations of this state
	 */
	public List<Relation> getRelations() {
		return relations;
	}

	@Override
	public String toString() {
		String output = "Instances:\n";
		for (Instance i : instances) {
			output += i.toString() + "\n";
		}
		output += "Relations:\n";
		for (Relation r : relations) {
			output += r.toString() + "\n";
		}
		return output;
	}

}
