/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.textextraction;

import java.util.Optional;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.common.util.AbbreviationDisambiguationHelper;
import edu.kit.kastel.mcse.ardoco.core.configuration.IConfigurable;
import edu.kit.kastel.mcse.ardoco.core.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * The Interface ITextState.
 */
public interface TextState extends IConfigurable, PipelineStepData {
    Logger logger = LoggerFactory.getLogger(TextState.class);

    String ID = "TextState";

    /**
     * {@return the text state strategy of the text state}
     */
    TextStateStrategy getTextStateStrategy();

    /**
     * Adds a name mapping to the state.
     *
     * @param word        word of the mapping
     * @param kind        the kind of the mapping
     * @param probability probability to be a name mapping
     */
    default NounMapping addNounMapping(Word word, MappingKind kind, Claimant claimant, double probability) {
        return getTextStateStrategy().addOrExtendNounMapping(word, kind, claimant, probability, Lists.immutable.with(word.getText()));
    }

    /**
     * Adds a noun mapping of the specified kind to the state with the specified word and surface forms with the provided confidence. The adding and merging of
     * the mapping is delegated to the {@link TextStateStrategy}.
     *
     * @param word         word of the mapping
     * @param kind         the kind of the mapping
     * @param claimant     the claimant of the mapping
     * @param probability  probability to be a noun mapping of this kind
     * @param surfaceForms list of the appearances of the mapping
     */
    default NounMapping addNounMapping(Word word, MappingKind kind, Claimant claimant, double probability, ImmutableList<String> surfaceForms) {
        return getTextStateStrategy().addOrExtendNounMapping(word, kind, claimant, probability, surfaceForms);
    }

    /**
     * Adds a noun mapping of the specified kind to the state that contains the specified words, surface forms, etc. The adding and merging of the mapping is
     * delegated to the {@link TextStateStrategy}.
     *
     * @param words          words of the mapping
     * @param kind           kind of the mapping
     * @param claimant       claimant of the mapping
     * @param probability    probability to be a noun mapping of this kind
     * @param referenceWords references of this noun mapping
     * @param surfaceForms   surface forms of this noun mapping
     * @param reference      a joined reference string
     * @return the new or merged mapping
     */
    default NounMapping addNounMapping(ImmutableSortedSet<Word> words, MappingKind kind, Claimant claimant, double probability,
            ImmutableList<Word> referenceWords, ImmutableList<String> surfaceForms, String reference) {
        return getTextStateStrategy().addNounMapping(words, kind, claimant, probability, referenceWords, surfaceForms, reference);
    }

    /**
     * Adds a noun mapping of the specified kind to the state that contains the specified words, surface forms, etc. The adding and merging of the mapping is
     * delegated to the {@link TextStateStrategy}.
     *
     * @param words          words of the mapping
     * @param distribution   distribution of the mapping for the mapping kinds
     * @param referenceWords reference words of the mapping
     * @param surfaceForms   surface forms of the mapping
     * @param reference      a joined reference string
     * @return the new or merged mapping
     */
    default NounMapping addNounMapping(ImmutableSortedSet<Word> words, ImmutableSortedMap<MappingKind, Confidence> distribution,
            ImmutableList<Word> referenceWords, ImmutableList<String> surfaceForms, String reference) {
        return getTextStateStrategy().addNounMapping(words, distribution, referenceWords, surfaceForms, reference);
    }

    /**
     * Removes a noun mapping from the state. Also removes phrase mappings that are associated with the noun mapping.
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

    /**
     * {@return all phrase mappings containing a specific phrase}
     *
     * @param phrase the phrase
     */
    default ImmutableList<PhraseMapping> getPhraseMappings(Phrase phrase) {
        return Lists.immutable.fromStream(getPhraseMappings().stream().filter(pm -> pm.getPhrases().contains(phrase)));
    }

    ImmutableList<NounMapping> getNounMappingsOfKind(MappingKind mappingKind);

    ImmutableList<NounMapping> getNounMappingsThatBelongToTheSamePhraseMapping(NounMapping nounMapping);

    void mergeNounMappings(NounMapping nounMapping, NounMapping otherNounMapping, Claimant claimant, ImmutableList<Word> referenceWords);

    NounMapping mergeNounMappings(NounMapping nounMapping, NounMapping textuallyEqualNounMapping, Claimant claimant);

