package edu.kit.kastel.mcse.ardoco.core.models.generators.uml;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.uml.UmlExtractor;
import edu.kit.kastel.mcse.ardoco.core.models.generators.ArchitectureExtractorTest;

class UmlExtractorTest extends ArchitectureExtractorTest {

    private static final Logger logger = LoggerFactory.getLogger(UmlExtractorTest.class);

    @Test
    void extractorTest() {
        ArchitectureModel model = UmlExtractor.getExtractor().extractModel("src/test/resources/mediastore/architecture/uml/ms.uml");
        checkModel(model);
    }
}
