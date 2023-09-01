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
     * Sets the {@link TextStateStrategy} used by the text state.
     *
     * @param textStateStrategy the text strategy
     */
    void setTextStateStrategy(TextStateStrategy textStateStrategy);

    /**
     * {@return the text state strategy of the text state}
     */
    TextStateStrategy getTextStateStrategy();

    /**
     * * Adds a name mapping to the state.
     *
     * @param word        word of the mapping
     * @param kind        the kind of the mapping
     * @param probability probability to be a name mapping
     */
    default NounMapping addNounMapping(Word word, MappingKind kind, Claimant claimant, double probability) {
        return getTextStateStrategy().addOrExtendNounMapping(word, kind, claimant, probability, Lists.immutable.with(word.getText()));
    }

    /**
     * * Adds a name mapping to the state.
     *
     * @param word         word of the mapping
     * @param kind         the kind of the mapping
     * @param probability  probability to be a name mapping
     * @param surfaceForms list of the appearances of the mapping
     */
    default NounMapping addNounMapping(Word word, MappingKind kind, Claimant claimant, double probability, ImmutableList<String> surfaceForms) {
        return getTextStateStrategy().addOrExtendNounMapping(word, kind, claimant, probability, surfaceForms);
    }

    default NounMapping addNounMapping(ImmutableSortedSet<Word> words, MappingKind kind, Claimant claimant, double probability,
            ImmutableList<Word> referenceWords, ImmutableList<String> surfaceForms, String reference) {
        return getTextStateStrategy().addNounMapping(words, kind, claimant, probability, referenceWords, surfaceForms, reference);
    }

    default NounMapping addNounMapping(ImmutableSortedSet<Word> words, ImmutableSortedMap<MappingKind, Confidence> distribution,
            ImmutableList<Word> referenceWords, ImmutableList<String> surfaceForms, String reference) {
        return getTextStateStrategy().addNounMapping(words, distribution, referenceWords, surfaceForms, reference);
    }

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

    default WordAbbreviation addWordAbbreviation(String abbreviation, Word word) {
        logger.debug("Added word abbreviation for {} with meaning {}", abbreviation, word.getText());
        var wordAbbreviation = getTextStateStrategy().addOrExtendWordAbbreviation(abbreviation, word);
        AbbreviationDisambiguationHelper.addTransient(wordAbbreviation);
        return wordAbbreviation;
    }

    default PhraseAbbreviation addPhraseAbbreviation(String abbreviation, Phrase phrase) {
        logger.debug("Added phrase abbreviation for {} with meaning {}", abbreviation, phrase.getText());
        var phraseAbbreviation = getTextStateStrategy().addOrExtendPhraseAbbreviation(abbreviation, phrase);
        AbbreviationDisambiguationHelper.addTransient(phraseAbbreviation);
        return phraseAbbreviation;
    }

    ImmutableSortedSet<WordAbbreviation> getWordAbbreviations();

    default ImmutableSortedSet<WordAbbreviation> getWordAbbreviations(Word word) {
        return SortedSets.immutable.ofAll(getWordAbbreviations().stream().filter(w -> w.getWords().contains(word)).toList());
    }

    default Optional<WordAbbreviation> getWordAbbreviation(String abbreviation) {
        return getWordAbbreviations().stream().filter(w -> w.getAbbreviation().equals(abbreviation)).findFirst();
    }

    ImmutableSortedSet<PhraseAbbreviation> getPhraseAbbreviations();

    default ImmutableSortedSet<PhraseAbbreviation> getPhraseAbbreviations(Phrase phrase) {
        return SortedSets.immutable.ofAll(getPhraseAbbreviations().stream().filter(p -> p.getPhrases().contains(phrase)).toList());
    }

    default Optional<PhraseAbbreviation> getPhraseAbbreviation(String abbreviation) {
        return getPhraseAbbreviations().stream().filter(p -> p.getAbbreviation().equals(abbreviation)).findFirst();
    }
}
