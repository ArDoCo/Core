package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.strategy;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;

import java.util.List;

/**
 * TODO
 */
public interface ComparisonStrategy {

	ComparisonStrategy AT_LEAST_ONE = new AtleastOneStrategy();

	ComparisonStrategy MAJORITY = new MajorityStrategy();

	static ComparisonStrategy threshold(double threshold) { return null; } // ?

	/**
	 * TODO
	 * @param ctx
	 * @param measures
	 * @return
	 */
	boolean areWordsSimilar(ComparisonContext ctx, List<WordSimMeasure> measures);

}
