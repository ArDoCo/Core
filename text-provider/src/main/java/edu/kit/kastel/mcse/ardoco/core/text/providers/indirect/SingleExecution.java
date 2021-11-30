/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect;


import edu.kit.ipd.parse.luna.agent.AbstractAgent;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.pipeline.PipelineStageException;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.StanfordCoreNLPProcessorAgent;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.indirect.GraphBuilder;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.indirect.TextSNLP;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.indirect.Tokenizer;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.pronat.prepipedatamodel.PrePipelineData;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Simply invoke each agent in a suitable order (once).
 *
 * @author Dominik Fuchss
 * @author Sophie Schulz
 *
 */
class SingleExecution implements IPARSEExecution {

    @Override
    public IGraph calculatePARSEGraph(InputStream text) throws LunaRunException {
        IGraph graph = generateIndirectGraphFromText(text);
        if (graph == null) {
            throw new IllegalArgumentException("The input is invalid and caused the graph to be null!");
        }
        runAdditionalIndirectAgentsOnGraph(graph);
        return graph;
    }

    private static IGraph generateIndirectGraphFromText(InputStream inputText) throws LunaRunException {
        var scanner = new Scanner(inputText, StandardCharsets.UTF_8);
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
    private static PrePipelineData init(String input) throws LunaRunException {

        var tokenizer = new Tokenizer();
        tokenizer.init();
        var snlp = new TextSNLP();
        snlp.init();
        var graphBuilder = new GraphBuilder();
        graphBuilder.init();

        var ppd = new PrePipelineData();

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

    private static void runAdditionalIndirectAgentsOnGraph(IGraph graph) throws LunaRunException {
        var stanfordAgent = new StanfordCoreNLPProcessorAgent();
        execute(graph, stanfordAgent);
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
    private static void execute(IGraph graph, AbstractAgent agent) throws LunaRunException {
        agent.init();
        agent.setGraph(graph);
        try {
            var exec = agent.getClass().getDeclaredMethod("exec");
            exec.setAccessible(true);
            exec.invoke(agent);
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            logger.warn("Error in executing agent!");
            logger.debug(e.getMessage(), e.getCause());
            throw new LunaRunException(e);
        }
    }
}
