/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval;

import java.util.HashMap;
import java.util.Map;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.ModelProvider;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;

public abstract class AbstractEvalStrategy implements IEvaluationStrategy {

    protected AbstractEvalStrategy() {
        super();
    }

    protected DataStructure runRecommendationConnectionInconsistency(DataStructure data) {
        Map<String, String> config = new HashMap<>();
        // Model Extractor has been executed & textExtractor does not depend on model changes
        DataRepository dataRepository = data.dataRepository();
        runRecommendationGenerator(dataRepository, config);
        runConnectionGenerator(dataRepository, config);
        runInconsistencyChecker(dataRepository, config);
        return data;
    }

    protected void runModelExtractor(DataRepository dataRepository, IModelConnector modelConnector, Map<String, String> configs) {
        ModelProvider modelExtractor = new ModelProvider(dataRepository, modelConnector);
        modelExtractor.applyConfiguration(configs);
        modelExtractor.run();
    }

    protected void runTextExtractor(DataRepository dataRepository, Map<String, String> configs) {
        var textModule = new TextExtraction(dataRepository);
        textModule.applyConfiguration(configs);
        textModule.run();
    }

    protected void runRecommendationGenerator(DataRepository dataRepository, Map<String, String> configs) {
        var recommendationModule = new RecommendationGenerator(dataRepository);
        recommendationModule.applyConfiguration(configs);
        recommendationModule.run();
    }

    protected void runConnectionGenerator(DataRepository dataRepository, Map<String, String> configs) {
        var connectionGenerator = new ConnectionGenerator(dataRepository);
        connectionGenerator.applyConfiguration(configs);
        connectionGenerator.run();
    }

    protected void runInconsistencyChecker(DataRepository dataRepository, Map<String, String> configs) {
        var inconsistencyChecker = new InconsistencyChecker(dataRepository);
        inconsistencyChecker.applyConfiguration(configs);
        inconsistencyChecker.run();
    }
}
