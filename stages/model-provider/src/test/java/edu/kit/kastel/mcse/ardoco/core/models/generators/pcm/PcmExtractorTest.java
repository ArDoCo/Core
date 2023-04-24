/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.models.generators.pcm;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.pcm.PcmExtractor;
import edu.kit.kastel.mcse.ardoco.core.models.generators.ArchitectureExtractorTest;

class PcmExtractorTest extends ArchitectureExtractorTest {

    private static final Logger logger = LoggerFactory.getLogger(PcmExtractorTest.class);

    @Test
    void extractorTest() {
        ArchitectureModel model = PcmExtractor.getExtractor().extractModel("src/test/resources/mediastore/architecture/pcm/ms.repository");
        checkModel(model);
    }

}
