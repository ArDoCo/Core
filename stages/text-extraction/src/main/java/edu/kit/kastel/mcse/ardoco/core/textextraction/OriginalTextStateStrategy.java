/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.io.Serializable;
import java.util.Arrays;
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
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.TextStateStrategy;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

import org.jetbrains.annotations.NotNull;

public class OriginalTextStateStrategy extends DefaultTextStateStrategy implements Serializable {

    OriginalTextStateStrategy(TextStateImpl textState) {
        super.setTextState(textState);
    }

    @NotNull
    @Override
    public NounMapping addOrExtendNounMapping(Word word, MappingKind kind, Claimant claimant, double probability, ImmutableList<String> surfaceForms) {

        NounMapping disposableNounMapping = new NounMappingImpl(SortedSets.immutable.with(word), kind, claimant, probability, Lists.immutable.with(word),
                surfaceForms);

        for (var existingNounMapping : super.getTextState().getNounMappings()) {
            if (SimilarityUtils.areNounMappingsSimilar(disposableNounMapping, existingNounMapping)) {

                return mergeNounMappings(existingNounMapping, disposableNounMapping, disposableNounMapping.getReferenceWords(),
                        disposableNounMapping.getReference(), disposableNounMapping.getKind(), claimant, disposableNounMapping.getProbability());
            }
        }

        getTextState().addNounMappingAddPhraseMapping(disposableNounMapping);
        return disposableNounMapping;
    }

    @NotNull
    @Override
    public NounMappingImpl mergeNounMappingsStateless(@NotNull NounMapping firstNounMapping, @NotNull NounMapping secondNounMapping, ImmutableList<Word> referenceWords,
            String reference, @NotNull MappingKind mappingKind, @NotNull Claimant claimant, double probability) {

        MutableSortedSet<Word> mergedWords = firstNounMapping.getWords().toSortedSet();
        mergedWords.add(secondNounMapping.getReferenceWords().get(0));
        //This makes only sense under specific conditions, since it is sequentially dependent. It might be fixed in future versions

        var existingNounMappingDistribution = firstNounMapping.getDistribution();
        var disposableNounMappingDistribution = secondNounMapping.getDistribution();
        var mergedRawMap = Arrays.stream(MappingKind.values()).collect(Collectors.toMap( //
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

        return new NounMappingImpl(NounMappingImpl.earliestCreationTime(firstNounMapping, secondNounMapping), mergedWords.toSortedSet().toImmutable(),
                mergedDistribution.toImmutable(), mergedReferenceWords.toImmutable(), mergedSurfaceForms.toImmutable(), mergedReference);
    }

    @NotNull
    @Override
    public NounMappingImpl mergeNounMappings(NounMapping firstNounMapping, NounMapping secondNounMapping, ImmutableList<Word> referenceWords, String reference,
            MappingKind mappingKind, Claimant claimant, double probability) {
        var mergedNounMapping = mergeNounMappingsStateless(firstNounMapping, secondNounMapping, referenceWords, reference, mappingKind, claimant, probability);

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
