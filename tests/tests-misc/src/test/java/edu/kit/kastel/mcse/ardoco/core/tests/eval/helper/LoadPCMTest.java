/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval.helper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.pcm.parser.PcmModel;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;

class LoadPCMTest {
    @DisplayName("Load Components of Project")
    @ParameterizedTest(name = "Loading components of {0}")
    @EnumSource(value = Project.class)
    void loadAllProjectComponentsTest(GoldStandardProject project) {
        var pcmModelFile = project.getModelFile(ArchitectureModelType.PCM);
        Assertions.assertNotNull(pcmModelFile);
        PcmModel model = new PcmModel(pcmModelFile);
        Assertions.assertFalse(model.getRepository().getComponents().isEmpty());

        for (var component : model.getRepository().getComponents()) {
            System.out.println(component.getEntityName() + ": " + component.getId());
        }

    }
}
