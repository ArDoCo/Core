/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.function.Function;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.common.util.ElementWrapper;

// TODO Document code
public interface TextStateStrategy {

    NounMapping addOrExtendNounMapping(Word word, MappingKind kind, Claimant claimant, double probability, ImmutableList<String> surfaceForms);

    ElementWrapper<NounMapping> wrap(NounMapping nounMapping);

    NounMapping mergeNounMappings(NounMapping firstNounMapping, NounMapping secondNounMapping, ImmutableList<Word> referenceWords, String reference,
            MappingKind mappingKind, Claimant claimant, double probability);

    Function<TextStateImpl, TextStateStrategy> creator();
}
