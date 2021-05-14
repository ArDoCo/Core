package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents_extractors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.ConnectionAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyType;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Loader;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.ConnectionExtractor;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.IExtractor;

@MetaInfServices(ConnectionAgent.class)
public class InitialConnectionAgent extends ConnectionAgent {

    private List<IExtractor> extractors = new ArrayList<>();

    public InitialConnectionAgent() {
        super(GenericConnectionConfig.class);
    }

    private InitialConnectionAgent(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, GenericConnectionConfig config) {
        super(DependencyType.TEXT_MODEL_RECOMMENDATION, GenericConnectionConfig.class, text, textState, modelState, recommendationState, connectionState);
        initializeAgents(config.connectionExtractors, config);
    }

    private void initializeAgents(List<String> extractorList, GenericConnectionConfig config) {
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
