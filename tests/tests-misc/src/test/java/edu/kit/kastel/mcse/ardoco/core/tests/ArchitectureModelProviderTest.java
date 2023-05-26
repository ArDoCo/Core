/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.tests;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.PcmXmlModelConnector;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.UmlModelConnector;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;

class ArchitectureModelProviderTest {

    @DisplayName("Test Model Providers")
    @ParameterizedTest(name = "Testing for {0}")
    @EnumSource(value = Project.class)
    void pcmUmlIT(Project project) {
        var pcmFile = project.getModelFile(ArchitectureModelType.PCM);
        var umlFile = project.getModelFile(ArchitectureModelType.UML);

        PcmXmlModelConnector modelProviderPcm = null;
        UmlModelConnector modelProviderUml = null;
        try {
            modelProviderPcm = new PcmXmlModelConnector(pcmFile);
            modelProviderUml = new UmlModelConnector(umlFile);
        } catch (IOException e) {
            Assertions.fail("Cannot load models", e);
        }

        var pcmInstances = modelProviderPcm.getInstances();
        var umlInstances = modelProviderUml.getInstances();

        var fullNameArrayPcm = pcmInstances.collect(ModelInstance::getFullName).toSortedList();
        var fullNameArrayUml = umlInstances.collect(ModelInstance::getFullName).toSortedList();
        var idArrayPcm = pcmInstances.collect(ModelInstance::getUid).toSortedList();
        var idArrayUml = umlInstances.collect(ModelInstance::getUid).toSortedList();

        Assertions.assertAll(//
                () -> Assertions.assertIterableEquals(fullNameArrayPcm, fullNameArrayUml), //
                () -> Assertions.assertIterableEquals(idArrayPcm, idArrayUml) //
        );
    }
}
