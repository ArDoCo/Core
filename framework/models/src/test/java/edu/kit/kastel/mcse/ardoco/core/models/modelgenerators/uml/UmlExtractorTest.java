package edu.kit.kastel.mcse.ardoco.core.models.modelgenerators.uml;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.models.amtl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.models.modelgenerators.ArchitectureExtractorTest;
import edu.kit.kastel.mcse.ardoco.core.models.modelgenerators.architecture.uml.UmlExtractor;

class UmlExtractorTest extends ArchitectureExtractorTest {

    private static final Logger logger = LoggerFactory.getLogger(UmlExtractorTest.class);

    @Test
    void extractorTest() {
        ArchitectureModel model = UmlExtractor.getExtractor().extractModel("src/test/resources/mediastore/architecture/uml/ms.uml");
        checkModel(model);
    }
}
