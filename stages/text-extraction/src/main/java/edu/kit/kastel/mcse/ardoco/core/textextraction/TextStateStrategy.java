/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.function.Function;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.common.util.ElementWrapper;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * The Interface for strategies for the text state.
 */
public interface TextStateStrategy {

    NounMapping addOrExtendNounMapping(Word word, MappingKind kind, Claimant claimant, double probability, ImmutableList<String> surfaceForms);

    ElementWrapper<NounMapping> wrap(NounMapping nounMapping);

    NounMapping mergeNounMappings(NounMapping firstNounMapping, NounMapping secondNounMapping, ImmutableList<Word> referenceWords, String reference,
            MappingKind mappingKind, Claimant claimant, double probability);

    Function<TextStateImpl, TextStateStrategy> creator();
}
