package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.strategy.ComparisonStrategy;

// TODO: DELETE
public class Example {

	void example() {
		NewSimilarityUtils.areWordsSimilar("hello", "hallo");
		NewSimilarityUtils.areWordsSimilar("hello", "hallo", ComparisonStrategy.AT_LEAST_ONE);
		NewSimilarityUtils.areWordsSimilar("hello", "hallo", ComparisonStrategy.MAJORITY);
		NewSimilarityUtils.areWordsSimilar("hello", "hallo", ComparisonStrategy.THRESHOLD(0.5));

		NewSimilarityUtils.setStrategy((ctx, measures) -> {
			return measures.stream().anyMatch(measure -> measure.areWordsSimilar(ctx));
		});

		NewSimilarityUtils.areWordsSimilar("hello", "hallo");
	}

}
