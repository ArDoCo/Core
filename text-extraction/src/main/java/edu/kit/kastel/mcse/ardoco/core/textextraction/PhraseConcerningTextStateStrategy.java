/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.common.util.ElementWrapper;

public class PhraseConcerningTextStateStrategy extends DefaultTextStateStrategy {

    private static final Function<NounMapping, Integer> NOUN_MAPPING_HASH = nm -> Objects.hash(nm.getReference(), nm.getPhrases());
    private static final BiPredicate<NounMapping, NounMapping> NOUN_MAPPING_EQUALS = (nm1, nm2) -> (Objects.equals(nm1.getPhrases(), nm2
            .getPhrases()) && Objects.equals(nm1.getReference(), nm2.getReference()));

    public PhraseConcerningTextStateStrategy(TextStateImpl textState) {
        super.setTextState(textState);
    }

    @Override
    public ElementWrapper<NounMapping> wrap(NounMapping nounMapping) {
        return new ElementWrapper<>(NounMapping.class, nounMapping, NOUN_MAPPING_HASH, NOUN_MAPPING_EQUALS);
    }

    @Override
    public NounMapping addOrExtendNounMapping(Word word, MappingKind kind, Claimant claimant, double probability, ImmutableList<String> surfaceForms) {
        var nounMappingsWithWord = super.getTextState().getNounMappings().select(nm -> nm.getWords().contains(word));

        if (nounMappingsWithWord.size() > 0) {
            NounMapping nounMapping = nounMappingsWithWord.get(0);
            if (surfaceForms == null || surfaceForms.equals(nounMapping.getSurfaceForms())) {
                nounMapping.addKindWithProbability(kind, claimant, probability);
                return nounMapping;
            }
        } else if (nounMappingsWithWord.size() > 1) {
            throw new IllegalStateException("The word '" + word.getText() + "' occurs several times in the text state.");
        }
        ImmutableSet<Word> words = Sets.immutable.with(word);
        if (surfaceForms == null) {
            surfaceForms = Lists.immutable.with(word.getText());
        }
        NounMapping nounMapping = new NounMappingImpl(System.currentTimeMillis(), words, kind, claimant, probability, words.toImmutableList(), surfaceForms);
        super.getTextState().addNounMappingAddPhraseMapping(nounMapping);
        return nounMapping;
    }

    @Override
    public NounMapping mergeNounMappings(NounMapping firstNounMapping, NounMapping secondNounMapping, ImmutableList<Word> referenceWords, String reference,
            MappingKind mappingKind, Claimant claimant, double probability) {

        MutableSet<Word> mergedWords = firstNounMapping.getWords().toSet();
        mergedWords.addAllIterable(secondNounMapping.getWords());

        var distribution1 = firstNounMapping.getDistribution().toMap();
        var distribution2 = secondNounMapping.getDistribution().toMap();
        var mergedRawMap = Arrays.stream(MappingKind.values())
                .collect(Collectors.toMap( //
                        kind -> kind, //
                        kind -> putAllConfidencesTogether(distribution1.get(kind), distribution2.get(kind)) //
                ));
        MutableMap<MappingKind, Confidence> mergedDistribution = Maps.mutable.withMap(mergedRawMap);

        MutableList<String> mergedSurfaceForms = firstNounMapping.getSurfaceForms().toList();
        for (var surface : secondNounMapping.getSurfaceForms()) {
            if (mergedSurfaceForms.contains(surface))
                continue;
            mergedSurfaceForms.add(surface);
        }

        // TODO: Overwork similarity metrics & references
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
                mergedReference = this.getTextState().calculateNounMappingReference(mergedReferenceWords);
            }
        }

        NounMapping mergedNounMapping = new NounMappingImpl(NounMappingImpl.earliestCreationTime(firstNounMapping, secondNounMapping), mergedWords
                .toImmutableSortedSet(), mergedDistribution, mergedReferenceWords.toImmutable(), mergedSurfaceForms.toImmutable(), mergedReference);
        mergedNounMapping.addKindWithProbability(mappingKind, claimant, probability);

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
