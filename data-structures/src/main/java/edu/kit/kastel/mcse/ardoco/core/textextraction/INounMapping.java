/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.Collection;
import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.common.ICopyable;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;

/**
 * The Interface INounMapping defines the mapping .
 */
public interface INounMapping extends ICopyable<INounMapping> {

    /**
     * Splits all occurrences with a whitespace in it at their spaces and returns all parts that are similar to the
     * reference. If it contains a separator or similar to the reference it is added to the comparables as a whole.
     *
     * @return all parts of occurrences (splitted at their spaces) that are similar to the reference.
     */
    ImmutableList<String> getRepresentativeComparables();

    /**
     * Sets the probability of the mapping.
     *
     * @param probability probability to set on
     */
    void hardSetProbability(double probability);

    /**
     * Returns the surface forms (previously called occurrences) of this mapping.
     *
     * @return all appearances of the mapping
     */
    ImmutableList<String> getSurfaceForms();

    /**
     * Returns all words that are contained by the mapping. This should include coreferences.
     *
     * @return all words that are referenced with this mapping
     */
    ImmutableList<IWord> getWords();

    /**
     * Adds nodes to the mapping, if they are not already contained.
     *
     * @param nodes graph nodes to add to the mapping
     */
    void addWords(ImmutableList<IWord> nodes);

    /**
     * Adds a node to the mapping, it its not already contained.
     *
     * @param word word to add.
     */
    void addWord(IWord word);

    /**
     * Removes a word from the mapping.
     *
     * @param word the word to remove
     */
    void removeWord(IWord word);

    /**
     * Returns the probability of being a mapping of its kind.
     *
     * @return probability of being a mapping of its kind.
     */
    double getProbability();

    /**
     * Returns the kind: name, type, name_or_type.
     *
     * @return the kind
     */
    MappingKind getKind();

    /**
     * Returns the reference, the comparable and naming attribute of this mapping.
     *
     * @return the reference
     */
    String getReference();

    /**
     * Changes the kind to another one and recalculates the probability with
     * {@link #recalculateProbability(double, double)}.
     *
     * @param kind        the new kind
     * @param probability the probability of the new mappingTzpe
     */
    void changeMappingType(MappingKind kind, double probability);

    /**
     * Returns the sentence numbers of occurrences, sorted.
     *
     * @return sentence numbers of the occurrences of this mapping.
     */
    ImmutableList<Integer> getMappingSentenceNo();

    /**
     * Updates the reference if the probability is high enough.
     *
     * @param ref         new reference
     * @param probability probability for the new reference.
     */
    void updateReference(String ref, double probability);

    /**
     * Adds occurrences to the mapping.
     *
     * @param occurrences occurrences to add
     */
    void addOccurrence(ImmutableList<String> occurrences);

    /**
     * Copies all nodes and occurrences matching the occurrence to another mapping.
     *
     * @param occurrence     the occurrence to copy
     * @param createdMapping the other mapping
     */
    void copyOccurrencesAndNodesTo(String occurrence, INounMapping createdMapping);

    /**
     * Updates the probability.
     *
     * @param newProbability the probability to update with.
     */
    void updateProbability(double newProbability);

    /**
     * Gets the probability for name.
     *
     * @return the probability for name
     */
    double getProbabilityForName();

    /**
     * Gets the probability for type.
     *
     * @return the probability for type
     */
    double getProbabilityForType();

    /**
     * Gets the probability for nort.
     *
     * @return the probability for nort
     */
    double getProbabilityForNort();

    /**
     * Gets the distribution of all mapping kinds.
     *
     * @return the distribution
     */
    Map<MappingKind, Double> getDistribution();

    /**
     * @param coreference the coreference to add
     */
    void addCoreference(IWord coreference);

    /**
     * @param coreferences the coreferences to add
     */
    void addCoreferences(Collection<IWord> coreferences);

    /**
     * @return the coreferences
     */
    ImmutableList<IWord> getCoreferences();

    /**
     * Creates a new INounMapping that resutls when merging the data from the INounMapping with a given other
     * INounMapping
     *
     * @param other the other INounMapping
     * @return new INounMapping that is a merge of the given INounMappings
     */
    INounMapping merge(INounMapping other);

    /**
     * Adds the kind with probability.
     *
     * @param kind        the kind
     * @param probability the probability
     */
    void addKindWithProbability(MappingKind kind, double probability);

    /**
     * @return if this is a phrase or contains a phrase
     */
    boolean isPhrase();

    void setAsPhrase(boolean hasPhrase);
}
