package edu.kit.kastel.mcse.ardoco.core.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

/**
 * General helper class for outsourced, common methods.
 *
 * @author Sophie
 *
 */
public final class Utilis {

    private Utilis() {
        throw new IllegalAccessError();
    }

    /**
     * Creates a cartesian product out of the current list and the parts.
     *
     * @param <T>         generic type of list elements
     * @param currentList the list to start with
     * @param parts       the list of lists with possibilities to add.
     * @return list of different combinations
     */
    public static <T> ImmutableList<ImmutableList<T>> cartesianProduct(ImmutableList<T> currentList, ImmutableList<ImmutableList<T>> parts) {
        List<T> cl = currentList.toList();
        List<List<T>> pl = parts.collect(l -> (List<T>) l.toList()).toList();

        return Lists.immutable.fromStream(privateCartesianProduct(cl, pl).stream()).collect(l -> Lists.immutable.withAll(l));
    }

    private static <T> List<List<T>> privateCartesianProduct(List<T> currentList, List<List<T>> parts) {
        List<List<T>> result = new ArrayList<>();

        if (parts.isEmpty()) {

            result.add(currentList);
            return result;
        }

        List<List<T>> cloneParts = new ArrayList<>(parts);
        cloneParts.remove(parts.get(0));

        for (T si : parts.get(0)) {
            currentList.add(si);
            result.addAll(privateCartesianProduct(new ArrayList<>(currentList), cloneParts));
            currentList.remove(si);
        }
        return result;
    }

}
