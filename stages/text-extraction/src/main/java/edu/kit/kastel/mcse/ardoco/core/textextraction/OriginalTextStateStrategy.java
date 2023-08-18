/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

public class OriginalTextStateStrategy extends DefaultTextStateStrategy {

    private static final Function<NounMapping, Integer> NOUN_MAPPING_HASH = nm -> Objects.hash(nm.getReferenceWords().toSet(), nm.getWords());
    private static final BiPredicate<NounMapping, NounMapping> NOUN_MAPPING_EQUALS = (nm1, nm2) -> Objects.equals(nm1.getReferenceWords().toSet(), nm2
            .getReferenceWords()
            .toSet()) && Objects.equals(nm1.getWords(), nm2.getWords());

    OriginalTextStateStrategy(TextStateImpl textState) {
        super.setTextState(textState);
    }

    @Override
    public NounMapping addOrExtendNounMapping(Word word, MappingKind kind, Claimant claimant, double probability, ImmutableList<String> surfaceForms) {

        NounMapping disposableNounMapping = new NounMappingImpl(System.currentTimeMillis(), SortedSets.immutable.with(word), kind, claimant, probability,
                Lists.immutable.with(word), surfaceForms);

        for (var existingNounMapping : super.getTextState().getNounMappings()) {
            if (SimilarityUtils.areNounMappingsSimilar(disposableNounMapping, existingNounMapping)) {

                return mergeNounMappings(existingNounMapping, disposableNounMapping, disposableNounMapping.getReferenceWords(), disposableNounMapping
                        .getReference(), disposableNounMapping.getKind(), claimant, disposableNounMapping.getProbability());
            }
        }
        super.getTextState().addNounMappingAddPhraseMapping(disposableNounMapping);

        return disposableNounMapping;
    }

    @Override
    public NounMapping mergeNounMappings(NounMapping firstNounMapping, NounMapping secondNounMapping, ImmutableList<Word> referenceWords, String reference,
            MappingKind mappingKind, Claimant claimant, double probability) {

        MutableSortedSet<Word> mergedWords = firstNounMapping.getWords().toSortedSet();
        mergedWords.add(secondNounMapping.getReferenceWords().get(0));
        //This makes only sense under specific conditions, since it is sequentially dependent. It might be fixed in future versions

        var existingNounMappingDistribution = firstNounMapping.getDistribution();
        var disposableNounMappingDistribution = secondNounMapping.getDistribution();
        var mergedRawMap = Arrays.stream(MappingKind.values())
                .collect(Collectors.toMap( //
                        kind -> kind, //
                        kind -> putAllConfidencesTogether(existingNounMappingDistribution.get(kind), disposableNounMappingDistribution.get(kind)) //
                ));
        MutableSortedMap<MappingKind, Confidence> mergedDistribution = SortedMaps.mutable.withSortedMap(mergedRawMap);

        MutableList<String> mergedSurfaceForms = firstNounMapping.getSurfaceForms().toList();
        for (var surface : secondNounMapping.getSurfaceForms()) {
            if (mergedSurfaceForms.contains(surface))
                continue;
            mergedSurfaceForms.add(surface);
        }

        ImmutableList<Word> mergedReferenceWords = firstNounMapping.getReferenceWords();

        String mergedReference = mergedReferenceWords.collect(Word::getText).makeString(" ");

        NounMapping mergedNounMapping = new NounMappingImpl(NounMappingImpl.earliestCreationTime(firstNounMapping, secondNounMapping), mergedWords.toSortedSet()
                .toImmutable(), mergedDistribution.toImmutable(), mergedReferenceWords.toImmutable(), mergedSurfaceForms.toImmutable(), mergedReference);

        this.getTextState().removeNounMappingFromState(firstNounMapping, mergedNounMapping);
        this.getTextState().removeNounMappingFromState(secondNounMapping, mergedNounMapping);

        this.getTextState().addNounMappingAddPhraseMapping(mergedNounMapping);

        return mergedNounMapping;
    }

    @Override
    public Function<TextStateImpl, TextStateStrategy> creator() {
        return OriginalTextStateStrategy::new;
    }
}
