package edu.kit.kastel.mcse.ardoco.core.codetraceability.informants;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

public class ArCoTLInformant extends Informant {
    private static final Logger logger = LoggerFactory.getLogger(ArCoTLInformant.class);

    public ArCoTLInformant(DataRepository dataRepository) {
        super(ArCoTLInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void run() {
        logger.info("Hi from ArCoTLInformant");
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // empty
    }

}
