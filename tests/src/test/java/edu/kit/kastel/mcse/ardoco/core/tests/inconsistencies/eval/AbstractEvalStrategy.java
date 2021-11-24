package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval;

import edu.kit.kastel.mcse.ardoco.core.common.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.common.IExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.model.provider.ModelProvider;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;

public abstract class AbstractEvalStrategy implements IEvaluationStrategy {

    protected AbstractEvalStrategy() {
        super();
    }

    protected static AgentDatastructure runRecommendationConnectionInconsistency(AgentDatastructure data) {
        // Model Extractor has been executed & text extractor does not depend on model
        // changes
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

    protected static AgentDatastructure runTextExtractor(AgentDatastructure data) {
        IExecutionStage textModule = new TextExtraction(data);
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
}
