/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;

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

        return Lists.immutable.fromStream(privateCartesianProduct(cl, pl).stream()).collect(Lists.immutable::withAll);
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
        var ref = reference;
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
        // TODO can we improve detection of similar types
        var identifiers = getTypeIdentifiers(modelState);
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
        var joiner = new StringJoiner(" ");
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
        var joiner = new StringJoiner(" ");
        for (String namePart : name.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
            joiner.add(namePart);
        }
        return joiner.toString().replaceAll("\\s+", " ");
    }

    /**
     * Splits a given String using {@link #splitCamelCase(String)} and {@link #splitSnakeAndKebabCase(String)}
     *
     * @param name the given name
     * @return the split name
     */
    public static String splitCases(String name) {
        return splitCamelCase(splitSnakeAndKebabCase(name));
    }

    /**
     * Checks if the given name is a CamelCased-word
     *
     * @param name the name to check
     * @return <code>true</code> if the given name is CamelCased
     */
    public static boolean nameIsCamelCased(String name) {
        var unCamelCased = CommonUtilities.splitCamelCase(name);
        return name.length() < unCamelCased.length();
    }

    /**
     * Checks if the given name is a snake_cased-word
     *
     * @param name the name to check
     * @return <code>true</code> if the given name is snake_cased
     */
    public static boolean nameIsSnakeCased(String name) {
        var split = name.split("_");
        return split.length > 1;
    }

    /**
     * Checks if the given name is a kebab-cased-word
     *
     * @param name the name to check
     * @return <code>true</code> if the given name is kebab-cased
     */
    public static boolean nameIsKebabCased(String name) {
        var split = name.split("-");
        return split.length > 1;
    }

    /**
     * Calculates the probability given the current probability and the update value
     *
     * @param currentProbability current probability
     * @param newProbability     update value
     * @return the new probability
     */
    public static double calcNewProbabilityValue(double currentProbability, double newProbability) {
        if (valueEqual(currentProbability, 1.0) || valueEqual(newProbability, 1.0)) {
            return 1.0;
        }
        if (currentProbability >= newProbability) {
            return currentProbability + newProbability * (1 - currentProbability);
        } else {
            return (currentProbability + newProbability) * 0.5;
        }
    }

    /**
     * Creates a reference given a list of words (phrase)
     *
     * @param phrase the given phrase
     * @return a reference that consists of the words in the given phrase
     */
    public static String createReferenceForPhrase(ImmutableList<IWord> phrase) {
        var sortedPhrase = phrase.toSortedListBy(IWord::getPosition);
        var referenceJoiner = new StringJoiner(" ");
        for (var w : sortedPhrase) {
            referenceJoiner.add(w.getText());
        }
        return referenceJoiner.toString();
    }

    public static ImmutableList<IWord> getCompoundPhrase(IWord word) {
        var deps = Lists.mutable.of(word);
        deps.addAll(word.getWordsThatAreDependencyOfThis(DependencyTag.COMPOUND).toList());
        var sortedWords = deps.toSortedListBy(IWord::getPosition);
        if (deps.size() < 2) {
            return Lists.immutable.empty();
        }
        return Lists.immutable.ofAll(sortedWords);
    }

    public static ImmutableList<IWord> filterWordsOfTypeMappings(ImmutableList<IWord> words, ITextState textState) {
        MutableList<IWord> filteredWords = Lists.mutable.empty();
        for (var word : words) {
            if (!textState.isWordContainedByTypeMapping(word)) {
                filteredWords.add(word);
            }
        }
        return filteredWords.toImmutable();
    }

    /**
     * Check if the word is CamelCased. Additionally, the word needs to have a length > 4, otherwise it is probably only
     * a abbreviation.
     *
     * @param word Word to check
     * @return <code>true</code> if the word is CamelCased and has a length greater than 4
     */
    public static boolean isCamelCasedWord(String word) {
        if (word.toUpperCase().equals(word)) {
            return false;
        }
        return word.length() > 4 && nameIsCamelCased(word);
    }

}
