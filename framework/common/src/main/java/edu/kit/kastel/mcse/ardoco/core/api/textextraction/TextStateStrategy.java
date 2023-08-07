/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.textextraction;

import edu.kit.kastel.mcse.ardoco.core.common.AggregationFunctions;
import edu.kit.kastel.mcse.ardoco.core.data.Confidence;

import java.io.Serializable;
import java.util.function.Function;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.common.util.ElementWrapper;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.ImmutableSet;

import static edu.kit.kastel.mcse.ardoco.core.common.AggregationFunctions.AVERAGE;

/**
 * The Interface for strategies for the text state. Responsible for creating {@link NounMapping NounMappings} from their constituent parts in a variety of situations.
 */
public interface TextStateStrategy extends Serializable {
    static final AggregationFunctions DEFAULT_AGGREGATOR = AVERAGE;

    NounMapping addOrExtendNounMapping(Word word, MappingKind kind, Claimant claimant, double probability, ImmutableList<String> surfaceForms);

    NounMapping addNounMapping(ImmutableSet<Word> words, MutableMap<MappingKind, Confidence> distribution, ImmutableList<Word> referenceWords,
            ImmutableList<String> surfaceForms, String reference);

    default NounMapping addNounMapping(ImmutableSet<Word> words, MappingKind kind, Claimant claimant, double probability, ImmutableList<Word> referenceWords,
            ImmutableList<String> surfaceForms, String reference) {
        MutableMap<MappingKind, Confidence> distribution = Maps.mutable.empty();
        distribution.put(MappingKind.NAME, new Confidence(DEFAULT_AGGREGATOR));
        distribution.put(MappingKind.TYPE, new Confidence(DEFAULT_AGGREGATOR));
        var nounMapping = addNounMapping(words, distribution, referenceWords, surfaceForms, reference);
        nounMapping.addKindWithProbability(kind, claimant, probability);
        return nounMapping;
    }

    ElementWrapper<NounMapping> wrap(NounMapping nounMapping);

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
}
