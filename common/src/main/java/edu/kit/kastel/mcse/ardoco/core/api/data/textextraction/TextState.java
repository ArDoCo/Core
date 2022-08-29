/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.textextraction;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.ImmutableSet;

import edu.kit.kastel.informalin.data.PipelineStepData;
import edu.kit.kastel.informalin.framework.common.ICopyable;
import edu.kit.kastel.informalin.framework.common.tuple.Pair;
import edu.kit.kastel.informalin.framework.configuration.IConfigurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;

/**
 * The Interface ITextState.
 */
public interface TextState extends ICopyable<TextState>, IConfigurable, PipelineStepData {
    String ID = "TextState";

    /**
     * * Adds a name mapping to the state.
     *
     * @param word        word of the mapping
     * @param kind        the kind of the mapping
     * @param probability probability to be a name mapping
     */
    NounMapping addNounMapping(Word word, MappingKind kind, Claimant claimant, double probability);

    /**
     * * Adds a name mapping to the state.
     *
     * @param word         word of the mapping
     * @param kind         the kind of the mapping
     * @param probability  probability to be a name mapping
     * @param surfaceForms list of the appearances of the mapping
     */
    NounMapping addNounMapping(Word word, MappingKind kind, Claimant claimant, double probability, ImmutableList<String> surfaceForms);

    NounMapping addNounMapping(ImmutableSet<Word> words, MappingKind kind, Claimant claimant, double probability, ImmutableList<Word> referenceWords,
            ImmutableList<String> surfaceForms, String reference);

    NounMapping addNounMapping(ImmutableSet<Word> words, MutableMap<MappingKind, Confidence> distribution, ImmutableList<Word> referenceWords,
            ImmutableList<String> surfaceForms, String reference);

    // --- remove section --->

    /**
     * Removes a noun mapping from the state.
     *
     * @param nounMapping noun mapping to remove
     * @param replacement the (optional) future replacement of the noun mapping
     * @see NounMapping#onDelete(NounMapping)
     */
    void removeNounMapping(NounMapping nounMapping, NounMapping replacement);

    /**
     * Returns the noun mapping containing the given word.
     *
     * @param word the given word
     * @return the noun mapping of the word or null if the text state has no noun mapping containing the given word.
     */
    NounMapping getNounMappingByWord(Word word);

    PhraseMapping getPhraseMappingByNounMapping(NounMapping nounMapping);

    ImmutableList<NounMapping> getNounMappingsByPhraseMapping(PhraseMapping phraseMapping);

    /**
     * Returns a list of all references of noun mappings.
     *
     * @param kind of references that shall be collected
     * @return all references of noun mappings with the specified kind as list.
     */
    ImmutableList<String> getListOfReferences(MappingKind kind);

    /**
     * Gets the all noun mappings.
     *
     * @return the all mappings
     */
    ImmutableList<NounMapping> getNounMappings();

    ImmutableList<PhraseMapping> getPhraseMappings();

    ImmutableList<NounMapping> getNounMappingsOfKind(MappingKind mappingKind);

    ImmutableList<NounMapping> getNounMappingsThatBelongToTheSamePhraseMapping(NounMapping nounMapping);

    void mergeNounMappings(NounMapping nounMapping, NounMapping otherNounMapping, Claimant claimant, ImmutableList<Word> referenceWords);

    void mergeNounMappings(NounMapping nounMapping, NounMapping textuallyEqualNounMapping, Claimant claimant);

    void mergePhraseMappingsAndNounMappings(PhraseMapping phraseMapping, PhraseMapping similarPhraseMapping,
            MutableList<Pair<NounMapping, NounMapping>> similarNounMappings, Claimant claimant);

    void mergePhraseMappings(PhraseMapping phraseMapping, PhraseMapping similarPhraseMapping);

    NounMapping setReferenceOfNounMapping(NounMapping nounMapping, ImmutableList<Word> referenceWords, String reference);

    void mergeNounMappings(NounMapping nounMapping, MutableList<NounMapping> nounMappingsToMerge, Claimant claimant);

    @Deprecated
    ImmutableList<NounMapping> getMappingsThatCouldBeOfKind(Word word, MappingKind kind);

    @Deprecated
    ImmutableList<NounMapping> getMappingsThatCouldBeMultipleKinds(Word word, MappingKind... kinds);

    @Deprecated
    ImmutableList<NounMapping> getNounMappingsByWord(Word word);

    @Deprecated
    ImmutableList<NounMapping> getNounMappingsByWordAndKind(Word word, MappingKind kind);

    @Deprecated
    boolean isWordContainedByMappingKind(Word word, MappingKind kind);

    @Deprecated
    ImmutableList<NounMapping> getNounMappingsWithSimilarReference(String reference);
}
