package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.informalin.framework.common.AggregationFunctions;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.common.util.ElementWrapper;

public class PhraseConcerningTextStateStrategy implements TextStateStrategy {

    private static final Function<NounMapping, Integer> NOUN_MAPPING_HASH = nm -> Objects.hash(nm.getReference(), nm.getPhrases());
    private static final BiPredicate<NounMapping, NounMapping> NOUN_MAPPING_EQUALS = (nm1,
            nm2) -> (Objects.equals(nm1.getPhrases(), nm2.getPhrases()) && Objects.equals(nm1.getReference(), nm2.getReference()));
    private final TextStateImpl textState;

    PhraseConcerningTextStateStrategy(TextStateImpl textState) {
        this.textState = textState;
    }

    public ElementWrapper<NounMapping> wrap(NounMapping nounMapping) {
        return new ElementWrapper<>(NounMapping.class, nounMapping, NOUN_MAPPING_HASH, NOUN_MAPPING_EQUALS);
    }

    @Override
    public NounMapping addOrExtendNounMapping(Word word, MappingKind kind, Claimant claimant, double probability, ImmutableSet<String> surfaceForms) {
        var nounMappingsWithWord = textState.getNounMappings().select(nm -> nm.getWords().contains(word));

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
            surfaceForms = Sets.immutable.with(word.getText());
        }
        NounMapping nounMapping = new NounMappingImpl(words, kind, claimant, probability, words.toImmutableList(), surfaceForms);
        textState.addNounMappingAddPhraseMapping(nounMapping);
        return nounMapping;
    }

    @Override
    public void mergeNounMappings(NounMapping nounMapping, MutableList<NounMapping> nounMappingsToMerge, Claimant claimant) {

        if (!textState.getNounMappings().contains(nounMapping)) {
            throw new IllegalStateException("The text state should contain the noun mappings that are merged!");
        }

        for (NounMapping otherNounMapping : nounMappingsToMerge) {

            if (!textState.getNounMappings().contains(otherNounMapping)) {

                final NounMapping finalOtherNounMapping = otherNounMapping;
                var otherNounMapping2 = textState.getNounMappings().select(nm -> nm.getWords().containsAllIterable(finalOtherNounMapping.getWords()));
                if (otherNounMapping2.size() == 0) {
                    continue;
                } else if (otherNounMapping2.size() == 1) {
                    otherNounMapping = otherNounMapping2.get(0);
                } else {
                    throw new IllegalStateException();
                }
            }

            assert (textState.getNounMappings().contains(otherNounMapping));

            var references = nounMapping.getReferenceWords().toList();
            references.addAllIterable(otherNounMapping.getReferenceWords());
            textState.mergeNounMappings(nounMapping, otherNounMapping, claimant, references.toImmutable());

            var mergedWords = Sets.mutable.empty();
            mergedWords.addAllIterable(nounMapping.getWords());
            mergedWords.addAllIterable(otherNounMapping.getWords());

            var mergedNounMapping = textState.getNounMappings().select(nm -> nm.getWords().toSet().equals(mergedWords));

            assert (mergedNounMapping.size() == 1);

            nounMapping = mergedNounMapping.get(0);
        }
    }

    @Override
    public NounMapping mergeNounMappings(NounMapping nounMapping, NounMapping nounMapping2, ImmutableList<Word> referenceWords, String reference,
            MappingKind mappingKind, Claimant claimant, double probability) {

        if (!textState.getNounMappings().contains(nounMapping) || !textState.getNounMappings().contains(nounMapping2)) {
            throw new IllegalArgumentException("The noun mappings that are merged should be in the current text state!");
        }

        MutableSet<Word> mergedWords = nounMapping.getWords().toSet();
        mergedWords.addAllIterable(nounMapping2.getWords());

        MutableMap<MappingKind, Confidence> mergedDistribution = nounMapping.getDistribution().toMap();
        AggregationFunctions globalAggregationFunc = nounMapping.getGlobalAggregationFunction();
        AggregationFunctions localAggregationFunc = nounMapping.getLocalAggregationFunction();
        var distribution2 = nounMapping2.getDistribution().toMap();
        mergedDistribution.keySet()
                .forEach(kind -> Confidence.merge(distribution2.get(kind), distribution2.get(kind), globalAggregationFunc, localAggregationFunc));

        MutableSet<String> mergedSurfaceForms = nounMapping.getSurfaceForms().toSet();
        mergedSurfaceForms.addAllIterable(nounMapping2.getSurfaceForms());

        ImmutableList<Word> mergedReferenceWords = referenceWords;

        if (mergedReferenceWords == null) {
            MutableList<Word> mergedRefWords = Lists.mutable.withAll(nounMapping.getReferenceWords());
            mergedRefWords.addAllIterable(nounMapping2.getReferenceWords());
            mergedReferenceWords = mergedRefWords.toImmutable();
        }

        String mergedReference = reference;

        if (mergedReference == null) {

            if (nounMapping.getReference().equalsIgnoreCase(nounMapping2.getReference())) {
                mergedReference = nounMapping.getReference();
            } else {
                mergedReference = textState.calculateNounMappingReference(mergedReferenceWords);
            }
        }

        MutableList<Word> mergedCoreferences = nounMapping.getCoreferences().toList();
        mergedCoreferences.addAllIterable(nounMapping2.getCoreferences());

        NounMapping mergedNounMapping = new NounMappingImpl(mergedWords.toImmutable(), mergedDistribution, mergedReferenceWords.toImmutable(),
                mergedSurfaceForms.toImmutable(), mergedReference, mergedCoreferences.toImmutable());
        mergedNounMapping.addKindWithProbability(mappingKind, claimant, probability);

        textState.removeNounMappingFromState(nounMapping);
        textState.removeNounMappingFromState(nounMapping2);

        textState.addNounMappingAddPhraseMapping(mergedNounMapping);

        return mergedNounMapping;
    }

}
