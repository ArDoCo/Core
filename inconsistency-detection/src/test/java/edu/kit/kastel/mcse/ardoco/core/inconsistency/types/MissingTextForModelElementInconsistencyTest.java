/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.types;

import org.junit.jupiter.api.BeforeEach;

import edu.kit.kastel.mcse.ardoco.core.inconsistency.IInconsistency;
import edu.kit.kastel.mcse.ardoco.core.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.model.Instance;

/**
 * @author Jan Keim
 *
 */
public class MissingTextForModelElementInconsistencyTest extends AbstractInconsistencyTypeTest {

    private MissingTextForModelElementInconsistency missingTextForModelElementInconsistency;

    @BeforeEach
    void beforeEach() {
        IModelInstance instance = new Instance("instance", "type", "uid1");
        missingTextForModelElementInconsistency = new MissingTextForModelElementInconsistency(instance);
    }

    @Override
    protected IInconsistency getInconsistency() {
        return missingTextForModelElementInconsistency;
    }

    @Override
    protected String getTypeString() {
        return "MissingTextForModelElement";
    }

    @Override
    protected String getReasonString() {
        return "Model contains an Instance that should be documented (because it is not whitelisted and its type \"type\" is configured to need documentation) but could not be found in documentation: instance";
    }

    @Override
    protected IInconsistency getUnequalInconsistency() {
        IModelInstance instance = new Instance("otherInstance", "otherType", "uid2");
        return new MissingTextForModelElementInconsistency(instance);
    }

    @Override
    protected IInconsistency getEqualInconsistency() {
        IModelInstance instance = new Instance("instance", "type", "uid1");
        return new MissingTextForModelElementInconsistency(instance);
    }

    @Override
    protected String[] getFileOutputEntry() {
        return new String[] { getTypeString(), "instance", "type" };
    }

}
