package edu.kit.kastel.mcse.ardoco.core.models.generators.java;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code.java.JavaExtractor;

class JavaExtractorTest {
    private static final Logger logger = LoggerFactory.getLogger(JavaExtractorTest.class);

    @Test
    void extractorTest() {
        CodeModel model = JavaExtractor.getExtractor().extractModel("src/test/resources/interface");
        Assertions.assertNotNull(model);
        for (Entity codePackage : model.getAllPackages()) {
            Assertions.assertNotNull(codePackage);
            logger.info("Package: {}", codePackage);
        }

        Assertions.assertEquals(7, model.getEndpoints().size());
    }
}
