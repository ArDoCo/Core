/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.textextraction;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.informalin.framework.common.ICopyable;
import edu.kit.kastel.informalin.framework.configuration.IConfigurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.IClaimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IPhrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.PhraseType;

/**
 * The Interface ITextState.
 */
public interface ITextState extends ICopyable<ITextState>, IConfigurable {

    /**
     * Minimum difference that need to shall not be reached to identify a NounMapping as NameOrType.
     * 
     * @see #getMappingsThatCouldBeOfKind(IWord, MappingKind)
     * @see #isWordContainedByMappingKind(IWord, MappingKind)
     */
    double MAPPINGKIND_MAX_DIFF = 0.1;

    /**
     * * Adds a name mapping to the state.
     *
     * @param word        word of the mapping
     * @param kind        the kind of the mapping
     * @param probability probability to be a name mapping
     */
    void addNounMapping(IWord word, MappingKind kind, IClaimant claimant, double probability);

    /**
     * * Adds a name mapping to the state.
     *
     * @param word        word of the mapping
     * @param kind        the kind of the mapping
     * @param probability probability to be a name mapping
     * @param occurrences list of the appearances of the mapping
     */
    void addNounMapping(IWord word, MappingKind kind, IClaimant claimant, double probability, ImmutableList<String> occurrences);

    // --- remove section --->

    /**
     * Removes a noun mapping from the state.
     *
     * @param word noun mapping to remove
     */
    void removeNounMapping(INounMapping word);

    /**
     * Returns all mappings containing the given word.
     *
     * @param word the given word
     * @return all mappings containing the given word as list
     */
    ImmutableList<INounMapping> getNounMappingsByWord(IWord word);

    ImmutableList<IPhraseMapping> getPhraseMappingsByNounMapping(INounMapping nounMapping);

    ImmutableList<IPhraseMapping> getPhraseMappingsByPhrase(IPhrase phrase);

    /**
     * Returns a list of all references of noun mappings.
     * 
     * @param kind of references that shall be collected
     * @return all references of noun mappings with the specified kind as list.
     */
    ImmutableList<String> getListOfReferences(MappingKind kind);

    /**
     * Returns all type mappings containing the given word.
     *
     * @param word word to filter for
     * @return a list of all type mappings containing the given word
     */
    ImmutableList<INounMapping> getNounMappingsByWordAndKind(IWord word, MappingKind kind);

    ImmutableList<IPhraseMapping> getPhraseMappingsByPhraseType(PhraseType phraseType);

    /**
     * Returns if a word is contained by the name mappings.
     *
     * @param word        word to check
     * @param mappingKind mappingKind to check
     * @return true if the word is contained by name mappings.
     */
    boolean isWordContainedByMappingKind(IWord word, MappingKind mappingKind);

    /**
     * Returns if a word is contained by the mappings.
     *
     * @param word word to check
     * @return true if the word is contained by mappings.
     */
    boolean isWordContainedByNounMappings(IWord word);

    /**
     * Gets the all noun mappings.
     *
     * @return the all mappings
     */
    ImmutableList<INounMapping> getNounMappings();

    ImmutableList<IPhraseMapping> getPhraseMappings();

    /**
     * Adds the noun mapping.
     *
     * @param nounMapping the noun mapping.
     */
    void addNounMapping(INounMapping nounMapping, IClaimant claimant);

    /**
     * Gets the mappings that could be a type.
     *
     * @param word        the word
     * @param mappingKind the mapping kind that
     * @return the mappings that could be a type
     */
    default ImmutableList<INounMapping> getMappingsThatCouldBeOfKind(IWord word, MappingKind mappingKind) {
        return getNounMappingsByWord(word).select(mapping -> mapping.getProbabilityForKind(mappingKind) > 0);
    }

    /**
     * Returns all mappings with a similar reference as given.
     *
     * @param ref the reference to search for
     * @return a list of noun mappings with the given reference.
     */
    ImmutableList<INounMapping> getNounMappingsWithSimilarReference(String ref);

    /**
     * Gets the mappings that could be a Name or Type.
     *
     * @param word  the word
     * @param kinds the required mappingKindso
     * @return the mappings that could be a Name or Type
     */
    default ImmutableList<INounMapping> getMappingsThatCouldBeMultipleKinds(IWord word, MappingKind... kinds) {
        if (kinds.length == 0) {
            throw new IllegalArgumentException("You need to provide some mapping kinds!");
        }

        if (kinds.length < 2) {
            return getNounMappingsOfKind(kinds[0]);
        }

        MutableList<INounMapping> result = Lists.mutable.empty();
        ImmutableList<INounMapping> mappings = getNounMappingsByWord(word);

        for (INounMapping mapping : mappings) {
            final ImmutableList<Double> probabilities = Lists.immutable.with(kinds).collect(mapping::getProbabilityForKind);
            if (probabilities.anySatisfy(p -> p <= 0)) {
                continue;
            }

            boolean similar = probabilities.allSatisfy(p1 -> probabilities.allSatisfy(p2 -> Math.abs(p1 - p2) < MAPPINGKIND_MAX_DIFF));
            if (similar) {
                result.add(mapping);
            }

        }

        return result.toImmutable();
    }

    ImmutableList<INounMapping> getNounMappingsOfKind(MappingKind mappingKind);

}
