/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.textextraction;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;

/**
 * The Interface INounMapping defines the mapping .
 */
public interface NounMapping {

    /**
     * Returns the surface forms (previously called occurrences) of this mapping.
     *
     * @return all appearances of the mapping
     */
    ImmutableList<String> getSurfaceForms();

    /**
     * Returns all words that are contained by the mapping.
     *
     * @return all words that are referenced with this mapping
     */
    ImmutableSortedSet<Word> getWords();

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
    ImmutableMap<MappingKind, Confidence> getDistribution();

    ImmutableSet<Claimant> getClaimants();

    /**
     * Adds the kind with probability.
     *
     * @param kind        the kind
     * @param claimant    the agent that claims the kind for this nounmapping with a certain probability
     * @param probability the probability
     */
    void addKindWithProbability(MappingKind kind, Claimant claimant, double probability);

    boolean isCompound();

    /**
     * Register a listener that will be notified on certain events.
     * 
     * @param listener the listener
     * @see #onDelete(NounMapping)
     */
    void registerChangeListener(NounMappingChangeListener listener);

    /**
     * Will be invoked during the deletion from a state.
     * Note: This can be invoked multiple times if the replacement is not available during deletion of the noun mapping
     *
     * @param replacement the replacing new noun mapping (or null if none exist)
     */
    void onDelete(NounMapping replacement);
}
