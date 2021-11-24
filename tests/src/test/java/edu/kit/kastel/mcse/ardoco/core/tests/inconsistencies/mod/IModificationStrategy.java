package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.mod;

import java.util.Iterator;

import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.text.IText;

public interface IModificationStrategy {
    Iterator<ModifiedElement<IModelConnector, IModelInstance>> getModifiedModelInstances();

    Iterator<ModifiedElement<IText, Integer>> getModifiedTexts();
}
