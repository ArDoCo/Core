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
import edu.kit.kastel.mcse.ardoco.core.textextractor.GenericTextConfig;

public class CorefAgent extends TextAgent {

    /**
     * Prototype constructor.
     */
    public CorefAgent() {
        super(GenericTextConfig.class);
    }

    private CorefAgent(IText text, ITextState textState, GenericTextConfig config) {
        super(GenericTextConfig.class, text, textState);
        // TODO config?
    }

    @Override
    public TextAgent create(IText text, ITextState textState, Configuration config) {
        return new CorefAgent(text, textState, (GenericTextConfig) config);
    }

    @Override
    public void exec() {
        var corefClusters = text.getCorefClusters();
        if (logger.isDebugEnabled()) {
            logCorefClusters(corefClusters);
        }

        // add words of coreferences to the noun mappings that they belong
        // so for each cluster, search for one or more NounMappings that contain the mentions
        // add the mentions to the NounMappings
        // if more than one NounMapping is found, merge (?) them

        for (var corefCluster : corefClusters) {
            MutableSet<INounMapping> nounMappings = Sets.mutable.empty();
            var words = getAllWordsFromCorefCluster(corefCluster);
            for (var word : words) {
                var nounMappingsForWord = textState.getNounMappingsByWord(word).castToCollection();
                nounMappings.addAll(nounMappingsForWord);
            }

            if (!nounMappings.isEmpty() && logger.isDebugEnabled()) {
                logger.debug("Found noun mappings for CorefCluster {}", corefCluster.getRepresentativeMention());
            }

            // TODO add mentions (or all words of them) to nounMappings
            for (var nounMapping : nounMappings) {
                // nounMapping.addWords(words);
                // Current problem: words are used in so many places for nounMappings
                // maybe add coreferences in some other fashion? Or change the use of nounmappings?
                logger.debug("  - {}", nounMapping.getReference());
            }

            // TODO merge NounMappings?
        }

    }

    private static ImmutableList<IWord> getAllWordsFromCorefCluster(ICorefCluster corefCluster) {
        return corefCluster.getMentions().flatCollect(mention -> mention);
    }

    private void logCorefClusters(ImmutableList<ICorefCluster> corefClusters) {
        for (var corefCluster : corefClusters) {
            logger.debug("Coref cluster with id {} and representative mention {}", corefCluster.getId(), corefCluster.getRepresentativeMention());
            for (var mention : corefCluster.getMentions()) {
                var firstPosition = mention.getFirst().getPosition();
                var lastPosition = mention.getLast().getPosition();
                var mentionText = ICorefCluster.getTextForMention(mention);
                logger.debug("  Mention: {} ({} - {})", mentionText, firstPosition, lastPosition);
            }
        }
    }
}
