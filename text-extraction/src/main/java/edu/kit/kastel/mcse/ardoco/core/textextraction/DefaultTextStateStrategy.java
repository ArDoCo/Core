package edu.kit.kastel.mcse.ardoco.core.textextraction;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.informalin.framework.common.AggregationFunctions;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;

public abstract class DefaultTextStateStrategy implements TextStateStrategy {

    private TextStateImpl textState;

    public void setTextState(TextStateImpl textExtractionState) {
        textState = textExtractionState;
    }

    public TextStateImpl getTextState() {
        return textState;
    }

    public NounMapping mergeNounMappings(NounMapping nounMapping, MutableList<NounMapping> nounMappingsToMerge, Claimant claimant) {
        /*
         * if (!textState.getNounMappings().contains(nounMapping)) { throw new
         * IllegalStateException("The text state should contain the noun mappings that are merged!"); }
         */

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

        return nounMapping;
    }

    public NounMapping mergeNounMappings(NounMapping nounMapping, NounMapping nounMapping2, ImmutableList<Word> referenceWords, String reference,
            MappingKind mappingKind, Claimant claimant, double probability) {
        /*
         * if (!textState.getNounMappings().contains(nounMapping) ||
         * !textState.getNounMappings().contains(nounMapping2)) { throw new
         * IllegalArgumentException("The noun mappings that are merged should be in the current text state!"); }
         */

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
