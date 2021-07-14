package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents_extractors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyType;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Loader;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.*;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.DependencyExtractor;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.IExtractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DependencyExtractionAgent extends DependencyAgent {
    private List<DependencyExtractor> extractors = new ArrayList<>();

    public DependencyExtractionAgent() {
        super(GenericRecommendationConfig.class);
    }

    public DependencyExtractionAgent(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
                                     GenericRecommendationConfig config) {
        super(DependencyType.TEXT_MODEL_RECOMMENDATION, GenericRecommendationConfig.class, text, textState, modelState, recommendationState);
        initializeExtractors(config.dependencyExtractors, config);
    }

    private void initializeExtractors(List<String> extractorList, GenericRecommendationConfig config) {
        Map<String, DependencyExtractor> loadedExtractors = Loader.loadLoadable(DependencyExtractor.class);

        for (String dependencyExtractor : extractorList) {
            if (!loadedExtractors.containsKey(dependencyExtractor)) {
                throw new IllegalArgumentException("DependencyExtractor " + dependencyExtractor + " not found");
            }
            extractors.add(loadedExtractors.get(dependencyExtractor).create(textState, modelState, recommendationState, config));
        }
    }

    @Override
    public DependencyExtractionAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState, Configuration config) {
        return new DependencyExtractionAgent(text, textState, modelState, recommendationState, (GenericRecommendationConfig) config);
    }

    @Override
    public void exec() {
        DependencyType dt = super.getDependencyType();
        logger.info("HELLO Dependency " + dt.name() + " - DependencyExtractionAgent");
        for (IWord word : text.getWords()) {
            System.out.print(word.getText() + " ");
        }
        System.out.println();

        for (DependencyExtractor extractor : extractors) {
            for (IRecommendedInstance rec : recommendationState.getRecommendedInstances()) {
                extractor.exec(rec);
            }
        }
    }
}
