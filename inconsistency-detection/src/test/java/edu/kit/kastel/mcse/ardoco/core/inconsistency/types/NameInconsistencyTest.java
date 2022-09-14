/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.types;

import org.junit.jupiter.api.BeforeEach;

import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.model.ModelInstanceImpl;

class NameInconsistencyTest extends AbstractInconsistencyTypeTest {
    private static final String NAME_INCONSISTENCY_TYPE = "NameInconsistency";
    private NameInconsistency nameInconsistency;

    @BeforeEach
    void beforeEach() {
        nameInconsistency = new NameInconsistency(new ModelInstanceImpl("Dummy", "BasicComponent", "1"), new DummyWord());
    }

    @Override
    protected Inconsistency getInconsistency() {
        return nameInconsistency;
    }

    @Override
    protected String getTypeString() {
        return NAME_INCONSISTENCY_TYPE;
    }

    @Override
    protected String getReasonString() {
        return "Inconsistent naming in trace link between textual occurence \"text\" (sentence 1) and model element \"Dummy\" (1)";
    }

    @Override
    protected Inconsistency getUnequalInconsistency() {
        return new NameInconsistency(new ModelInstanceImpl("DummyComposite", "CompositeComponent", "2"), new DummyWord());
    }

    @Override
    protected Inconsistency getEqualInconsistency() {
        return new NameInconsistency(new ModelInstanceImpl("Dummy", "BasicComponent", "1"), new DummyWord());
    }

    @Override
    protected String[] getFileOutputEntry() {
        return new String[] { getTypeString(), Integer.toString(1), "text", "Dummy", "1" };
    }
}
