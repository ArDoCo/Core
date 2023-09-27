/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.types;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureInterface;

import org.junit.jupiter.api.BeforeEach;

import edu.kit.kastel.mcse.ardoco.core.api.inconsistency.Inconsistency;

import java.util.HashSet;

/**
 *
 */
public class MissingTextForModelElementInconsistencyTest extends AbstractInconsistencyTypeTest {

    private MissingTextForModelElementInconsistency missingTextForModelElementInconsistency;

    @BeforeEach
    void beforeEach() {
        Entity entity = new ArchitectureInterface("instance", "uid1", new HashSet<>());
        missingTextForModelElementInconsistency = new MissingTextForModelElementInconsistency(entity);
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
        Entity entity = new ArchitectureComponent("otherInstance", "uid2", new HashSet<>(), new HashSet<>(), new HashSet<>());
        return new MissingTextForModelElementInconsistency(entity);
    }

    @Override
    protected Inconsistency getEqualInconsistency() {
        Entity entity = new ArchitectureInterface("instance", "uid1", new HashSet<>());
        return new MissingTextForModelElementInconsistency(entity);
    }

    @Override
    protected String[] getFileOutputEntry() {
        return new String[] { getTypeString(), "instance", "type" };
    }

}
