/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;


import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.text.ICorefCluster;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.text.POSTag;
import edu.kit.kastel.mcse.ardoco.core.textextraction.GenericTextConfig;
import edu.kit.kastel.mcse.ardoco.core.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextAgent;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.MutableSet;

public class CorefAgent extends TextAgent {

    private boolean enabled;
    private static boolean doMerging = false;

    /**
     * Prototype constructor.
     */
    public CorefAgent() {
        super(GenericTextConfig.class);
    }

    private CorefAgent(IText text, ITextState textState, GenericTextConfig config) {
        super(GenericTextConfig.class, text, textState);
        enabled = config.corefEnable;
    }

    @Override
    public TextAgent create(IText text, ITextState textState, Configuration config) {
        return new CorefAgent(text, textState, (GenericTextConfig) config);
    }

    @Override
    public void exec() {
        if (!enabled) {
            return;
        }
        var corefClusters = text.getCorefClusters();

        for (var corefCluster : corefClusters) {
            var words = getAllWordsFromCorefCluster(corefCluster);

            MutableSet<INounMapping> nounMappings = addWordsFromCorefClusterToNounMappings(words, textState);
            addWordsToNounMappingsAsCoreferences(nounMappings, words);

            if (doMerging && nounMappings.size() > 1) {
                logger.debug("MORE THAN 1 FOR {}", corefCluster.getRepresentativeMention());
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

    private static MutableSet<INounMapping> addWordsFromCorefClusterToNounMappings(ImmutableList<IWord> words, ITextState textState) {
        MutableSet<INounMapping> nounMappings = Sets.mutable.empty();
        for (var word : words) {
            var nounMappingsForWord = textState.getNounMappingsByWord(word).castToCollection();
            nounMappings.addAll(nounMappingsForWord);
        }
        return nounMappings;
    }

    private static ImmutableList<IWord> getAllWordsFromCorefCluster(ICorefCluster corefCluster) {
        return corefCluster.getMentions().flatCollect(mention -> mention);
    }

}
