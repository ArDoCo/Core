package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.Scanner;

import edu.kit.ipd.indirect.depparser.DepParser;
import edu.kit.ipd.indirect.graphBuilder.GraphBuilder;
import edu.kit.ipd.indirect.textSNLP.Stanford;
import edu.kit.ipd.indirect.textSNLP.TextSNLP;
import edu.kit.ipd.indirect.tokenizer.Tokenizer;
import edu.kit.ipd.parse.luna.LunaRunException;
import edu.kit.ipd.parse.luna.agent.AbstractAgent;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.pipeline.PipelineStageException;
import edu.kit.ipd.parse.luna.tools.ConfigManager;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.CoreferenceResolution;

/**
 * Simply invoke each agent in a suitable order (once).
 *
 * @author Dominik Fuchss
 * @author Sophie Schulz
 *
 */
class SingleExecution implements IPARSEExecution {

    private static final String TAGGER_MODEL = "TAGGER_MODEL";
    private static final String TAGGER_MODEL_PROPERTY = "/edu/stanford/nlp/models/pos-tagger/english-bidirectional/english-bidirectional-distsim.tagger";
    private static final String LEMMAS = "LEMMAS";
    private static final String LEMMAS_PROPERTIES = "seconds/NNS/second;milliseconds/NNS/millisecond;hours/NNS/hour;minutes/NNS/minute;months/NNS/month;years/NNS/year";

    @Override
    public IGraph calculatePARSEGraph(InputStream text) throws LunaRunException {
        IGraph graph = generateIndirectGraphFromText(text);
        if (graph == null) {
            throw new IllegalArgumentException("The input is invalid and caused the graph to be null!");
        }
        runAdditionalIndirectAgentsOnGraph(graph);
        return graph;
    }

    private IGraph generateIndirectGraphFromText(InputStream inputText) throws LunaRunException {
        Scanner scanner = new Scanner(inputText);
        scanner.useDelimiter("\\A");
        String content = scanner.next();
        scanner.close();

        PrePipelineData ppd = init(content);

        try {
            return ppd.getGraph();
        } catch (MissingDataException e) {
            throw new LunaRunException(e);
        }

    }

    /**
     * Runs the preprocessing on a given text.
     *
     * @param input input text to run on
     * @return data of the preprocessing
     * @throws Exception if a step of the preprocessing fails
     */
    private PrePipelineData init(String input) throws LunaRunException {

        Properties props = ConfigManager.getConfiguration(Stanford.class);
        props.setProperty(LEMMAS, LEMMAS_PROPERTIES);
        props.setProperty(TAGGER_MODEL, TAGGER_MODEL_PROPERTY);

        Tokenizer tokenizer = new Tokenizer();
        tokenizer.init();
        TextSNLP snlp = new TextSNLP();
        snlp.init();
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.init();

        PrePipelineData ppd = new PrePipelineData();

        ppd.setTranscription(input);

        try {
            tokenizer.exec(ppd);
            snlp.exec(ppd);
            graphBuilder.exec(ppd);
        } catch (PipelineStageException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage(), e.getCause());
            }
            throw new LunaRunException(e);
        }

        return ppd;
    }

    private void runAdditionalIndirectAgentsOnGraph(IGraph graph) throws LunaRunException {
        DepParser depAgent = new DepParser();
        execute(graph, depAgent);

        CoreferenceResolution coref = new CoreferenceResolution();
        execute(graph, coref);
    }

    /**
     * Runs an agent on a graph
     *
     * @param graph graph to run on
     * @param agent agent to run
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws Exception             if agent fails
     */
    private void execute(IGraph graph, AbstractAgent agent) throws LunaRunException {
        agent.init();
        agent.setGraph(graph);
        try {
            Method exec = agent.getClass().getDeclaredMethod("exec");
            exec.setAccessible(true);
            exec.invoke(agent);
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            logger.warn("Error in executing agent!");
            logger.debug(e.getMessage(), e.getCause());
            throw new LunaRunException(e);
        }
    }
}
