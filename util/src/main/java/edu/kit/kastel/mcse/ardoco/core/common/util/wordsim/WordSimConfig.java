package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.equality.EqualityMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.jarowinkler.JaroWinklerMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.levenshtein.LevenshteinMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.ngram.NgramMeasure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A configuration consisting of word similarity measures.
 */
public class WordSimConfig {

    public static final WordSimConfig DEFAULT = new WordSimConfig(
            List.of(new EqualityMeasure(),
                    new JaroWinklerMeasure(),
                    new LevenshteinMeasure(),
                    new NgramMeasure(NgramMeasure.Variant.LUCENE, 2, 0.8)
            )
    );

    private final List<WordSimMeasure> measures;

    public WordSimConfig(Collection<WordSimMeasure> measures) {
        this.measures = new ArrayList<>(measures);
    }

    public List<WordSimMeasure> getMeasures() {
        return measures;
    }

    @Override public String toString() {
        return "WordSimConfig{" + "measures=" + measures + '}';
    }

}
