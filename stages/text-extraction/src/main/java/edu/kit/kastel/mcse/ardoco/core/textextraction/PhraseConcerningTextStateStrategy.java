/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.TextStateStrategy;
import edu.kit.kastel.mcse.ardoco.core.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

public class PhraseConcerningTextStateStrategy extends DefaultTextStateStrategy {

    public PhraseConcerningTextStateStrategy(TextStateImpl textState) {
        super.setTextState(textState);
    }

    @Override
    public NounMapping addOrExtendNounMapping(Word word, MappingKind kind, Claimant claimant, double probability, ImmutableList<String> surfaceForms) {
        var nounMappingsWithWord = super.getTextState().getNounMappings().select(nm -> nm.getWords().contains(word));

        if (!nounMappingsWithWord.isEmpty()) {
            NounMapping nounMapping = nounMappingsWithWord.get(0);
            if (surfaceForms == null || surfaceForms.equals(nounMapping.getSurfaceForms())) {
                nounMapping.addKindWithProbability(kind, claimant, probability);
                return nounMapping;
            }
        } else if (nounMappingsWithWord.size() > 1) {
            throw new IllegalStateException("The word '" + word.getText() + "' occurs several times in the text state.");
        }
        ImmutableSortedSet<Word> words = SortedSets.immutable.with(word);
        if (surfaceForms == null) {
            surfaceForms = Lists.immutable.with(word.getText());
        }
        NounMapping nounMapping = new NounMappingImpl(words, kind, claimant, probability, words.toImmutableList(), surfaceForms);
        super.getTextState().addNounMappingAddPhraseMapping(nounMapping);
        return nounMapping;
    }

    @Override
    public NounMappingImpl mergeNounMappingsStateless(NounMapping firstNounMapping, NounMapping secondNounMapping, ImmutableList<Word> referenceWords,
            String reference, MappingKind mappingKind, Claimant claimant, double probability) {

        MutableSortedSet<Word> mergedWords = firstNounMapping.getWords().toSortedSet();
        mergedWords.addAllIterable(secondNounMapping.getWords());

        var distribution1 = firstNounMapping.getDistribution();
        var distribution2 = secondNounMapping.getDistribution();
        var mergedRawMap = Arrays.stream(MappingKind.values())
                .collect(Collectors.toMap( //
                        kind -> kind, //
                        kind -> putAllConfidencesTogether(distribution1.get(kind), distribution2.get(kind)) //
                ));
        MutableSortedMap<MappingKind, Confidence> mergedDistribution = SortedMaps.mutable.withSortedMap(mergedRawMap);

        MutableList<String> mergedSurfaceForms = firstNounMapping.getSurfaceForms().toList();
        for (var surface : secondNounMapping.getSurfaceForms()) {
            if (mergedSurfaceForms.contains(surface))
                continue;
            mergedSurfaceForms.add(surface);
        }

        // Maybe rework similarity metrics & references in the future
        ImmutableList<Word> mergedReferenceWords = referenceWords;

        if (mergedReferenceWords == null) {
            MutableList<Word> mergedRefWords = Lists.mutable.withAll(firstNounMapping.getReferenceWords());
            mergedRefWords.addAllIterable(secondNounMapping.getReferenceWords());
            mergedReferenceWords = mergedRefWords.toImmutable();
        }

        String mergedReference = reference;

        if (mergedReference == null) {

            if (firstNounMapping.getReference().equalsIgnoreCase(secondNounMapping.getReference())) {
                mergedReference = firstNounMapping.getReference();
            } else {
                mergedReference = calculateNounMappingReference(mergedReferenceWords);
            }
        }

        NounMappingImpl mergedNounMapping = new NounMappingImpl(NounMappingImpl.earliestCreationTime(firstNounMapping, secondNounMapping), mergedWords
                .toImmutableSortedSet(), mergedDistribution.toImmutable(), mergedReferenceWords.toImmutable(), mergedSurfaceForms.toImmutable(),
                mergedReference);
        mergedNounMapping.addKindWithProbability(mappingKind, claimant, probability);

        return mergedNounMapping;
    }

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
        return PhraseConcerningTextStateStrategy::new;
    }
}