    void mergePhraseMappingsAndNounMappings(PhraseMapping phraseMapping, PhraseMapping similarPhraseMapping,
            MutableList<Pair<NounMapping, NounMapping>> similarNounMappings, Claimant claimant);

    PhraseMapping mergePhraseMappings(PhraseMapping phraseMapping, PhraseMapping similarPhraseMapping);

    NounMapping setReferenceOfNounMapping(NounMapping nounMapping, ImmutableList<Word> referenceWords, String reference);

    ImmutableList<NounMapping> getMappingsThatCouldBeOfKind(Word word, MappingKind kind);

    ImmutableList<NounMapping> getMappingsThatCouldBeMultipleKinds(Word word, MappingKind... kinds);

    ImmutableList<NounMapping> getNounMappingsByWord(Word word);

    ImmutableList<NounMapping> getNounMappingsByWordAndKind(Word word, MappingKind kind);

    boolean isWordContainedByMappingKind(Word word, MappingKind kind);

    ImmutableList<NounMapping> getNounMappingsWithSimilarReference(String reference);

    /**
     * Adds a {@link WordAbbreviation} for the abbreviation with the specified word meaning to the state using the state's {@link TextStateStrategy}.
     *
     * @param abbreviation the abbreviation
     * @param word         the meaning
     * @return the resulting word abbreviation in the state
     */
    default WordAbbreviation addWordAbbreviation(String abbreviation, Word word) {
        logger.debug("Added word abbreviation for {} with meaning {}", abbreviation, word.getText());
        var wordAbbreviation = getTextStateStrategy().addOrExtendWordAbbreviation(abbreviation, word);
        AbbreviationDisambiguationHelper.addTransient(wordAbbreviation);
        return wordAbbreviation;
    }

    /**
     * Adds a {@link PhraseAbbreviation} for the abbreviation with the specified phrase meaning to the state using the state's {@link TextStateStrategy}.
     *
     * @param abbreviation the abbrevation
     * @param phrase       the meaning
     * @return the resulting phrase abbreviation in the state
     */
    default PhraseAbbreviation addPhraseAbbreviation(String abbreviation, Phrase phrase) {
        logger.debug("Added phrase abbreviation for {} with meaning {}", abbreviation, phrase.getText());
        var phraseAbbreviation = getTextStateStrategy().addOrExtendPhraseAbbreviation(abbreviation, phrase);
        AbbreviationDisambiguationHelper.addTransient(phraseAbbreviation);
        return phraseAbbreviation;
    }

    /**
     * {@return all word abbreviations from the state}
     */
    ImmutableSortedSet<WordAbbreviation> getWordAbbreviations();

    /**
     * {@return all word abbreviations from the state that have the specified meaning}
     *
     * @param word the meaning to search for
     */
    default ImmutableSortedSet<WordAbbreviation> getWordAbbreviations(Word word) {
        return SortedSets.immutable.ofAll(getWordAbbreviations().stream().filter(w -> w.getWords().contains(word)).toList());
    }

    /**
     * {@return the optional word abbreviation from the state that has the specified abbreviation}
     *
     * @param abbreviation the abbreviation
     */
    default Optional<WordAbbreviation> getWordAbbreviation(String abbreviation) {
        return getWordAbbreviations().stream().filter(w -> w.getAbbreviation().equals(abbreviation)).findFirst();
    }

    /**
     * {@return all phrase abbreviations from the state}
     */
    ImmutableSortedSet<PhraseAbbreviation> getPhraseAbbreviations();

    /**
     * {@return all phrase abbreviations from the state that have the specified meaning}
     *
     * @param phrase the meaning to search for
     */
    default ImmutableSortedSet<PhraseAbbreviation> getPhraseAbbreviations(Phrase phrase) {
        return SortedSets.immutable.ofAll(getPhraseAbbreviations().stream().filter(p -> p.getPhrases().contains(phrase)).toList());
    }

    /**
     * {@return the optional phrase abbreviation from the state that has the specified abbreviation}
     *
     * @param abbreviation the abbreviation
     */
    default Optional<PhraseAbbreviation> getPhraseAbbreviation(String abbreviation) {
        return getPhraseAbbreviations().stream().filter(p -> p.getAbbreviation().equals(abbreviation)).findFirst();
    }
}
