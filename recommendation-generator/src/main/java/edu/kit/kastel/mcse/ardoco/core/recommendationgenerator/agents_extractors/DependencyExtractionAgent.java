package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents_extractors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.*;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.IExtractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DependencyExtractionAgent extends RecommendationAgent {
    private List<IExtractor> extractors = new ArrayList<>();

    public DependencyExtractionAgent() { super(GenericRecommendationConfig.class); }
    public DependencyExtractionAgent(Class<? extends Configuration> configType) { super(configType); }

    public DependencyExtractionAgent(DependencyType dependencyType,
                                     Class<? extends Configuration> configType,
                                     IText text,
                                     ITextState textState,
                                     IModelState modelState,
                                     IRecommendationState recommendationState) {
        super(dependencyType, configType, text, textState, modelState, recommendationState);

    }

    private void initializeExtractors(List<String> extractorList, GenericRecommendationConfig config) {
        Map<String, SpecificDependencyExtractor> loadedExtractors = Loader.loadLoadable(SpecificDependencyExtractor.class);

        for (String recommendationExtractor : extractorList) {
            if (!loadedExtractors.containsKey(recommendationExtractor)) {
                throw new IllegalArgumentException("RecommendationExtractor " + recommendationExtractor + " not found");
            }
            extractors.add(loadedExtractors.get(recommendationExtractor).create(textState, modelState, recommendationState, config));
        }
    }

    @Override
    public void exec() {
        DependencyType dt = super.getDependencyType();
        logger.info("HELLO Dependency " + dt.name());
    }

    @Override
    public RecommendationAgent create(IText text, ITextState textState, IModelState modelState,
                                      IRecommendationState recommendationState, Configuration config) {
        return new DependencyExtractionAgent(DependencyType.RECOMMENDATION, GenericRecommendationConfig.class,
                                               text, textState, modelState, recommendationState);
    }
}
