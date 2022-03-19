/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval;

import java.util.HashMap;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.IExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.provider.ModelProvider;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;

public abstract class AbstractEvalStrategy implements IEvaluationStrategy {

    protected AbstractEvalStrategy() {
        super();
    }

    protected static DataStructure runRecommendationConnectionInconsistency(DataStructure data) {
        Map<String, String> config = new HashMap<>();
        // Model Extractor has been executed & textExtractor does not depend on model changes
        runRecommendationGenerator(data, config);
        runConnectionGenerator(data, config);
        runInconsistencyChecker(data, config);
        return data;
    }

    protected static IModelState runModelExtractor(IModelConnector modelConnector) {
        ModelProvider modelExtractor = new ModelProvider(modelConnector);
        return modelExtractor.execute();
    }

    protected static DataStructure runTextExtractor(DataStructure data, Map<String, String> configs) {
        IExecutionStage textModule = new TextExtraction();
        textModule.execute(data, configs);
        return data;
    }

    private static DataStructure runRecommendationGenerator(DataStructure data, Map<String, String> configs) {
        IExecutionStage recommendationModule = new RecommendationGenerator();
        recommendationModule.execute(data, configs);
        return data;
    }

    private static DataStructure runConnectionGenerator(DataStructure data, Map<String, String> configs) {
        IExecutionStage connectionGenerator = new ConnectionGenerator();
        connectionGenerator.execute(data, configs);
        return data;
    }

    private static DataStructure runInconsistencyChecker(DataStructure data, Map<String, String> configs) {
        IExecutionStage inconsistencyChecker = new InconsistencyChecker();
        inconsistencyChecker.execute(data, configs);
        return data;
    }
}
