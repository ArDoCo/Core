package edu.kit.kastel.mcse.ardoco.core.textextractor.agents;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.TextAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
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
        // TODO Auto-generated method stub
        logger.info("Executing CoRef Agent");

        var corefClusters = text.getCorefClusters();

        if (logger.isDebugEnabled()) {
            for (var corefCluster : corefClusters) {
                logger.debug("Coref cluster with id {} and representative mention {}", corefCluster.getId(), corefCluster.getRepresentativeMention());
                for (var mention : corefCluster.getMentions()) {
                    var firstPosition = mention.getFirst().getPosition();
                    var lastPosition = 0;
                    var textBuilder = new StringBuilder();
                    for (var word : mention) {
                        lastPosition = word.getPosition();
                        textBuilder.append(word.getText());
                        textBuilder.append(" ");
                    }
                    var mentionText = textBuilder.toString().strip();
                    logger.debug("  Mention: {} ({} - {})", mentionText, firstPosition, lastPosition);
                }
            }
        }

    }

}
