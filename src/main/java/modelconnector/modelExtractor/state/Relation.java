package modelconnector.modelExtractor.state;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a relation extracted from a model. A relation must have at least
 * two end points. These end points are defined as instances.
 *
 * @author Sophie
 *
 */
public class Relation {

	private List<Instance> instances;
	private String type;
	private int uid;

	/**
	 * Creates a new relation.
	 *
	 * @param instance1 first instance
	 * @param instance2 second instance
	 * @param type      title of relation
	 * @param uid       unique identifier for trace linking
	 */
	public Relation(Instance instance1, Instance instance2, String type, int uid) {
		instances = new ArrayList<>();
		instances.add(instance1);
		instances.add(instance2);
		this.type = type;
		this.uid = uid;
	}

	/**
	 * Adds more end points to the relation. Checks if the instance is already
	 * contained.
	 *
	 * @param others list of other end points of this relation
	 */
	public void addOtherInstances(List<Instance> others) {
		for (Instance o : others) {
			if (!instances.contains(o)) {
				instances.add(o);
			}
		}
	}

	/**
	 * Returns the end points of this relation as instances.
	 *
	 * @return list of connected instances by this relation
	 */
	public List<Instance> getInstances() {
		return instances;
	}

	/**
	 * Returns the determiner of the relation
	 *
	 * @return the type of relation
	 */
	public String getType() {
		return type;
	}

	/**
	 * Returns the unique identifier of this relation.
	 *
	 * @return the uid of this relation
	 */
	public int getUid() {
		return uid;
	}

	@Override
	public String toString() {

		List<String> instanceNames = this.instances.stream().map(Instance::getLongestName).collect(Collectors.toList());

		return "Relation: [" + " name=" + this.type + ", instances= " + String.join(", ", instanceNames) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((instances == null) ? 0 : instances.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + uid;
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
		Relation other = (Relation) obj;
		if (instances == null) {
			if (other.instances != null) {
				return false;
			}
		} else if (!instances.equals(other.instances)) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		if (uid != other.uid) {
			return false;
		}
		return true;
	}

}
