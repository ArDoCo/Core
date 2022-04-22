/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.ipd.parse.luna.LunaInitException;
import edu.kit.ipd.parse.luna.LunaRunException;
import edu.kit.ipd.parse.luna.graph.IGraph;

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
