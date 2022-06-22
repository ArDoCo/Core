/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.mod;

import java.util.Iterator;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelConnector;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Text;

public interface ModificationStrategy {
    Iterator<ModifiedElement<ModelConnector, ModelInstance>> getModifiedModelInstances();

    Iterator<ModifiedElement<Text, Integer>> getModifiedTexts();
}
