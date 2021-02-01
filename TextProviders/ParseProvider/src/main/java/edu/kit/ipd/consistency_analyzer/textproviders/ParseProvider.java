package edu.kit.ipd.consistency_analyzer.textproviders;

import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.kit.ipd.consistency_analyzer.datastructures.IText;
import edu.kit.ipd.indirect.textSNLP.Stanford;
import edu.kit.ipd.parse.changeWD.ChangeWatchdog;
import edu.kit.ipd.parse.luna.Luna;
import edu.kit.ipd.parse.luna.LunaRunException;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.tools.ConfigManager;

public class ParseProvider implements ITextConnector {

    private static final Logger logger = LogManager.getLogger(ParseProvider.class);

    private IText annotatedText;

    public ParseProvider(InputStream text) throws LunaRunException {
        IGraph graph = calculatePARSEGraph(text);
        annotatedText = convertParseGraphToAnnotatedText(graph);
    }

    private IText convertParseGraphToAnnotatedText(IGraph graph) {
        ParseConverter converter = new ParseConverter(graph);
        converter.convert();
        return converter.getAnnotatedText();
    }

    private IGraph calculatePARSEGraph(InputStream text) throws LunaRunException {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting creation of PARSE Graph");
        }

        Scanner scanner = new Scanner(text);
        scanner.useDelimiter("\\A");
        String content = scanner.next();
        scanner.close();

        Properties stanfordProps = ConfigManager.getConfiguration(Stanford.class);
        stanfordProps.setProperty("LEMMAS",
                "seconds/NNS/second;milliseconds/NNS/millisecond;hours/NNS/hour;minutes/NNS/minute;months/NNS/month;years/NNS/year");
        stanfordProps.setProperty("TAGGER_MODEL", "/edu/stanford/nlp/models/pos-tagger/english-bidirectional/english-bidirectional-distsim.tagger");

        Properties changeWatchdogProps = ConfigManager.getConfiguration(ChangeWatchdog.class);
        // TODO Find a suitable time for termination. Currently 10s
        changeWatchdogProps.setProperty("CHANGE_TIMEOUT_THRESHOLD", "10000");

        Properties lunaProps = ConfigManager.getConfiguration(Luna.class);
        lunaProps.setProperty("PRE_PIPE", String.join(",", "indirect_tokenizer", "textSNLP", "graphBuilder"));
        lunaProps.setProperty("AGENTS", String.join(",", "depParser", "changeWatchdog"));

        Luna luna = Luna.getInstance();
        luna.getPrePipelineData().setTranscription(content);

        luna.init();
        luna.run();

        if (logger.isDebugEnabled()) {
            logger.debug("Finished creation of PARSE Graph");
        }
        return luna.getMainGraph();

    }

    @Override
    public IText getAnnotatedText() {
        return annotatedText;
    }

}
