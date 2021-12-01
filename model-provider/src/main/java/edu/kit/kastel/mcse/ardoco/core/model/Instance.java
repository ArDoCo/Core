/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.model;

import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;

/**
 * This class represents an instance extracted from a model. The name of an instance (as well as the type) are splitted
 * at spaces and can be seen as multiple names. Therefore, the longestName (and type) is the original name (type) of the
 * instance.
 *
 * @author Sophie
 *
 */
public class Instance implements IModelInstance {

    private String longestName;
    private String longestType;
    private MutableList<String> names;
    private MutableList<String> types;
    private String uid;

    @Override
    public IModelInstance createCopy() {
        return new Instance(longestName, longestType, Lists.immutable.withAll(names), Lists.immutable.withAll(types), uid);

    }

    private Instance(String longestName, String longestType, ImmutableList<String> names, ImmutableList<String> types, String uid) {
        this.longestName = longestName;
        this.longestType = longestType;
        this.names = names.toList();
        this.types = types.toList();
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

        String splitName = CommonUtilities.splitCases(name);
        names = Lists.mutable.with(splitName.split(" "));
        if (names.size() > 1) {
            names.add(name);
        }

        String splitType = CommonUtilities.splitCases(type);
        types = Lists.mutable.with(splitType.split(" "));
        if (types.size() > 1) {
            types.add(type);
        }
        this.uid = uid;
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
    public ImmutableList<String> getNames() {
        return names.toImmutable();
    }

    /**
     * Returns all type parts of the instance.
     *
     * @return all type parts of the instance as list
     */
    @Override
    public ImmutableList<String> getTypes() {
        return types.toImmutable();
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
