package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents_extractors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyType;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.*;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.DependencyExtractor;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.RecommendationExtractor;
import org.kohsuke.MetaInfServices;

import java.util.List;

@MetaInfServices(DependencyExtractor.class)
public class SpecificDependencyExtractor extends DependencyExtractor {

    public SpecificDependencyExtractor(ITextState textExtractionState, IModelState modelExtractionState, IRecommendationState recommendationState) {
        this(textExtractionState, modelExtractionState, recommendationState, GenericRecommendationConfig.DEFAULT_CONFIG);
    }

    public SpecificDependencyExtractor() {
        this(null, null, null);
    }

    public SpecificDependencyExtractor(ITextState textExtractionState, IModelState modelExtractionState, IRecommendationState recommendationState,
                             GenericRecommendationConfig config) {
        super(DependencyType.TEXT_MODEL_RECOMMENDATION, textExtractionState, modelExtractionState, recommendationState);
    }

    @Override
    public DependencyExtractor create(ITextState textState, IModelState modelExtractionState, IRecommendationState recommendationState, Configuration config) {
        return new SpecificDependencyExtractor(textState, modelExtractionState, recommendationState, (GenericRecommendationConfig) config);
    }

    @Override
    public void exec(IWord word) {
        System.out.println(word.getText());
    }

    @Override
    public void exec(INounMapping mapping) {
        System.out.println(mapping.getReference());
    }

    @Override
    public void exec(IRecommendedInstance rec) {
        System.out.println(rec.toString());
    }

    @Override
    public void setProbability(List<Double> probabilities) {

    }
}
