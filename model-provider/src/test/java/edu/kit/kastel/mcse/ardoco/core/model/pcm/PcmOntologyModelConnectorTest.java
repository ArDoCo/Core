/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.model.pcm;

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.model.IModelInstance;

class PcmOntologyModelConnectorTest {
    private static final Logger logger = LogManager.getLogger();

    private static PcmOntologyModelConnector loadModel(String modelFile) {
        File file = new File(modelFile);

        String absolutePath = file.getAbsolutePath();
        return new PcmOntologyModelConnector(absolutePath);
    }

    @Test
    @DisplayName("Get all instances from MediaStore ontology")
    void getInstancesFromMediaStoreTest() {
        PcmOntologyModelConnector connectorMediaStore = loadModel("src/test/resources/mediastore.owl");
        if (connectorMediaStore == null) {
            logger.debug("connector is null");
            Assertions.assertTrue(false, "Connector is null, thus the model was not loaded.");
        }
        ImmutableList<IModelInstance> instances = connectorMediaStore.getInstances();

        if (logger.isDebugEnabled()) {
            logger.debug("Listing MediaStore instances:");
            for (IModelInstance instance : instances) {
                String info = instance.toString();
                logger.debug(info);
                logger.debug(instance.getNameParts());
            }
            logger.debug("\n");
        }

        int expectedNumberOfInstances = 14;
        Assertions.assertEquals(expectedNumberOfInstances, instances.size(), "The number of expected and found instances differs!");

        List<String> expectedInstancesNames = List.of("FileStorage", "Reencoding", "DB", "MediaAccess", "Facade", "TagWatermarking", "UserDBAdapter",
                "AudioWatermarking", "UserManagement", "ParallelWatermarking", "MediaManagement", "Cache", "Packaging", "DownloadLoadBalancer");

        for (IModelInstance instance : instances) {
            String name = instance.getFullName();
            Assertions.assertTrue(expectedInstancesNames.contains(name), "Found instance does not match one of the expected instances!");
        }

        connectorMediaStore = null;
    }

    @Test
    @DisplayName("Get all instances from TeaStore ontology")
    void getInstancesFromTeaStoreTest() {
        PcmOntologyModelConnector connectorTeaStore = loadModel("src/test/resources/teastore.owl");
        if (connectorTeaStore == null) {
            logger.debug("connector is null");
            Assertions.assertTrue(false, "Connector is null, thus the model was not loaded.");
        }
        ImmutableList<IModelInstance> instances = connectorTeaStore.getInstances();

        if (logger.isDebugEnabled()) {
            logger.debug("Listing TeaStore instances:");
            for (IModelInstance instance : instances) {
                String info = instance.toString();
                logger.debug(info);
                logger.debug(instance.getNameParts());
            }
            logger.debug("\n");
        }

        int expectedNumberOfInstances = 13;
        Assertions.assertEquals(expectedNumberOfInstances, instances.size(), "The number of expected and found instances differs!");

        connectorTeaStore = null;
    }

    @Test
    @DisplayName("Get all instances from TEAMMATES ontology")
    void getInstancesFromTeammatesTest() {
        PcmOntologyModelConnector connectorTeaStore = loadModel("src/test/resources/teammates.owl");
        if (connectorTeaStore == null) {
            logger.debug("connector is null");
            Assertions.assertTrue(false, "Connector is null, thus the model was not loaded.");
        }
        ImmutableList<IModelInstance> instances = connectorTeaStore.getInstances();

        Assertions.assertFalse(instances.isEmpty(), "There need to be some instances contained in the model.");

        if (logger.isDebugEnabled()) {
            logger.debug("Listing TEAMMATES instances:");
            for (IModelInstance instance : instances) {
                String info = instance.toString();
                logger.debug(info);
                logger.debug(instance.getNameParts());
            }
            logger.debug("\n");
        }

        connectorTeaStore = null;
    }

}
