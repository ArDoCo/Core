package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect;

import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.kit.ipd.parse.luna.LunaInitException;
import edu.kit.ipd.parse.luna.LunaRunException;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;

public class ParseProvider implements ITextConnector {

    private static final Logger logger = LogManager.getLogger(ParseProvider.class);

    private static boolean useLUNA = false;

    private IText annotatedText;

    public ParseProvider(InputStream text) throws LunaRunException, LunaInitException {
        IPARSEExecution parse = useLUNA ? new LunaExecution() : new SingleExecution();
        IGraph graph = parse.calculatePARSEGraph(text);
        annotatedText = convertParseGraphToAnnotatedText(graph);
    }

    private IText convertParseGraphToAnnotatedText(IGraph graph) {
        if (logger.isDebugEnabled()) {
            logger.debug("Converting to IText");
        }

        ParseConverter converter = new ParseConverter(graph);
        converter.convert();

        if (logger.isDebugEnabled()) {
            logger.debug("Finished converting to IText");
        }
        return converter.getAnnotatedText();
    }

    @Override
    public IText getAnnotatedText() {
        return annotatedText;
    }

}
