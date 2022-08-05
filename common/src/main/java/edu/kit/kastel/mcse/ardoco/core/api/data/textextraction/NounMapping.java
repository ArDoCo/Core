/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.textextraction;

import java.util.Collection;
import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;

import edu.kit.kastel.informalin.framework.common.AggregationFunctions;
import edu.kit.kastel.informalin.framework.common.ICopyable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;

/**
 * The Interface INounMapping defines the mapping .
 */
public interface NounMapping extends ICopyable<NounMapping> {

    /**
     * Splits all occurrences with a whitespace in it at their spaces and returns all parts that are similar to the
     * reference. If it contains a separator or similar to the reference it is added to the comparables as a whole.
     *
     * @return all parts of occurrences (splitted at their spaces) that are similar to the reference.
     */
    ImmutableList<String> getRepresentativeComparables();

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
    ImmutableList<Word> getWords();

    /**
     * Adds nodes to the mapping, if they are not already contained.
     *
     * @param nodes graph nodes to add to the mapping
     */
    void addWords(ImmutableList<Word> nodes);

    /**
     * Adds a node to the mapping, it its not already contained.
     *
     * @param word word to add.
     */
    void addWord(Word word);

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
     * Returns the reference words
     *
     * @return the reference words
     */
    ImmutableList<Word> getReferenceWords();

    /**
     * Returns the sentence numbers of occurrences, sorted.
     *
     * @return sentence numbers of the occurrences of this mapping.
     */
    ImmutableList<Integer> getMappingSentenceNo();

    ImmutableSet<Phrase> getPhrases();

    NounMapping splitByPhrase(Phrase phrase);

    /**
     * Adds occurrences to the mapping.
     *
     * @param occurrences occurrences to add
     */
    void addOccurrence(ImmutableList<String> occurrences);

    /**
     * Gets the probability for name.
     *
     * @return the probability for name
     */
    double getProbabilityForKind(MappingKind mappingKind);

    /**
     * Gets the distribution of all mapping kinds.
     *
     * @return the distribution
     */
    Map<MappingKind, Confidence> getDistribution();

    /**
     * @param coreference the coreference to add
     */
    void addCoreference(Word coreference);

    /**
     * @param coreferences the coreferences to add
     */
    void addCoreferences(Collection<Word> coreferences);

    /**
     * @return the coreferences
     */
    ImmutableList<Word> getCoreferences();

    AggregationFunctions getAggregationFunction();

    /**
     * Creates a new INounMapping that resutls when merging the data from the INounMapping with a given other
     * INounMapping
     *
     * @param other the other INounMapping
     * @return new INounMapping that is a merge of the given INounMappings
     */
    NounMapping merge(NounMapping other);

    /**
     * Adds the kind with probability.
     *
     * @param kind        the kind
     * @param claimant    the agent that claims the kind for this nounmapping with a certain probability
     * @param probability the probability
     */
    void addKindWithProbability(MappingKind kind, Claimant claimant, double probability);

    boolean isTheSameAs(NounMapping other);

    boolean containsSameWordsAs(NounMapping nounMapping);

    boolean sharesTextualWordRepresentation(NounMapping nounMapping);

    NounMapping split(ImmutableList<Word> words);

    /**
     * Check whether this noun mapping could have several kinds according to confidence levels.
     * 
     * @param kinds kinds that should be checked
     * @return true if noun mapping could be of all kinds, false if some kind could be excluded.
     */
    boolean couldBeMultipleKinds(MappingKind... kinds);

    /**
     * Checks whether this noun mapping could be of the specified kind
     * 
     * @param kind the kind that should be checked
     * @return true if the specified kind has a probability > 0
     */
    default boolean couldBeOfKind(MappingKind kind) {
        return this.getProbabilityForKind(kind) > 0;
    }

}
