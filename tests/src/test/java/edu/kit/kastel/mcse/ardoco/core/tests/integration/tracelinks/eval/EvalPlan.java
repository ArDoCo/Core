/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an evaluation plan.
 */
public class EvalPlan {

    private final String group;
    private final Baseline baseline;
    private final int threshold;
    private final List<WordSimMeasure> measures;

    public EvalPlan(String group, Baseline base, int threshold) {
        this(group, base, threshold, Collections.emptyList());
    }

    public EvalPlan(String group, Baseline base, int threshold, WordSimMeasure measure) {
        this(group, base, threshold, List.of(measure));
    }

    public EvalPlan(String group, Baseline base, int threshold, List<WordSimMeasure> measures) {
        this.group = group;
        this.baseline = base;
        this.threshold = threshold;
        this.measures = new ArrayList<>(measures);
	    this.measures.addAll(base.getMeasures());
    }

    public String getId() {
        return group + "_b" + baseline.getId() + "_t" + threshold;
    }

    public String getGroup() {
        return group;
    }

    public Baseline getBaseline() {
        return baseline;
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
