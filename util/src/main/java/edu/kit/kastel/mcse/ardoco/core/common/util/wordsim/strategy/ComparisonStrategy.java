package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.strategy;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;

import java.util.List;

public interface ComparisonStrategy {

	ComparisonStrategy AT_LEAST_ONE = new AtleastOneStrategy();

	ComparisonStrategy MAJORITY = null;

	static ComparisonStrategy THRESHOLD(double threshold) { return null; } // ?

	boolean areWordsSimilar(ComparisonContext ctx, List<WordSimMeasure> measures);

}
