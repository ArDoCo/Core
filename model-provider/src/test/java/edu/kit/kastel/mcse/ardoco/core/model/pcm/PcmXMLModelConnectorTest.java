/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.model.pcm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.model.PcmXMLModelConnector;

class PcmXMLModelConnectorTest {
    private static final Logger logger = LoggerFactory.getLogger(PcmXMLModelConnectorTest.class);

    private static PcmXMLModelConnector loadModel(String modelFile) throws ReflectiveOperationException, IOException {
        return new PcmXMLModelConnector(new File(modelFile));
    }

    @Test
    @DisplayName("Get all instances from MediaStore")
    void getInstancesFromMediaStoreTest() throws ReflectiveOperationException, IOException {
        var connectorMediaStore = loadModel("src/test/resources/mediastore.repository");
        ImmutableList<ModelInstance> instances = connectorMediaStore.getInstances();

        if (logger.isDebugEnabled()) {
            logger.debug("Listing MediaStore instances:");
            for (ModelInstance instance : instances) {
                String info = instance.toString();
                logger.debug(info);
                logger.debug(String.valueOf(instance.getNameParts()));
            }
            logger.debug("\n");
        }

        int expectedNumberOfInstances = 14;
        Assertions.assertEquals(expectedNumberOfInstances, instances.size(), "The number of expected and found instances differs!");

        List<String> expectedInstancesNames = List.of("FileStorage", "Reencoding", "DB", "MediaAccess", "Facade", "TagWatermarking", "UserDBAdapter",
                "AudioWatermarking", "UserManagement", "ParallelWatermarking", "MediaManagement", "Cache", "Packaging", "DownloadLoadBalancer");

        for (ModelInstance instance : instances) {
            String name = instance.getFullName();
            Assertions.assertTrue(expectedInstancesNames.contains(name), "Found instance does not match one of the expected instances!");
        }
    }

    @Test
    @DisplayName("Get all instances from TeaStore")
    void getInstancesFromTeaStoreTest() throws ReflectiveOperationException, IOException, ParserConfigurationException, SAXException {
        var connectorTeaStore = loadModel("src/test/resources/teastore.repository");
        ImmutableList<ModelInstance> instances = connectorTeaStore.getInstances();

        if (logger.isDebugEnabled()) {
            logger.debug("Listing TeaStore instances:");
            for (ModelInstance instance : instances) {
                String info = instance.toString();
                logger.debug(info);
                logger.debug(String.valueOf(instance.getNameParts()));
            }
            logger.debug("\n");
        }

        int expectedNumberOfInstances = 11;
        Assertions.assertEquals(expectedNumberOfInstances, instances.size(), "The number of expected and found instances differs!");

    }

    @Test
    @DisplayName("Get all instances from TEAMMATES")
    void getInstancesFromTeammatesTest() throws ReflectiveOperationException, IOException, ParserConfigurationException, SAXException {
        var connectorTeaStore = loadModel("src/test/resources/teammates.repository");
        ImmutableList<ModelInstance> instances = connectorTeaStore.getInstances();

        Assertions.assertFalse(instances.isEmpty(), "There need to be some instances contained in the model.");

        if (logger.isDebugEnabled()) {
            logger.debug("Listing TEAMMATES instances:");
            for (ModelInstance instance : instances) {
                String info = instance.toString();
                logger.debug(info);
                logger.debug(String.valueOf(instance.getNameParts()));
            }
            logger.debug("\n");
        }

    }

    @Test
    @DisplayName("Simply test loading of MEDIASTORE")
    void testLoadMediaStore() throws Exception {
        InputStream is = Objects.requireNonNull(PcmXMLModelConnectorTest.class.getResourceAsStream("/mediastore.repository"));
        PcmXMLModelConnector connector = new PcmXMLModelConnector(is);
        is.close();

        Assertions.assertEquals("_7zbcYHDhEeSqnN80MQ2uGw", connector.getModelId());
        Assertions.assertEquals(Metamodel.ARCHITECTURE, connector.getMetamodel());
        Assertions.assertEquals(14, connector.getInstances().size());
        Assertions.assertTrue(connector.getInstances().allSatisfy(i -> i.getFullType().equals("BasicComponent")));
    }

}
