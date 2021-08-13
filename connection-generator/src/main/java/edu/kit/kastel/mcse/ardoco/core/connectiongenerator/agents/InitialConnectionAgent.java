package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents;

import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.GenericConnectionConfig;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.ConnectionAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Loader;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.ConnectionExtractor;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.IExtractor;

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
