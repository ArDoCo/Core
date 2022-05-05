/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.ipd.parse.luna.LunaInitException;
import edu.kit.ipd.parse.luna.LunaRunException;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;

/**
 * The Class ParseProvider defines an {@link ITextConnector} for that uses PARSE.
 */
public class ParseProvider implements ITextConnector {

    private static final Logger logger = LoggerFactory.getLogger(ParseProvider.class);

    private IText annotatedText;

    /**
     * Instantiates a new parses the provider.
     *
     * @param text the text
     * @throws LunaRunException  the luna run exception
     * @throws LunaInitException the luna init exception
     */
    public ParseProvider(InputStream text) throws LunaRunException, LunaInitException {
        IPARSEExecution parse = new SingleExecution();
        IGraph graph = parse.calculatePARSEGraph(text);
        annotatedText = convertParseGraphToAnnotatedText(graph);
    }

    private static IText convertParseGraphToAnnotatedText(IGraph graph) {
        if (logger.isDebugEnabled()) {
            logger.debug("Converting to IText");
        }

        var converter = new ParseConverter(graph);
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

    @Override
    public IText getAnnotatedText(String textName) {
        logger.warn("Returning annotated text ignoring the provided name");
        return annotatedText;
    }

}
