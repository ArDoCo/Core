package edu.kit.kastel.mcse.ardoco.core.datastructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstance;

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
	 * @param name name of the instance.
	 * @param type type of the instance.
	 * @param uid  unique identifier of the instance needed for trace linking.
	 */
	public Instance(String name, String type, String uid) {

		String splitName = splitSnakeAndKebabCase(name);
		splitName = splitCamelCase(splitName);
		names = Arrays.stream(splitName.split(" ")).collect(Collectors.toList());
		if (names.size() > 1) {
			names.add(name);
		}

		String splitType = splitCamelCase(type);
		types = Arrays.stream(splitType.split(" ")).collect(Collectors.toList());
		if (types.size() > 1) {
			types.add(type);
		}
		this.uid = uid;
		longestName = name;
		longestType = type;
	}

	private static String splitCamelCase(String name) {
		StringJoiner joiner = new StringJoiner(" ");
		for (String namePart : name.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
			joiner.add(namePart);
		}
		return joiner.toString().replaceAll("\\s+", " "); // also remove extra spaces between words
	}

	private static String splitSnakeAndKebabCase(String name) {
		StringJoiner joiner = new StringJoiner(" ");
		for (String namePart : name.split("[-_]")) {
			joiner.add(namePart);
		}
		return joiner.toString().replaceAll("\\s+", " "); // also remove extra spaces between words
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
		return Objects.hash(longestName, longestType, uid);
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
		return Objects.equals(longestName, other.longestName) && Objects.equals(longestType, other.longestType) && Objects.equals(uid, other.uid);
	}

}
