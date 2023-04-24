/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.models.old.uml;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UMLModelTest {
    private static final String PATH_TO_MODEL = "../../tests/src/test/resources/benchmark/mediastore/model_2016/uml/ms.uml";

    @Test
    void simpleLoad() throws IOException {
        UMLModel pcmModel = new UMLModel(new File(PATH_TO_MODEL));
        Assertions.assertNotNull(pcmModel.getModel());
        var model = pcmModel.getModel();
        Assertions.assertNotNull(model);
        for (var component : model.getComponents()) {
            Assertions.assertFalse(component.getProvided().isEmpty(), "Component " + component.getName() + " has no provided interface");
        }
    }
}
