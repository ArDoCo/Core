/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.n;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.jarowinkler.JaroWinklerMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.levenshtein.LevenshteinMeasure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an evaluation plan.
 */
public class EvalPlan {

    private final String group;
    private final int base;
    private final int threshold;
    private final List<WordSimMeasure> measures;

    public EvalPlan(String group, int base, int threshold) {
        this(group, base, threshold, Collections.emptyList());
    }

    public EvalPlan(String group, int base, int threshold, WordSimMeasure measure) {
        this(group, base, threshold, base == 2 ? List.of(measure, new JaroWinklerMeasure(), new LevenshteinMeasure()) : List.of(measure));
    }

    public EvalPlan(String group, int base, int threshold, List<WordSimMeasure> measures) {
        this.group = group;
        this.base = base;
        this.threshold = threshold;
        this.measures = new ArrayList<>(measures);

        if (base == 2) {
            this.measures.add(new JaroWinklerMeasure());
            this.measures.add(new LevenshteinMeasure());
        }
    }

    public String getId() {
        return getGroup() + "_b" + getBase() + "_t" + getThreshold();
    }

    public String getGroup() {
        return group;
    }

    public int getBase() {
        return base;
    }

    public int getThreshold() {
        return threshold;
    }

    public List<WordSimMeasure> getMeasures() {
        return measures;
    }

    public EvalPlan with(WordSimMeasure measure) {
        this.measures.add(measure);
        return this;
    }

}
