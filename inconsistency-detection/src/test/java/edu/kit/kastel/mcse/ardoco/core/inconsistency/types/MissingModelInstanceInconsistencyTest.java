/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.types;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.BeforeEach;

import edu.kit.kastel.mcse.ardoco.core.api.agent.IClaimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.IInconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.textextraction.NounMapping;

/**
 * @author Jan Keim
 */
public class MissingModelInstanceInconsistencyTest extends AbstractInconsistencyTypeTest implements IClaimant {

    private MissingModelInstanceInconsistency missingModelInstanceInconsistency;

    @BeforeEach
    void beforeEach() {
        ImmutableList<IWord> words = Lists.immutable.of(new DummyWord());
        ImmutableList<String> occurences = Lists.immutable.of("occurence");
        var nounMapping = new NounMapping(words, MappingKind.NAME, this, 1.0, Lists.immutable.withAll(words), occurences);
        var recommendedInstance = new RecommendedInstance("name", "type", this, 1.0, Lists.immutable.of(nounMapping), Lists.immutable.empty());
        missingModelInstanceInconsistency = new MissingModelInstanceInconsistency(recommendedInstance);
    }

    @Override
    protected IInconsistency getInconsistency() {
        return missingModelInstanceInconsistency;
    }

    @Override
    protected String getTypeString() {
        return "MissingModelInstance";
    }

    @Override
    protected String getReasonString() {
        return "Text indicates (confidence: 0.00) that \"name\" should be contained in the model(s) but could not be found. Sentences: 1";
    }

    @Override
    protected IInconsistency getUnequalInconsistency() {
        var recommendedInstance = new RecommendedInstance("otherName", "otherType", null, 1.0, Lists.immutable.empty(), Lists.immutable.<INounMapping> empty());
        return new MissingModelInstanceInconsistency(recommendedInstance);
    }

    @Override
    protected IInconsistency getEqualInconsistency() {
        var recommendedInstance = new RecommendedInstance("name", "type", null, 1.0, Lists.immutable.empty(), Lists.immutable.empty());
        return new MissingModelInstanceInconsistency(recommendedInstance);
    }

    @Override
    protected String[] getFileOutputEntry() {
        return new String[] { getTypeString(), "1", "name", "text", Double.toString(0.0) };
    }

}
