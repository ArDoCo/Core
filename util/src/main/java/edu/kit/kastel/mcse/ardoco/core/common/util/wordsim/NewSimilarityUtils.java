/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.strategy.ComparisonStrategy;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A static class that provides various utility methods to calculate similarity between different kinds of objects.
 */
public class NewSimilarityUtils {

    private static List<WordSimMeasure> MEASURES = WordSimLoader.loadUsingProperties();
	private static ComparisonStrategy STRATEGY = ComparisonStrategy.AT_LEAST_ONE; // TODO: load default from properties?

    public static void setMeasures(Collection<WordSimMeasure> measures) { MEASURES = new ArrayList<>(measures); }

	public static void setStrategy(ComparisonStrategy strategy) { STRATEGY = strategy; }

    public static boolean areWordsSimilar(ComparisonContext ctx, ComparisonStrategy strategy) {
        Objects.requireNonNull(ctx);
		Objects.requireNonNull(strategy);

		return strategy.areWordsSimilar(ctx, MEASURES);
    }

	public static boolean areWordsSimilar(ComparisonContext ctx) {
		Objects.requireNonNull(ctx);
		return areWordsSimilar(ctx, STRATEGY);
	}

    public static boolean areWordsSimilar(String firstWord, String secondWord) {
        return areWordsSimilar(new ComparisonContext(firstWord, secondWord, false), STRATEGY);
    }

	public static boolean areWordsSimilar(String firstWord, String secondWord, ComparisonStrategy strategy) {
		return areWordsSimilar(new ComparisonContext(firstWord, secondWord, false), strategy);
	}

    public static boolean areWordsSimilar(IWord firstWord, IWord secondWord) {
        return areWordsSimilar(new ComparisonContext(firstWord, secondWord, false), STRATEGY);
    }

	public static boolean areWordsSimilar(IWord firstWord, IWord secondWord, ComparisonStrategy strategy) {
		return areWordsSimilar(new ComparisonContext(firstWord, secondWord, false), strategy);
	}

    public static boolean areWordsSimilar(String firstWord, IWord secondWord) {
        return areWordsSimilar(new ComparisonContext(firstWord, secondWord.getText(), null, secondWord, false), STRATEGY);
    }

	public static boolean areWordsSimilar(String firstWord, IWord secondWord, ComparisonStrategy strategy) {
		return areWordsSimilar(new ComparisonContext(firstWord, secondWord.getText(), null, secondWord, false), strategy);
	}

    private NewSimilarityUtils() {
    }

}
