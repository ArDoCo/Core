/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.mod;

import java.util.Iterator;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;

public interface IModificationStrategy {
    Iterator<ModifiedElement<IModelConnector, IModelInstance>> getModifiedModelInstances();

    Iterator<ModifiedElement<IText, Integer>> getModifiedTexts();
}
