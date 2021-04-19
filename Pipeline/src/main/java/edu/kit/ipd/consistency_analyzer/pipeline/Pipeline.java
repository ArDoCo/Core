package edu.kit.ipd.consistency_analyzer.pipeline;

import java.io.InputStream;
import java.time.Duration;

import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.AgentDatastructure;
import edu.kit.ipd.consistency_analyzer.datastructures.IModelState;
import edu.kit.ipd.consistency_analyzer.datastructures.IText;
import edu.kit.ipd.consistency_analyzer.modelproviders.IModelConnector;
import edu.kit.ipd.consistency_analyzer.modelproviders.exception.InconsistentModelException;
import edu.kit.ipd.consistency_analyzer.modelproviders.pcm.PcmOntologyModelConnector;
import edu.kit.ipd.consistency_analyzer.modules.ConnectionGenerator;
import edu.kit.ipd.consistency_analyzer.modules.IModule;
import edu.kit.ipd.consistency_analyzer.modules.ModelExtractor;
import edu.kit.ipd.consistency_analyzer.modules.RecommendationGenerator;
import edu.kit.ipd.consistency_analyzer.modules.TextExtractor;
import edu.kit.ipd.consistency_analyzer.pipeline.helpers.FilePrinter;
import edu.kit.ipd.consistency_analyzer.textproviders.ITextConnector;
import edu.kit.ipd.consistency_analyzer.textproviders.ParseProvider;
import edu.kit.ipd.parse.luna.LunaRunException;

public class Pipeline {

    public static void main(String[] args) throws LunaRunException {
        InputStream documentation = Pipeline.class.getResourceAsStream(PipelineConfig.DOCUMENTATION_PATH);
        // InputStream test = Pipeline.class.getResourceAsStream(PipelineConfig.TEST_DOCUMENTATION_PATH);
        run(documentation);
    }

    public static void run(InputStream text) throws LunaRunException {
        long startTime = System.currentTimeMillis();

        ITextConnector connector = new ParseProvider(text);
        IText annotatedText = connector.getAnnotatedText();

        // HardCodedModelInput hardCodedModel = new HardCodedModelInput();
        // IModelConnector modelConnector = new HardCodedModelConnector(hardCodedModel);
        IModelConnector pcmTeammatesModel = new PcmOntologyModelConnector("src/main/resources/teammates.owl");
        FilePrinter.writeModelInstancesInCsvFile(null, runModelExtractor(pcmTeammatesModel), "Teammates");

        IModelConnector mediaStoreModel = new PcmOntologyModelConnector("src/main/resources/mediastore.owl");
        FilePrinter.writeModelInstancesInCsvFile(null, runModelExtractor(mediaStoreModel), "MediaStore");

        IModelConnector teaStoreModel = new PcmOntologyModelConnector("src/main/resources/TeaStore.owl");
        FilePrinter.writeModelInstancesInCsvFile(null, runModelExtractor(teaStoreModel), "TeaStore");

        AgentDatastructure data = new AgentDatastructure(annotatedText, null, runModelExtractor(pcmTeammatesModel), null, null);
        data.overwrite(runTextExtractor(data));
        data.overwrite(runRecommendationGenerator(data));
        data.overwrite(runConnectionGenerator(data));

        Duration duration = Duration.ofMillis(System.currentTimeMillis() - startTime);

        printResultsInFiles(data, duration);
    }

    private static void printResultsInFiles(//
            AgentDatastructure data, Duration duration) {

        // FilePrinter.writeEval1ToFile(text, textExtractionState, 0);
        // FilePrinter.writeRecommendationsToFile(recommendationState, 0);
        // FilePrinter.writeRecommendedRelationToFile(recommendationState);
        // FilePrinter.writeConnectionsToFile(connectionState, 0);
        // FilePrinter.writeConnectionRelationsToFile(connectionState);

        FilePrinter.writeStatesToFile(data.getModelState(), data.getTextState(), data.getRecommendationState(), data.getConnectionState(), duration);
        FilePrinter.writeNounMappingsInCsvFile(null, data.getTextState());
        FilePrinter.writeTraceLinksInCsvFile(null, data.getConnectionState());

    }

    private static IModelState runModelExtractor(IModelConnector modelConnector) throws InconsistentModelException {
        IModule<IModelState> hardCodedModelExtractor = new ModelExtractor(modelConnector);
        hardCodedModelExtractor.exec();
        return hardCodedModelExtractor.getState();
    }

    private static AgentDatastructure runTextExtractor(AgentDatastructure data) {
        IModule<AgentDatastructure> textModule = new TextExtractor(data);
        textModule.exec();
        return textModule.getState();
    }

    private static AgentDatastructure runRecommendationGenerator(AgentDatastructure data) {
        IModule<AgentDatastructure> recommendationModule = new RecommendationGenerator(data);
        recommendationModule.exec();
        return recommendationModule.getState();
    }

    private static AgentDatastructure runConnectionGenerator(AgentDatastructure data) {
        IModule<AgentDatastructure> mcAgent = new ConnectionGenerator(data);
        mcAgent.exec();
        return mcAgent.getState();
    }

}
