package edu.kit.kastel.mcse.ardoco.core.textextraction;

import org.eclipse.collections.api.set.ImmutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.common.util.ElementWrapper;

public interface TextStateStrategy {

    NounMapping addOrExtendNounMapping(Word word, MappingKind kind, Claimant claimant, double probability, ImmutableSet<String> surfaceForms);

    ElementWrapper<NounMapping> wrap(NounMapping nounMapping);

}
