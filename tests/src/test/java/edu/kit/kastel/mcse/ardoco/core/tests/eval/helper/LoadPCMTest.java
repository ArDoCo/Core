/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval.helper;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import edu.kit.kastel.informalin.framework.models.pcm.PCMModel;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;

class LoadPCMTest {
    @DisplayName("Load Components of Project")
    @ParameterizedTest(name = "Loading components of {0}")
    @EnumSource(value = Project.class)
    void loadAllProjectComponentsTest(Project project) throws IOException {
        var pcmModelFile = project.getModelFile(ArchitectureModelType.PCM);
        Assertions.assertNotNull(pcmModelFile);
        PCMModel model = new PCMModel(pcmModelFile);
        Assertions.assertFalse(model.getRepository().getComponents().isEmpty());

        for (var component : model.getRepository().getComponents()) {
            System.out.println(component.getEntityName() + ": " + component.getId());
        }

    }
}
