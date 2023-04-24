package edu.kit.kastel.mcse.ardoco.core.models.modelgenerators;

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.models.amtl.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.models.amtl.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.models.amtl.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.models.amtl.ArchitectureModel;

public class ArchitectureExtractorTest {

    private static final Logger logger = LoggerFactory.getLogger(ArchitectureExtractorTest.class);

    public void checkModel(ArchitectureModel model) {
        Assertions.assertNotNull(model);

        Set<? extends ArchitectureItem> modelContent = model.getContent();
        Assertions.assertFalse(modelContent.isEmpty());
        for (Entity archEndpoint : modelContent) {
            Assertions.assertNotNull(archEndpoint);
            if (archEndpoint instanceof ArchitectureComponent modelComponent) {
                logger.info("---");
                logger.info("{}", modelComponent);
                printInterfaces("Provided", modelComponent.getProvidedInterfaces());
                printInterfaces("Required", modelComponent.getRequiredInterfaces());
            }
        }
        logger.info("---");
    }

    public void printInterfaces(String description, Set<ArchitectureInterface> interfaces) {
        if (!interfaces.isEmpty()) {
            logger.info("{} Interfaces: ", description);
            interfaces.forEach(anInterface -> {
                Assertions.assertNotNull(anInterface);
                logger.info(" {} ", anInterface.getName());
                logger.info("  Methods:");
                anInterface.getSignatures().forEach(signature -> logger.info("  {}", signature));
            });
        }
    }
}
