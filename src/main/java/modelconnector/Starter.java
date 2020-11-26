package modelconnector;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.Scanner;

import org.apache.log4j.Logger;

import edu.kit.ipd.indirect.depparser.DepParser;
import edu.kit.ipd.indirect.textSNLP.Stanford;
import edu.kit.ipd.indirect.textSNLP.TextSNLP;
import edu.kit.ipd.indirect.tokenizer.Tokenizer;
import edu.kit.ipd.parse.graphBuilder.GraphBuilder;
import edu.kit.ipd.parse.luna.agent.AbstractAgent;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.tools.ConfigManager;
import modelconnector.connectionGenerator.ConnectionAgent;
import modelconnector.connectionGenerator.state.ConnectionState;
import modelconnector.helpers.FilesWriter;
import modelconnector.helpers.ModelConnectorConfiguration;
import modelconnector.helpers.ModelHardcoder;
import modelconnector.modelExtractor.state.ModelExtractionState;
import modelconnector.recommendationGenerator.RecommendationAgent;
import modelconnector.recommendationGenerator.state.RecommendationState;
import modelconnector.textExtractor.TextExtractionAgent;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * Class to set up and start the model connector.
 *
 * @author Sophie
 *
 */
public final class Starter {
    private static final Logger logger = Logger.getLogger(Starter.class);

    private Starter() {
        // private to disable instantiation
    }

    static {
        new File("evaluations").mkdirs();
    }
    static InputStream documentation = Starter.class.getResourceAsStream(
            ModelConnectorConfiguration.documentation_Path);
    static InputStream test = Starter.class.getResourceAsStream(ModelConnectorConfiguration.testDocumentation_Path);

    /**
     * Executes the preprocessing on the textual input. Sets the extraction state. Runs the agents of the model
     * connector and prints the results.
     *
     * @param args
     *            command line arguments
     * @throws Exception
     *             if a step fails.
     */
    public static void main(String[] args) throws Exception {
        run(documentation);
    }

    public static void runTest() throws Exception {
        run(test);
    }

    public static void runDocumentation() throws Exception {
        run(documentation);
    }

    private static void run(InputStream text) throws Exception {
        long startTime = System.currentTimeMillis();

        IGraph graph = generateIndirectGraphFromText(text);
        if (graph == null) {
            throw new IllegalArgumentException("The input is invalid and caused the graph to be null!");
        }
        runAdditionalIndirectAgentsOnGraph(graph);

        ModelExtractionState extractionState = ModelHardcoder.hardCodeExtractionStateOfTeammates();
        // ModelExtractionState extractionState =
        // ModelHardcoder.getEmptyExtractionState();

        TextExtractionAgent textExtractionAgent = new TextExtractionAgent();
        execute(graph, textExtractionAgent);
        TextExtractionState textExtractionState = textExtractionAgent.getState();

        double duration1Part = ((System.currentTimeMillis() - startTime) / 1000.0) / 60;
        FilesWriter.writeEval1ToFile(graph, textExtractionState, duration1Part);

        startTime = System.currentTimeMillis();

        RecommendationAgent recAgent = new RecommendationAgent(graph, extractionState, textExtractionState);
        execute(graph, recAgent);
        RecommendationState recommendationState = recAgent.getRecommendationState();

        double duration2Part = ((System.currentTimeMillis() - startTime) / 1000.0) / 60;
        FilesWriter.writeRecommendationsToFile(recommendationState, duration2Part);

        FilesWriter.writeRecommendedRelationToFile(recommendationState);

        startTime = System.currentTimeMillis();

        ConnectionAgent mcAgent = new ConnectionAgent(graph, extractionState, textExtractionState, recommendationState);
        execute(graph, mcAgent);
        ConnectionState connectionState = mcAgent.getConnectionState();

        double duration = (System.currentTimeMillis() - startTime) / 1000.0;
        double min = duration / 60 + duration1Part + duration2Part;

        FilesWriter.writeConnectionsToFile(connectionState, min);
        FilesWriter.writeConnectionRelationsToFile(connectionState);

        // writeSentencesInFile(graph);
        FilesWriter.writeStatesToFile(extractionState, textExtractionState, recommendationState, connectionState, min);

        return;
    }

    private static void runAdditionalIndirectAgentsOnGraph(IGraph graph) throws Exception {
        DepParser depAgent = new DepParser();
        execute(graph, depAgent);
    }

    private static IGraph generateIndirectGraphFromText(InputStream inputText) throws Exception {
        Scanner scanner = new Scanner(inputText);
        scanner.useDelimiter("\\A");
        String content = scanner.next();
        scanner.close();

        PrePipelineData ppd = init(content);
        if (ppd == null) {
            return null;
        }
        return ppd.getGraph();

    }

    /**
     * Runs the preprocessing on a given text.
     *
     * @param input
     *            input text to run on
     * @return data of the preprocessing
     * @throws Exception
     *             if a step of the preprocessing fails
     */
    private static PrePipelineData init(String input) throws Exception {

        Properties props = ConfigManager.getConfiguration(Stanford.class);
        props.setProperty("LEMMAS",
                "seconds/NNS/second;milliseconds/NNS/millisecond;hours/NNS/hour;minutes/NNS/minute;months/NNS/month;years/NNS/year");
        props.setProperty("TAGGER_MODEL",
                "/edu/stanford/nlp/models/pos-tagger/english-bidirectional/english-bidirectional-distsim.tagger");

        Tokenizer tokenizer = new Tokenizer();
        tokenizer.init();
        TextSNLP snlp = new TextSNLP();
        snlp.init();
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.init();

        PrePipelineData ppd = new PrePipelineData();

        // ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(text, true));
        ppd.setTranscription(input);

        try {
            tokenizer.exec(ppd);
            snlp.exec(ppd);
            graphBuilder.exec(ppd);
            return ppd;
        } catch (Exception e) {
            logger.debug(e.getMessage(), e.getCause());
            return null;
        }
    }

    /**
     * Runs an agent on a graph
     *
     * @param graph
     *            graph to run on
     * @param agent
     *            agent to run
     * @throws Exception
     *             if agent failes
     */
    private static void execute(IGraph graph, AbstractAgent agent) throws Exception {
        agent.init();
        agent.setGraph(graph);
        Method exec = agent.getClass()
                           .getDeclaredMethod("exec");
        exec.setAccessible(true);
        exec.invoke(agent);
    }
}
