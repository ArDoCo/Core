package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.strategy;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;

import java.util.List;

public class MajorityStrategy implements ComparisonStrategy {
	@Override
	public boolean areWordsSimilar(ComparisonContext ctx, List<WordSimMeasure> measures) {
		return false; // TODO
	}
}
