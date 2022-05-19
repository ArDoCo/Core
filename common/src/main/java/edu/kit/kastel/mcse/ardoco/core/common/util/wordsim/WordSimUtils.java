/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.strategy.ComparisonStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A static class that provides various utility methods to calculate similarity between different kinds of objects.
 * This class statically keeps a reference to a fallback {@link ComparisonStrategy} and a fallback list of
 * word similarity measures.
 * These fallbacks can be changed with the {@link #setMeasures(Collection)} and {@link #setStrategy(ComparisonStrategy)}
 * methods.
 * Any calls to methods that provide their own measures or strategies will not utilize these fallbacks.
 * Any calls that do not provide their own measures or strategies will utilize them.
 * As of right now, no protections against simultaneous write access from multiple threads exist.
 * Therefore, this class is not threadsafe.
 */
public class WordSimUtils {

	private static List<WordSimMeasure> MEASURES = WordSimLoader.loadUsingProperties();
	private static ComparisonStrategy STRATEGY = ComparisonStrategy.AT_LEAST_ONE;

	/**
	 * Sets which measures should be used for similarity comparison.
	 * The specified collection of measures will be used for all subsequent comparisons.
	 *
	 * @param measures the measures to use
	 */
	public static void setMeasures(Collection<WordSimMeasure> measures) {
		MEASURES = new ArrayList<>(measures);
	}

	/**
	 * Sets the default comparison strategy.
	 * The specified strategy will be used for all subsequent comparisons that themselves do not specify
	 * a strategy.
	 *
	 * @param strategy the new default strategy
	 */
	public static void setStrategy(ComparisonStrategy strategy) {
		STRATEGY = strategy;
	}

	/**
	 * Evaluates whether the words from the given {@link ComparisonContext} are similar
	 * using the specified comparison strategy.
	 *
	 * @param ctx      the context
	 * @param strategy the strategy
	 * @return Returns {@code true} if the given strategy considers the words similar enough.
	 */
	public static boolean areWordsSimilar(ComparisonContext ctx, ComparisonStrategy strategy) {
		Objects.requireNonNull(ctx);
		Objects.requireNonNull(strategy);

		return strategy.areWordsSimilar(ctx, MEASURES);
	}

	/**
	 * Evaluates whether the words from the given {@link ComparisonContext} are similar
	 * using the default comparison strategy.
	 * The default strategy can be changed with the {@link #setStrategy(ComparisonStrategy)} method.
	 *
	 * @param ctx the context
	 * @return Returns {@code true} if the default strategy considers the words similar enough.
	 */
	public static boolean areWordsSimilar(ComparisonContext ctx) {
		Objects.requireNonNull(ctx);
		return areWordsSimilar(ctx, STRATEGY);
	}

	/**
	 * Evaluates whether the given words are similar using the default comparison strategy.
	 * The default strategy can be changed with the {@link #setStrategy(ComparisonStrategy)} method.
	 *
	 * @param firstWord  the first word
	 * @param secondWord the second word
	 * @return Returns {@code true} if the default strategy considers the words similar enough.
	 */
	public static boolean areWordsSimilar(String firstWord, String secondWord) {
		return areWordsSimilar(new ComparisonContext(firstWord, secondWord, false), STRATEGY);
	}

	/**
	 * Evaluates whether the given words are similar using the specified comparison strategy.
	 *
	 * @param firstWord  the first word
	 * @param secondWord the second word
	 * @param strategy   the strategy to use
	 * @return Returns {@code true} if the given strategy considers the words similar enough.
	 */
	public static boolean areWordsSimilar(String firstWord, String secondWord, ComparisonStrategy strategy) {
		return areWordsSimilar(new ComparisonContext(firstWord, secondWord, false), strategy);
	}

	/**
	 * Evaluates whether the given words are similar using the default comparison strategy.
	 * The default strategy can be changed with the {@link #setStrategy(ComparisonStrategy)} method.
	 *
	 * @param firstWord  the first word
	 * @param secondWord the second word
	 * @return Returns {@code true} if the default strategy considers the words similar enough.
	 */
	public static boolean areWordsSimilar(IWord firstWord, IWord secondWord) {
		return areWordsSimilar(new ComparisonContext(firstWord, secondWord, false), STRATEGY);
	}

	/**
	 * Evaluates whether the given words are similar using the specified comparison strategy.
	 *
	 * @param firstWord  the first word
	 * @param secondWord the second word
	 * @param strategy   the strategy to use
	 * @return Returns {@code true} if the given strategy considers the words similar enough.
	 */
	public static boolean areWordsSimilar(IWord firstWord, IWord secondWord, ComparisonStrategy strategy) {
		return areWordsSimilar(new ComparisonContext(firstWord, secondWord, false), strategy);
	}

	/**
	 * Evaluates whether the given words are similar using the default comparison strategy.
	 * The default strategy can be changed with the {@link #setStrategy(ComparisonStrategy)} method.
	 *
	 * @param firstWord  the first word
	 * @param secondWord the second word
	 * @return Returns {@code true} if the default strategy considers the words similar enough.
	 */
	public static boolean areWordsSimilar(String firstWord, IWord secondWord) {
		return areWordsSimilar(new ComparisonContext(firstWord, secondWord.getText(), null, secondWord, false), STRATEGY);
	}

	/**
	 * Evaluates whether the given words are similar using the specified comparison strategy.
	 *
	 * @param firstWord  the first word
	 * @param secondWord the second word
	 * @param strategy   the strategy to use
	 * @return Returns {@code true} if the given strategy considers the words similar enough.
	 */
	public static boolean areWordsSimilar(String firstWord, IWord secondWord, ComparisonStrategy strategy) {
		return areWordsSimilar(new ComparisonContext(firstWord, secondWord.getText(), null, secondWord, false), strategy);
	}

	private WordSimUtils() {
	}

}
