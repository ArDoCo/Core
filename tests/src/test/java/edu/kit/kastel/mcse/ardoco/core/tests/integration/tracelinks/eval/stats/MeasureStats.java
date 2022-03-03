package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.stats;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.stats.Comparison;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MeasureStats implements Comparable<MeasureStats> {

	public final WordSimMeasure measure;
	public final String measureId;
	public final List<Comparison> accepted = new ArrayList<>();
	public final List<Comparison> denied = new ArrayList<>();
	public final List<Comparison> uniquelyAccepted = new ArrayList<>();

	public MeasureStats(WordSimMeasure measure) {
		this.measure = measure;
		this.measureId = measure.getClass().getSimpleName();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MeasureStats)) return false;
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
