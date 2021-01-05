package edu.kit.ipd.consistency_analyzer.textproviders;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.Scanner;

import org.apache.log4j.Logger;

import edu.kit.ipd.consistency_analyzer.datastructures.IText;
import edu.kit.ipd.indirect.depparser.DepParser;
import edu.kit.ipd.indirect.graphBuilder.GraphBuilder;
import edu.kit.ipd.indirect.textSNLP.Stanford;
import edu.kit.ipd.indirect.textSNLP.TextSNLP;
import edu.kit.ipd.indirect.tokenizer.Tokenizer;
import edu.kit.ipd.parse.luna.agent.AbstractAgent;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.pipeline.PipelineStageException;
import edu.kit.ipd.parse.luna.tools.ConfigManager;

public class ParseProvider implements ITextConnector {

	private static final Logger LOGGER = Logger.getLogger(ParseProvider.class);
	private IText annotatedText;

	public ParseProvider(InputStream text) throws PipelineStageException, MissingDataException, NoSuchMethodException {
		IGraph graph = calculatePARSEGraph(text);
		annotatedText = convertParseGraphToAnnotatedText(graph);
	}

	private IText convertParseGraphToAnnotatedText(IGraph graph) {
		ParseConverter converter = new ParseConverter(graph);
		converter.convert();
		return converter.getAnnotatedText();
	}

	private IGraph calculatePARSEGraph(InputStream text) throws NoSuchMethodException, PipelineStageException, MissingDataException {
		IGraph graph = generateIndirectGraphFromText(text);
		if (graph == null) {
			throw new IllegalArgumentException("The input is invalid and caused the graph to be null!");
		}
		runAdditionalIndirectAgentsOnGraph(graph);
		return graph;
	}

	@Override
	public IText getAnnotatedText() {
		return annotatedText;
	}

	private IGraph generateIndirectGraphFromText(InputStream inputText) throws PipelineStageException, MissingDataException {
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
	 * @param input input text to run on
	 * @return data of the preprocessing
	 * @throws Exception if a step of the preprocessing fails
	 */
	private PrePipelineData init(String input) throws PipelineStageException {

		Properties props = ConfigManager.getConfiguration(Stanford.class);
		props.setProperty("LEMMAS", "seconds/NNS/second;milliseconds/NNS/millisecond;hours/NNS/hour;minutes/NNS/minute;months/NNS/month;years/NNS/year");
		props.setProperty("TAGGER_MODEL", "/edu/stanford/nlp/models/pos-tagger/english-bidirectional/english-bidirectional-distsim.tagger");

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
			LOGGER.debug(e.getMessage(), e.getCause());
			return null;
		}
		return ppd;
	}

	private void runAdditionalIndirectAgentsOnGraph(IGraph graph) throws NoSuchMethodException {
		DepParser depAgent = new DepParser();
		execute(graph, depAgent);
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
	private void execute(IGraph graph, AbstractAgent agent) throws NoSuchMethodException {
		agent.init();
		agent.setGraph(graph);
		Method exec = agent.getClass().getDeclaredMethod("exec");
		try {
			exec.invoke(agent);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			LOGGER.warn("Error in executing agent!");
			LOGGER.debug(e.getMessage(), e.getCause());
		}
	}

}
