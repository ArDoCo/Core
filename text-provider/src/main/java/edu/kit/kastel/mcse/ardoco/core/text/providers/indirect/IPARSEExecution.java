/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect;


import edu.kit.ipd.parse.luna.LunaInitException;
import edu.kit.ipd.parse.luna.LunaRunException;
import edu.kit.ipd.parse.luna.graph.IGraph;
import java.io.InputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Simple interface to generate a PARSE Graph from text.
 *
 * @author Dominik Fuchss
 *
 */
interface IPARSEExecution {
    Logger logger = LogManager.getLogger(ParseProvider.class);

    IGraph calculatePARSEGraph(InputStream text) throws LunaRunException, LunaInitException;
}
