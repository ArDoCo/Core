/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an evaluation plan.
 */
public class EvalPlan {

    private final String group;
    private final Baseline baseline;
    private final int threshold;
    private final List<WordSimMeasure> measures;

    public EvalPlan(String groupPrefix, Baseline base, int threshold, WordSimMeasure measure) {
        this(groupPrefix, base, threshold, List.of(measure));
    }

    public EvalPlan(String groupPrefix, Baseline base, int threshold, List<WordSimMeasure> measures) {
        this.group = groupPrefix + "_b" + base.getId();
        this.baseline = base;
        this.threshold = threshold;
        this.measures = new ArrayList<>(measures);
	    this.measures.addAll(base.getMeasures());
    }

    public String getId() {
        return getGroup() + "_t" + getThreshold();
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

}
