package edu.kit.kastel.mcse.ardoco.core.model.pcm;

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstance;

@RunWith(JUnitPlatform.class)
class PcmOntologyModelConnectorTest {
	private static Logger logger = LogManager.getLogger();

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
		}
		List<IInstance> instances = connectorMediaStore.getInstances();

		if (logger.isDebugEnabled()) {
			logger.debug("Listing MediaStore insances:");
			for (IInstance instance : instances) {
				String info = instance.toString();
				logger.debug(info);
				logger.debug(instance.getNames());
			}
			logger.debug("\n");
		}

		int expectedNumberOfInstances = 14;
		Assertions.assertEquals(expectedNumberOfInstances, instances.size(), "The number of expected and found instances differs!");

		List<String> expectedInstancesNames = List.of("FileStorage", "Reencoding", "DB", "MediaAccess", "Facade", "TagWatermarking", "UserDBAdapter",
				"AudioWatermarking", "UserManagement", "ParallelWatermarking", "MediaManagement", "Cache", "Packaging", "DownloadLoadBalancer");

		for (IInstance instance : instances) {
			String name = instance.getLongestName();
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
		}
		List<IInstance> instances = connectorTeaStore.getInstances();

		if (logger.isDebugEnabled()) {
			logger.debug("Listing TeaStore insances:");
			for (IInstance instance : instances) {
				String info = instance.toString();
				logger.debug(info);
				logger.debug(instance.getNames());
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
		}
		List<IInstance> instances = connectorTeaStore.getInstances();

		if (logger.isDebugEnabled()) {
			logger.debug("Listing TEAMMATES instances:");
			for (IInstance instance : instances) {
				String info = instance.toString();
				logger.debug(info);
				logger.debug(instance.getNames());
			}
			logger.debug("\n");
		}

		connectorTeaStore = null;
	}

}
