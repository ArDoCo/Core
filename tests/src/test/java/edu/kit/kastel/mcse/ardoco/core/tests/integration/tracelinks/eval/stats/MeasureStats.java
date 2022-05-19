/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.stats;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.deletelater.Comparison;

public class MeasureStats implements Comparable<MeasureStats> {

    public final WordSimMeasure measure;
    public final String measureId;
    public final Set<Comparison> accepted = new HashSet<>();
    public final Set<Comparison> denied = new HashSet<>();
    public final Set<Comparison> uniquelyAccepted = new HashSet<>();

    public MeasureStats(WordSimMeasure measure) {
        this.measure = measure;
        this.measureId = measure.getClass().getSimpleName();
    }

    public void addFrom(MeasureStats other) {
        this.accepted.addAll(other.accepted);
        this.denied.addAll(other.denied);
        this.uniquelyAccepted.addAll(other.uniquelyAccepted);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof MeasureStats))
            return false;
        MeasureStats that = (MeasureStats) o;
        return measureId.equals(that.measureId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(measureId);
    }

    @Override
    public int compareTo(MeasureStats o) {
        return this.measureId.compareTo(o.measureId);
    }

}
