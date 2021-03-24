package edu.kit.ipd.consistency_analyzer.modelproviders.pcm;

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import edu.kit.ipd.consistency_analyzer.datastructures.IInstance;

@RunWith(JUnitPlatform.class)
class PcmOntologyModelConnectorTest {
    private static Logger logger = LogManager.getLogger();

    private static PcmOntologyModelConnector connector;

    @BeforeAll
    static void setup() {
        File file = new File("src/test/resources/mediastore.owl");

        String absolutePath = file.getAbsolutePath();
        connector = new PcmOntologyModelConnector(absolutePath);
    }

    @Test
    @DisplayName("Get all instances from MediaStore ontology")
    void getInstancesFromMediaStoreTest() {
        if (connector == null) {
            logger.debug("connector is null");
        }
        List<IInstance> instances = connector.getInstances();

        if (logger.isDebugEnabled()) {
            for (IInstance instance : instances) {
                String info = instance.toString();
                logger.debug(info);
                logger.debug(instance.getNames());
            }
        }

        int expectedNumberOfInstances = 14;
        Assertions.assertEquals(expectedNumberOfInstances, instances.size(), "The number of expected and found instances differs!");

        List<String> expectedInstancesNames = List.of("FileStorage", "Reencoding", "DB", "MediaAccess", "Facade", "TagWatermarking", "UserDBAdapter",
                "AudioWatermarking", "UserManagement", "ParallelWatermarking", "MediaManagement", "Cache", "Packaging", "DownloadLoadBalancer");

        for (IInstance instance : instances) {
            String name = instance.getLongestName();
            Assertions.assertTrue(expectedInstancesNames.contains(name), "Found instance does not match one of the expected instances!");
        }

    }

    @AfterAll
    static void cleanUp() {
        connector = null;
    }
}
