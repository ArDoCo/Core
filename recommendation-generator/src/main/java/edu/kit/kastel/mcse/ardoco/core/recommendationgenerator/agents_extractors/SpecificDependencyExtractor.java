package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents_extractors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyType;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.Extractor;

import java.util.List;

public class SpecificDependencyExtractor extends Extractor {

    public SpecificDependencyExtractor() {
        super(DependencyType.RECOMMENDATION);
    }

    public SpecificDependencyExtractor(DependencyType dependencyType) {
        super(dependencyType);
    }

    public SpecificDependencyExtractor create(ITextState textState, IModelState modelExtractionState, IRecommendationState recommendationState,
                                              Configuration config) {
        return new SpecificDependencyExtractor(textState, modelExtractionState, recommendationState, (GenericRecommendationConfig) config);
    }

    @Override
    public Extractor create(AgentDatastructure data, Configuration config) {
        return null;
    }

    @Override
    public void exec(IWord word) {

    }

    @Override
    public void setProbability(List<Double> probabilities) {

    }
}
