/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.types;

import org.junit.jupiter.api.BeforeEach;

import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.model.ModelInstanceImpl;

/**
 *
 */
public class MissingTextForModelElementInconsistencyTest extends AbstractInconsistencyTypeTest {

    private MissingTextForModelElementInconsistency missingTextForModelElementInconsistency;

    @BeforeEach
    void beforeEach() {
        ModelInstance instance = new ModelInstanceImpl("instance", "type", "uid1");
        missingTextForModelElementInconsistency = new MissingTextForModelElementInconsistency(instance);
    }

    @Override
    protected Inconsistency getInconsistency() {
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
    protected Inconsistency getUnequalInconsistency() {
        ModelInstance instance = new ModelInstanceImpl("otherInstance", "otherType", "uid2");
        return new MissingTextForModelElementInconsistency(instance);
    }

    @Override
    protected Inconsistency getEqualInconsistency() {
        ModelInstance instance = new ModelInstanceImpl("instance", "type", "uid1");
        return new MissingTextForModelElementInconsistency(instance);
    }

    @Override
    protected String[] getFileOutputEntry() {
        return new String[] { getTypeString(), "instance", "type" };
    }

}
