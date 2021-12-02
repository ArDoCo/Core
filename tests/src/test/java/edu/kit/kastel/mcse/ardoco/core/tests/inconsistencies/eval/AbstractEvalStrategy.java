/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval;

import java.util.HashMap;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.common.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.common.IExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.model.provider.ModelProvider;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.textextraction.GenericTextConfig;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtractionConfig;

public abstract class AbstractEvalStrategy implements IEvaluationStrategy {

    protected AbstractEvalStrategy() {
        super();
    }

    protected static AgentDatastructure runRecommendationConnectionInconsistency(AgentDatastructure data) {
        // Model Extractor has been executed & textExtractor does not depend on model changes
        data.overwrite(runRecommendationGenerator(data));
        data.overwrite(runConnectionGenerator(data));
        data.overwrite(runInconsistencyChecker(data));
        return data;
    }

    protected static IModelState runModelExtractor(IModelConnector modelConnector) {
        IExecutionStage modelExtractor = new ModelProvider(modelConnector);
        modelExtractor.exec();
        return modelExtractor.getBlackboard().getModelState();
    }

    protected static AgentDatastructure runTextExtractor(AgentDatastructure data, Map<String, String> configs) {
        IExecutionStage textModule = new TextExtraction(data);
        if (configs != null && !configs.isEmpty()) {
            textModule = textModule.create(data, configs);
        }
        textModule.exec();
        return textModule.getBlackboard();
    }

    private static AgentDatastructure runRecommendationGenerator(AgentDatastructure data) {
        IExecutionStage recommendationModule = new RecommendationGenerator(data);
        recommendationModule.exec();
        return recommendationModule.getBlackboard();
    }

    private static AgentDatastructure runConnectionGenerator(AgentDatastructure data) {
        IExecutionStage connectionGenerator = new ConnectionGenerator(data);
        connectionGenerator.exec();
        return connectionGenerator.getBlackboard();
    }

    private static AgentDatastructure runInconsistencyChecker(AgentDatastructure data) {
        IExecutionStage inconsistencyChecker = new InconsistencyChecker(data);
        inconsistencyChecker.exec();
        return inconsistencyChecker.getBlackboard();
    }

    protected static Map<String, String> getTextExtractionConfigurations(Project project) {
        Map<String, String> configurations = new HashMap<>();

        Configuration.mergeConfigToMap(configurations, TextExtractionConfig.DEFAULT_CONFIG);
        Configuration.mergeConfigToMap(configurations, GenericTextConfig.DEFAULT_CONFIG);

        // more config options would come here...

        return configurations;
    }
}
