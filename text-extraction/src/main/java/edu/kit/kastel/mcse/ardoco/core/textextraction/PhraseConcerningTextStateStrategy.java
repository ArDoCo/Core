package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.common.util.ElementWrapper;

public class PhraseConcerningTextStateStrategy extends DefaultTextStateStrategy {

    private static final Function<NounMapping, Integer> NOUN_MAPPING_HASH = nm -> Objects.hash(nm.getReference(), nm.getPhrases());
    private static final BiPredicate<NounMapping, NounMapping> NOUN_MAPPING_EQUALS = (nm1,
            nm2) -> (Objects.equals(nm1.getPhrases(), nm2.getPhrases()) && Objects.equals(nm1.getReference(), nm2.getReference()));

    PhraseConcerningTextStateStrategy(TextStateImpl textState) {
        super.setTextState(textState);
    }

    public ElementWrapper<NounMapping> wrap(NounMapping nounMapping) {
        return new ElementWrapper<>(NounMapping.class, nounMapping, NOUN_MAPPING_HASH, NOUN_MAPPING_EQUALS);
    }

    @Override
    public NounMapping addOrExtendNounMapping(Word word, MappingKind kind, Claimant claimant, double probability, ImmutableSet<String> surfaceForms) {
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
            surfaceForms = Sets.immutable.with(word.getText());
        }
        NounMapping nounMapping = new NounMappingImpl(words, kind, claimant, probability, words.toImmutableList(), surfaceForms);
        super.getTextState().addNounMappingAddPhraseMapping(nounMapping);
        return nounMapping;
    }

}
