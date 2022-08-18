/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.Confidence;
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

    protected final Confidence putAllConfidencesTogether(Confidence confidence, Confidence confidence1) {

        Confidence result = confidence.createCopy();
        result.addAllConfidences(confidence1);
        return result;
    }
}
