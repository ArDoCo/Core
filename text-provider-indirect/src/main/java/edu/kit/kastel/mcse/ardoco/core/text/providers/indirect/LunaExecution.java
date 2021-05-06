package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect;

import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

import edu.kit.ipd.indirect.textSNLP.Stanford;
import edu.kit.ipd.parse.luna.Luna;
import edu.kit.ipd.parse.luna.LunaInitException;
import edu.kit.ipd.parse.luna.LunaRunException;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.tools.ConfigManager;
import edu.kit.ipd.pronat.change_watchdog.ChangeWatchdog;
import edu.kit.ipd.pronat.prepipedatamodel.PrePipelineData;

/**
 * Simply invoke LUNA Framework.
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

        Scanner scanner = new Scanner(text);
        scanner.useDelimiter("\\A");
        String content = scanner.next();
        scanner.close();

        Properties stanfordProps = ConfigManager.getConfiguration(Stanford.class);
        stanfordProps.setProperty("LEMMAS",
                "seconds/NNS/second;milliseconds/NNS/millisecond;hours/NNS/hour;minutes/NNS/minute;months/NNS/month;years/NNS/year");
        stanfordProps.setProperty("TAGGER_MODEL", "/edu/stanford/nlp/models/pos-tagger/english-bidirectional/english-bidirectional-distsim.tagger");

        Properties changeWatchdogProps = ConfigManager.getConfiguration(ChangeWatchdog.class);
        // TODO Find a suitable time for termination. Currently 5s
        changeWatchdogProps.setProperty("CHANGE_TIMEOUT_THRESHOLD", "5000");

        Properties lunaProps = ConfigManager.getConfiguration(Luna.class);
        lunaProps.setProperty("PRE_PIPE", String.join(",", "indirect_tokenizer", "textSNLP", "graphBuilder"));
        lunaProps.setProperty("AGENTS", String.join(",", "stanfordAgent", "changeWatchdog"));

        Luna luna = Luna.getInstance();
        PrePipelineData ppd = new PrePipelineData();
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
