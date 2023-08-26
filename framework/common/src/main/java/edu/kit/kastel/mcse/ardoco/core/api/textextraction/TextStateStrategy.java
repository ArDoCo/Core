/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.textextraction;

import static edu.kit.kastel.mcse.ardoco.core.common.AggregationFunctions.AVERAGE;

import java.io.Serializable;
import java.util.function.Function;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.common.AggregationFunctions;
import edu.kit.kastel.mcse.ardoco.core.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * The Interface for strategies for the text state. Responsible for creating {@link NounMapping NounMappings} from their constituent parts in a variety of
 * situations.
 */
public interface TextStateStrategy extends Serializable {
    AggregationFunctions DEFAULT_AGGREGATOR = AVERAGE;

    NounMapping addOrExtendNounMapping(Word word, MappingKind kind, Claimant claimant, double probability, ImmutableList<String> surfaceForms);

    NounMapping createNounMappingStateless(ImmutableSortedSet<Word> words, ImmutableSortedMap<MappingKind, Confidence> distribution,
            ImmutableList<Word> referenceWords, ImmutableList<String> surfaceForms, String reference);

    NounMapping addNounMapping(ImmutableSortedSet<Word> words, ImmutableSortedMap<MappingKind, Confidence> distribution, ImmutableList<Word> referenceWords,
            ImmutableList<String> surfaceForms, String reference);

    NounMapping addNounMapping(ImmutableSortedSet<Word> words, MappingKind kind, Claimant claimant, double probability, ImmutableList<Word> referenceWords,
            ImmutableList<String> surfaceForms, String reference);

    NounMapping mergeNounMappingsStateless(NounMapping firstNounMapping, NounMapping secondNounMapping, ImmutableList<Word> referenceWords, String reference,
            MappingKind mappingKind, Claimant claimant, double probability);

    NounMapping mergeNounMappings(NounMapping firstNounMapping, NounMapping secondNounMapping, ImmutableList<Word> referenceWords, String reference,
            MappingKind mappingKind, Claimant claimant, double probability);

    Function<? extends TextState, TextStateStrategy> creator();

    default String calculateNounMappingReference(ImmutableList<Word> referenceWords) {
        StringBuilder refBuilder = new StringBuilder();
        referenceWords.toSortedListBy(Word::getPosition);
        referenceWords.toSortedListBy(Word::getSentenceNo);

        for (int i = 0; i < referenceWords.size() - 1; i++) {
            refBuilder.append(referenceWords.get(i).getText()).append(" ");
        }
        refBuilder.append(referenceWords.get(referenceWords.size() - 1).getText());
        return refBuilder.toString();
    }

    WordAbbreviation addOrExtendWordAbbreviation(String abbreviation, Word word);

    PhraseAbbreviation addOrExtendPhraseAbbreviation(String abbreviation, Phrase phrase);
}
