/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.model.PcmXMLModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.UMLModelConnector;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;

class ArchitectureModelProviderTest {

    @DisplayName("Test Model Providers")
    @ParameterizedTest(name = "Testing for {0}")
    @EnumSource(value = Project.class)
    void pcmUmlIT(Project project) {
        var pcmFile = project.getModelFile(ArchitectureModelType.PCM);
        var umlFile = project.getModelFile(ArchitectureModelType.UML);

        PcmXMLModelConnector modelProviderPcm = null;
        UMLModelConnector modelProviderUml = null;
        try {
            modelProviderPcm = new PcmXMLModelConnector(pcmFile);
            modelProviderUml = new UMLModelConnector(umlFile);
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
