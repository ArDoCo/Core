/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents;


import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.common.IExtractor;
import edu.kit.kastel.mcse.ardoco.core.common.Loader;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionAgent;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionExtractor;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.GenericConnectionConfig;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;
import java.util.Map;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.kohsuke.MetaInfServices;

/**
 * The agent that executes the extractors of this stage.
 */
@MetaInfServices(ConnectionAgent.class)
public class InitialConnectionAgent extends ConnectionAgent {

    private MutableList<IExtractor> extractors = Lists.mutable.empty();

    /**
     * Create the agent.
     */
    public InitialConnectionAgent() {
        super(GenericConnectionConfig.class);
    }

    private InitialConnectionAgent(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, GenericConnectionConfig config) {
        super(GenericConnectionConfig.class, text, textState, modelState, recommendationState, connectionState);
        initializeAgents(config.connectionExtractors, config);
    }

    private void initializeAgents(ImmutableList<String> extractorList, GenericConnectionConfig config) {
        Map<String, ConnectionExtractor> loadedExtractors = Loader.loadLoadable(ConnectionExtractor.class);

        for (String connectionExtractor : extractorList) {
            if (!loadedExtractors.containsKey(connectionExtractor)) {
                throw new IllegalArgumentException("ConnectionExtractor " + connectionExtractor + " not found");
            }
            extractors.add(loadedExtractors.get(connectionExtractor).create(textState, modelState, recommendationState, connectionState, config));
        }

    }

    @Override
    public ConnectionAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, Configuration config) {
        return new InitialConnectionAgent(text, textState, modelState, recommendationState, connectionState, (GenericConnectionConfig) config);
    }

    @Override
    public void exec() {
        for (IExtractor extractor : extractors) {
            for (IWord word : text.getWords()) {
                extractor.exec(word);
            }
        }
    }
}
