package edu.kit.kastel.mcse.ardoco.core.models.modelgenerators.pcm;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.models.amtl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.models.modelgenerators.ArchitectureExtractorTest;
import edu.kit.kastel.mcse.ardoco.core.models.modelgenerators.architecture.pcm.PcmExtractor;

class PcmExtractorTest extends ArchitectureExtractorTest {

    private static final Logger logger = LoggerFactory.getLogger(PcmExtractorTest.class);

    @Test
    void extractorTest() {
        ArchitectureModel model = PcmExtractor.getExtractor().extractModel("src/test/resources/mediastore/architecture/pcm/ms.repository");
        checkModel(model);
    }

}
