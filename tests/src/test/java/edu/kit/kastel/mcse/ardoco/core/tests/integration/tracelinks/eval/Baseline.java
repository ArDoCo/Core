/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.equality.EqualityMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.jarowinkler.JaroWinklerMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.levenshtein.LevenshteinMeasure;

/**
 * A baseline for the evaluation
 */
public enum Baseline {

    FIRST(1, List.of(new EqualityMeasure())),

    SECOND(2, List.of(new EqualityMeasure(), new LevenshteinMeasure(), new JaroWinklerMeasure()));

    private final int id;
    private final List<WordSimMeasure> measures;

    Baseline(int id, List<WordSimMeasure> measures) {
        this.id = id;
        this.measures = measures;
    }

    public int getId() {
        return id;
    }

    public List<WordSimMeasure> getMeasures() {
        return measures;
    }

}
