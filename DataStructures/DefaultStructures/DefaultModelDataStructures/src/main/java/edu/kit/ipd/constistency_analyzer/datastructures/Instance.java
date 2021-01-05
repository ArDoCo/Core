package edu.kit.ipd.constistency_analyzer.datastructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import edu.kit.ipd.consistency_analyzer.datastructures.IInstance;

/**
 * This class represents an instance extracted from a model. The name of an
 * instance (as well as the type) are splitted at spaces and can be seen as
 * multiple names. Therefore, the longestName (and type) is the original name
 * (type) of the instance.
 *
 * @author Sophie
 *
 */
public class Instance implements IInstance {

	private String longestName;
	private String longestType;
	private List<String> names;
	private List<String> types;
	private String uid;

	@Override
	public IInstance createCopy() {
		return new Instance(longestName, longestType, new ArrayList<>(names), new ArrayList<>(types), uid);

	}

	private Instance(String longestName, String longestType, List<String> names, List<String> types, String uid) {
		this.longestName = longestName;
		this.longestType = longestType;
		this.names = names;
		this.types = types;
		this.uid = uid;
	}

	/**
	 * Creates a new instance.
	 *
	 * @param name   name of the instance.
	 * @param type   type of the instance.
	 * @param string unique identifier of the instance needed for trace linking.
	 */
	public Instance(String name, String type, String string) {

		names = Arrays.stream(name.split(" ")).collect(Collectors.toList());
		if (names.size() != 1) {
			names.add(name);
		}
		types = Arrays.stream(type.split(" ")).collect(Collectors.toList());
		if (types.size() != 1) {
			types.add(type);
		}
		uid = string;
		longestName = name;
		longestType = type;
	}

	/**
	 * Returns the longest name of the instance.
	 *
	 * @return the original name of the instance
	 */
	@Override
	public String getLongestName() {
		return longestName;
	}

	/**
	 * Returns the longest type of the instance.
	 *
	 * @return the original type of the instance
	 */
	@Override
	public String getLongestType() {
		return longestType;
	}

	/**
	 * Returns all name parts of the instance.
	 *
	 * @return all name parts of the instance as list
	 */
	@Override
	public List<String> getNames() {
		return names;
	}

	/**
	 * Returns all type parts of the instance.
	 *
	 * @return all type parts of the instance as list
	 */
	@Override
	public List<String> getTypes() {
		return types;
	}

	/**
	 * Returns the unique identifier of the instance.
	 *
	 * @return the unique identifier of the instance
	 */
	@Override
	public String getUid() {
		return uid;
	}

	@Override
	public String toString() {
		return "Instance [names=" + String.join(", ", names) + ", type=" + String.join(", ", types) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((longestName == null) ? 0 : longestName.hashCode());
		result = prime * result + ((longestType == null) ? 0 : longestType.hashCode());
		result = prime * result + uid.hashCode();
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
		Instance other = (Instance) obj;
		if (longestName == null) {
			if (other.longestName != null) {
				return false;
			}
		} else if (!longestName.equals(other.longestName)) {
			return false;
		}
		if (longestType == null) {
			if (other.longestType != null) {
				return false;
			}
		} else if (!longestType.equals(other.longestType)) {
			return false;
		}
		return (uid == other.uid);
	}

}
