package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect;

import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

import edu.kit.ipd.parse.luna.Luna;
import edu.kit.ipd.parse.luna.LunaInitException;
import edu.kit.ipd.parse.luna.LunaRunException;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.tools.ConfigManager;
import edu.kit.ipd.pronat.change_watchdog.ChangeWatchdog;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.pronat.prepipedatamodel.PrePipelineData;

/**
 * Simply invoke LUNA Framework.
 *
 * CAUTION: NOT TESTED! USE AT YOUR OWN RISK!
 *
 * @author Dominik Fuchss
 * @author Sophie Schulz
 *
 */
class LunaExecution implements IPARSEExecution {
    @Override
    public IGraph calculatePARSEGraph(InputStream text) throws LunaRunException, LunaInitException {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting creation of PARSE Graph");
        }

        var scanner = new Scanner(text);
        scanner.useDelimiter("\\A");
        String content = scanner.next();
        scanner.close();

        Properties changeWatchdogProps = ConfigManager.getConfiguration(ChangeWatchdog.class);
        // TODO Find a suitable time for termination. Currently 5s
        changeWatchdogProps.setProperty("CHANGE_TIMEOUT_THRESHOLD", "5000");

        Properties lunaProps = ConfigManager.getConfiguration(Luna.class);
        lunaProps.setProperty("PRE_PIPE", String.join(",", "indirect_tokenizer", "textSNLP", "graphBuilder"));
        lunaProps.setProperty("AGENTS", String.join(",", "stanfordAgent", "changeWatchdog"));

        var luna = Luna.getInstance();
        var ppd = new PrePipelineData();
        ppd.setTranscription(content);
        luna.setPrePipelineData(ppd);

        luna.init();
        luna.run();

        if (logger.isDebugEnabled()) {
            logger.debug("Finished creation of PARSE Graph");
        }
        return luna.getMainGraph();

    }
}
