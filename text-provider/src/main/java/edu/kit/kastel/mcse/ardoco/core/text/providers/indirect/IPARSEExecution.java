/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect;

import edu.kit.ipd.parse.luna.LunaInitException;
import edu.kit.ipd.parse.luna.LunaRunException;
import edu.kit.ipd.parse.luna.graph.IGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Simple interface to generate a PARSE Graph from text.
 *
 * @author Dominik Fuchss
 *
 */
interface IPARSEExecution {
    Logger logger = LoggerFactory.getLogger(ParseProvider.class);

    IGraph calculatePARSEGraph(InputStream text) throws LunaRunException, LunaInitException;
}
