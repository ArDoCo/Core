/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.codetraceability.informants;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

public class TraceLinkCombiner extends Informant {
    private static final Logger logger = LoggerFactory.getLogger(TraceLinkCombiner.class);

    public TraceLinkCombiner(DataRepository dataRepository) {
        super(TraceLinkCombiner.class.getSimpleName(), dataRepository);
    }

    @Override
    public void run() {
        logger.info("Hi from TraceLinkCombiner");
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // empty
    }
}
