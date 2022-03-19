/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;

import java.util.Map;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.common.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.ICorefCluster;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.POSTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;

public class CorefAgent extends TextAgent {

    @Configurable
    private boolean enabled = false;
    private static boolean doMerging = false;

    /**
     * Prototype constructor.
     */
    public CorefAgent() {
    }

    @Override
    public void execute(TextAgentData data) {
        if (!enabled) {
            return;
        }

        var text = data.getText();
        var textState = data.getTextState();
        var corefClusters = text.getCorefClusters();

        for (var corefCluster : corefClusters) {
            var words = getAllWordsFromCorefCluster(corefCluster);

            MutableSet<INounMapping> nounMappings = getNounMappingsForWordsFromCorefClusters(words, textState);
            addWordsToNounMappingsAsCoreferences(nounMappings, words);

            if (doMerging && nounMappings.size() > 1) {
                if (logger.isDebugEnabled()) {
                    logger.debug("MORE THAN 1 FOR {}", corefCluster.representativeMention());
                }
                mergeNounMappings(nounMappings, textState);
            }
        }
    }

    private static void mergeNounMappings(MutableSet<INounMapping> nounMappings, ITextState textState) {
        INounMapping mergedNounMapping = null;
        for (var nounMapping : nounMappings) {
            mergedNounMapping = nounMapping.merge(mergedNounMapping);
            textState.removeNounMapping(nounMapping);
        }
        textState.addNounMapping(mergedNounMapping);
    }

    private static void addWordsToNounMappingsAsCoreferences(MutableSet<INounMapping> nounMappings, ImmutableList<IWord> words) {
        // add words to noun mappings as coreferences
        // only add pronoun-related coreferences for now
        for (var nounMapping : nounMappings) {
            for (var word : words) {
                if (word.getPosTag() == POSTag.PRONOUN_PERSONAL || word.getPosTag() == POSTag.PRONOUN_POSSESSIVE) {
                    nounMapping.addCoreference(word);
                }
            }
        }
    }

    private static MutableSet<INounMapping> getNounMappingsForWordsFromCorefClusters(ImmutableList<IWord> words, ITextState textState) {
        MutableSet<INounMapping> nounMappings = Sets.mutable.empty();
        for (var word : words) {
            var nounMappingsForWord = textState.getNounMappingsByWord(word).castToCollection();
            nounMappings.addAll(nounMappingsForWord);
        }
        return nounMappings;
    }

    private static ImmutableList<IWord> getAllWordsFromCorefCluster(ICorefCluster corefCluster) {
        return corefCluster.mentions().flatCollect(mention -> mention);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
    }
}
