/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.common.util.ElementWrapper;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

public class OriginalTextStateStrategy extends DefaultTextStateStrategy {

    private static final Function<NounMapping, Integer> NOUN_MAPPING_HASH = nm -> Objects.hash(nm.getReferenceWords().toSet(), nm.getWords());
    private static final BiPredicate<NounMapping, NounMapping> NOUN_MAPPING_EQUALS = (nm1, nm2) -> Objects.equals(nm1.getReferenceWords().toSet(), nm2
            .getReferenceWords()
            .toSet()) && Objects.equals(nm1.getWords(), nm2.getWords());

    OriginalTextStateStrategy(TextStateImpl textState) {
        super.setTextState(textState);
    }

    @Override
    public NounMapping addOrExtendNounMapping(Word word, MappingKind kind, Claimant claimant, double probability, ImmutableSet<String> surfaceForms) {

        NounMapping nounMapping = new NounMappingImpl(Sets.immutable.with(word), kind, claimant, probability, Lists.immutable.with(word), surfaceForms);

        for (var existingNounMapping : super.getTextState().getNounMappings()) {
            if (SimilarityUtils.areNounMappingsSimilar(nounMapping, existingNounMapping)) {

                return mergeNounMappings(existingNounMapping, nounMapping, nounMapping.getReferenceWords(), nounMapping.getReference(), nounMapping.getKind(),
                        claimant, nounMapping.getProbability());

                // NounMapping extendedNounMapping = appendNounMappingToExistingNounMapping(nounMapping,
                // existingNounMapping, claimant);
                // return extendedNounMapping;
            }
        }
        super.getTextState().addNounMappingAddPhraseMapping(nounMapping);

        return nounMapping;
    }

    /*
     * private NounMapping appendNounMappingToExistingNounMapping(NounMapping disposableNounMapping, NounMapping
     * existingNounMapping, Claimant claimant) {
     * 
     * existingNounMapping.addKindWithProbability(disposableNounMapping.getKind(), claimant,
     * disposableNounMapping.getProbability());
     * existingNounMapping.addOccurrence(disposableNounMapping.getSurfaceForms());
     * existingNounMapping.addWord(disposableNounMapping.getReferenceWords().get(0)); }
     */
    @Override
    public ElementWrapper<NounMapping> wrap(NounMapping nounMapping) {

        return new ElementWrapper<>(NounMapping.class, nounMapping, NOUN_MAPPING_HASH, NOUN_MAPPING_EQUALS);
    }

    public NounMapping mergeNounMappings(NounMapping nounMapping, NounMapping nounMapping2, ImmutableList<Word> referenceWords, String reference,
            MappingKind mappingKind, Claimant claimant, double probability) {
        /*
         * if (!textState.getNounMappings().contains(nounMapping) ||
         * !textState.getNounMappings().contains(nounMapping2)) { throw new
         * IllegalArgumentException("The noun mappings that are merged should be in the current text state!"); }
         */

        MutableSet<Word> mergedWords = nounMapping.getWords().toSet();
        mergedWords.add(nounMapping2.getReferenceWords().get(0));
        //mergedWords.addAllIterable(nounMapping2.getWords());

        var distribution1 = nounMapping.getDistribution().toMap();
        var distribution2 = nounMapping2.getDistribution().toMap();
        var mergedRawMap = Arrays.stream(MappingKind.values())
                .collect(Collectors.toMap( //
                        kind -> kind, //
                        kind -> putAllConfidencesTogether(distribution1.get(kind), distribution2.get(kind)) //
                ));
        MutableMap<MappingKind, Confidence> mergedDistribution = Maps.mutable.withMap(mergedRawMap);

        MutableSet<String> mergedSurfaceForms = nounMapping.getSurfaceForms().toSet();
        mergedSurfaceForms.addAllIterable(nounMapping2.getSurfaceForms());

        ImmutableList<Word> mergedReferenceWords = nounMapping.getReferenceWords();

        String mergedReference = mergedReferenceWords.collect(Word::getText).makeString(" ");

        NounMapping mergedNounMapping = new NounMappingImpl(mergedWords.toSortedSet().toImmutable(), mergedDistribution, mergedReferenceWords.toImmutable(),
                mergedSurfaceForms.toImmutable(), mergedReference, new AtomicBoolean(false));

        this.getTextState().removeNounMappingFromState(nounMapping);
        this.getTextState().removeNounMappingFromState(nounMapping2);

        this.getTextState().addNounMappingAddPhraseMapping(mergedNounMapping);

        return mergedNounMapping;
    }

}
