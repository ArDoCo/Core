package edu.kit.kastel.mcse.ardoco.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;

/**
 * General helper class for outsourced, common methods.
 *
 * @author Sophie
 *
 */
public final class CommonUtilities {

    private CommonUtilities() {
        throw new IllegalAccessError();
    }

    /**
     * Compare two double values.
     *
     * @param d1 value 1
     * @param d2 value 2
     * @return <code>true</code> iff similar enough to be equal
     */
    public static boolean valueEqual(double d1, double d2) {
        return Math.abs(d1 - d2) < 1E-8;
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

    /**
     * Replaces all defined separators in a given string with a whitespace and returns the resulting string.
     *
     * @param reference given string
     * @return reference with whitespaces instead of separators
     */
    public static ImmutableList<String> splitAtSeparators(String reference) {
        String ref = reference;
        for (String sep : CommonTextToolsConfig.SEPARATORS_TO_SPLIT) {
            ref = ref.replaceAll(sep, " ");
        }
        return Lists.immutable.withAll(Lists.immutable.with(ref.split(" ")));
    }

    /**
     * Checks if a string contains any separators.
     *
     * @param reference string to check
     * @return true, if a separator is contained or false, if not
     */
    public static boolean containsSeparator(String reference) {
        for (String sep : CommonTextToolsConfig.SEPARATORS_TO_CONTAIN) {
            if (reference.contains(sep) && !reference.contentEquals(sep)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates {@link IRecommendedInstance}s for the given {@link INounMapping}s using the given information about
     * similar types and probability and type mappings. Adds the created {@link IRecommendedInstance}s to the given
     * {@link IRecommendationState}
     *
     * @param similarTypes        The list of similar types
     * @param nameMappings        the noun mappings
     * @param typeMappings        the type mappings
     * @param recommendationState the state the new RecommendedInstances should be added to
     * @param probability         the probability that should be annotated
     */
    public static void addRecommendedInstancesFromNounMappings(ImmutableList<String> similarTypes, ImmutableList<INounMapping> nameMappings,
            ImmutableList<INounMapping> typeMappings, IRecommendationState recommendationState, double probability) {
        for (var nameMapping : nameMappings) {
            var name = nameMapping.getReference();
            for (var type : similarTypes) {
                recommendationState.addRecommendedInstance(name, type, probability, nameMappings, typeMappings);
            }
        }
    }

    /**
     * Retrieves a list of similar types in the given model state given the word.
     *
     * @param word       the word that might have type names in the model state
     * @param modelState the model state containing information about types
     * @return List of type names in the model state that are similar to the given word
     */
    public static ImmutableList<String> getSimilarTypes(IWord word, IModelState modelState) {
        Set<String> identifiers = getTypeIdentifiers(modelState);
        return Lists.immutable.fromStream(identifiers.stream().filter(typeId -> SimilarityUtils.areWordsSimilar(typeId, word.getText())));
    }

    /**
     * Returns a set of identifiers for the types in the model state.
     *
     * @param modelState the model state
     * @return Set of identifiers for existing types
     */
    public static Set<String> getTypeIdentifiers(IModelState modelState) {
        Set<String> identifiers = modelState.getInstanceTypes()
                .stream()
                .map(CommonUtilities::splitSnakeAndKebabCase)
                .map(CommonUtilities::splitCamelCase)
                .map(type -> type.split(" "))
                .flatMap(Arrays::stream)
                .collect(Collectors.toSet());
        identifiers.addAll(modelState.getInstanceTypes());
        return identifiers;
    }

    /**
     * Splits a given String at Snake and Kebab cases. For example, "test-string" and "test_string" become "test
     * string".
     *
     * @param name the given name
     * @return the name split at snake and kebab case
     */
    public static String splitSnakeAndKebabCase(String name) {
        StringJoiner joiner = new StringJoiner(" ");
        for (String namePart : name.split("[-_]")) {
            joiner.add(namePart);
        }
        return joiner.toString().replaceAll("\\s+", " ");
    }

    /**
     * Splits a given String at camel cases. For example, "testString" becomes "test String".
     *
     * @param name the given name
     * @return the name split at camel case
     */
    public static String splitCamelCase(String name) {
        StringJoiner joiner = new StringJoiner(" ");
        for (String namePart : name.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
            joiner.add(namePart);
        }
        return joiner.toString().replaceAll("\\s+", " ");
    }

}
