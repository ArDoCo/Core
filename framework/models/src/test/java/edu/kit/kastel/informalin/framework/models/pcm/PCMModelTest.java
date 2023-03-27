/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.informalin.framework.models.pcm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PCMModelTest {
    private static final String PATH_TO_MODEL = "src/test/resources/benchmark/mediastore/model_2016/pcm/ms.repository";

    @Test
    void simpleLoad() throws IOException {
        PCMModel pcmModel = new PCMModel(new File(PATH_TO_MODEL));
        Assertions.assertNotNull(pcmModel.getRepository());
        var repo = pcmModel.getRepository();
        for (var component : repo.getComponents()) {
            Assertions.assertFalse(component.getProvided().isEmpty(), "Component " + component.getEntityName() + " has no provided interface");
        }
    }

    @Test
    void simpleLoadViaProvidedStream() throws IOException {
        try (FileInputStream fis = new FileInputStream(PATH_TO_MODEL)) {
            PCMModel pcmModel = new PCMModel(fis);
            Assertions.assertNotNull(pcmModel.getRepository());
            var repo = pcmModel.getRepository();
            for (var component : repo.getComponents()) {
                Assertions.assertFalse(component.getProvided().isEmpty(), "Component " + component.getEntityName() + " has no provided interface");
            }
        }
    }
}
