package edu.kit.kastel.mcse.ardoco.core.textextractor.agents;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.TextAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ICorefCluster;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.POSTag;
import edu.kit.kastel.mcse.ardoco.core.textextractor.GenericTextConfig;

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
            logger.info("Coref-Resolution is disabled in the config. This is usually due to the rather bad performance of the resolution "
                    + "that slightly increases recall but often decreases precision by a lot.");
            return;
        }
        var corefClusters = text.getCorefClusters();

        for (var corefCluster : corefClusters) {
            MutableSet<INounMapping> nounMappings = Sets.mutable.empty();
            var words = getAllWordsFromCorefCluster(corefCluster);
            for (var word : words) {
                var nounMappingsForWord = textState.getNounMappingsByWord(word).castToCollection();
                nounMappings.addAll(nounMappingsForWord);
            }

            // add words to noun mappings as coreferences
            // only add pronoun-related coreferences for now
            for (var nounMapping : nounMappings) {
                for (var word : words) {
                    if (word.getPosTag() == POSTag.PRONOUN_PERSONAL || word.getPosTag() == POSTag.PRONOUN_POSSESSIVE) {
                        nounMapping.addCoreference(word);
                    }
                }
            }

            if (doMerging && nounMappings.size() > 1) {
                logger.debug("MORE THAN 1 FOR {}", corefCluster.getRepresentativeMention());
                INounMapping mergedNounMapping = null;
                for (var nounMapping : nounMappings) {
                    mergedNounMapping = nounMapping.merge(mergedNounMapping);
                    textState.removeNounNode(nounMapping);
                }
                textState.addNounMapping(mergedNounMapping);
            }
        }
    }

    private static ImmutableList<IWord> getAllWordsFromCorefCluster(ICorefCluster corefCluster) {
        return corefCluster.getMentions().flatCollect(mention -> mention);
    }

}
