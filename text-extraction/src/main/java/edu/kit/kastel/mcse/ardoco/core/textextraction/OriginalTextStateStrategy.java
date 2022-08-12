package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.common.util.ElementWrapper;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

public class OriginalTextStateStrategy extends DefaultTextStateStrategy {

    private static final Function<NounMapping, Integer> NOUN_MAPPING_HASH = nm -> Objects.hash(nm.getReferenceWords().toSet(), nm.getWords());
    private static final BiPredicate<NounMapping, NounMapping> NOUN_MAPPING_EQUALS = (nm1,
            nm2) -> Objects.equals(nm1.getReferenceWords().toSet(), nm2.getReferenceWords().toSet()) && Objects.equals(nm1.getWords(), nm2.getWords());

    OriginalTextStateStrategy(TextStateImpl textState) {
        super.setTextState(textState);
    }

    @Override
    public NounMapping addOrExtendNounMapping(Word word, MappingKind kind, Claimant claimant, double probability, ImmutableSet<String> surfaceForms) {

        NounMapping nounMapping = new NounMappingImpl(Sets.immutable.with(word), kind, claimant, probability, Lists.immutable.with(word), surfaceForms);

        for (var existingNounMapping : super.getTextState().getNounMappings()) {
            if (SimilarityUtils.areNounMappingsSimilar(nounMapping, existingNounMapping)) {

                return mergeNounMappings(nounMapping, existingNounMapping, nounMapping.getReferenceWords(), nounMapping.getReference(), nounMapping.getKind(),
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
}
