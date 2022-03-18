/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.types;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.BeforeEach;

import edu.kit.kastel.mcse.ardoco.core.inconsistency.IInconsistency;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.textextraction.NounMapping;

/**
 * @author Jan Keim
 *
 */
public class MissingModelInstanceInconsistencyTest extends AbstractInconsistencyTypeTest {

    private MissingModelInstanceInconsistency missingModelInstanceInconsistency;

    @BeforeEach
    void beforeEach() {
        ImmutableList<IWord> words = Lists.immutable.of(new DummyWord());
        ImmutableList<String> occurences = Lists.immutable.of("occurence");
        var nounMapping = new NounMapping(words, MappingKind.NAME, 1.0, words.toList(), occurences);
        var recommendedInstance = new RecommendedInstance("name", "type", 1.0, Lists.immutable.of(nounMapping), Lists.immutable.<INounMapping> empty());
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
        var recommendedInstance = new RecommendedInstance("otherName", "otherType", 1.0, Lists.immutable.<INounMapping> empty(),
                Lists.immutable.<INounMapping> empty());
        return new MissingModelInstanceInconsistency(recommendedInstance);
    }

    @Override
    protected IInconsistency getEqualInconsistency() {
        var recommendedInstance = new RecommendedInstance("name", "type", 1.0, Lists.immutable.<INounMapping> empty(), Lists.immutable.<INounMapping> empty());
        return new MissingModelInstanceInconsistency(recommendedInstance);
    }

    @Override
    protected String[] getFileOutputEntry() {
        return new String[] { getTypeString(), "1", "name", "text", Double.toString(0.0) };
    }

}
