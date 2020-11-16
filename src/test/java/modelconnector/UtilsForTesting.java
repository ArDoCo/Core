package modelconnector;

import java.util.Properties;

import edu.kit.ipd.indirect.textSNLP.Stanford;
import edu.kit.ipd.indirect.textSNLP.TextSNLP;
import edu.kit.ipd.indirect.tokenizer.Tokenizer;
import edu.kit.ipd.parse.graphBuilder.GraphBuilder;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.tools.ConfigManager;

/**
 * Helper for the tests. Creates with INDIRECT a PARSE graph.
 *
 * @author Sophie
 *
 */
public final class UtilsForTesting {

	private UtilsForTesting() {

	}

	/**
	 * Creates an INDIRECT (PARSE) Graph to to the given inputText
	 *
	 * @param inputText text as source for the graph
	 * @return the created PARSE Graph
	 * @throws Exception if the creation fails.
	 */
	public static IGraph getGraph(String inputText) throws Exception {
		PrePipelineData ppd = init(inputText);
		return ppd.getGraph();
	}

	private static PrePipelineData init(String input) throws Exception {

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

//		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(text, true));
		ppd.setTranscription(input);

		try {
			tokenizer.exec(ppd);
			snlp.exec(ppd);
			graphBuilder.exec(ppd);
			return ppd;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
