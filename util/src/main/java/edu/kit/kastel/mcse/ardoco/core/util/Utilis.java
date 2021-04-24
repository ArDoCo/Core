package edu.kit.kastel.mcse.ardoco.core.util;

import java.util.ArrayList;
import java.util.List;

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
    public static <T> List<List<T>> cartesianProduct(List<T> currentList, List<List<T>> parts) {

        List<List<T>> result = new ArrayList<>();

        if (parts.isEmpty()) {

            result.add(currentList);
            return result;
        }

        List<List<T>> cloneParts = new ArrayList<>(parts);
        cloneParts.remove(parts.get(0));

        for (T si : parts.get(0)) {
            currentList.add(si);
            result.addAll(cartesianProduct(new ArrayList<>(currentList), cloneParts));
            currentList.remove(si);
        }
        return result;
    }

}
