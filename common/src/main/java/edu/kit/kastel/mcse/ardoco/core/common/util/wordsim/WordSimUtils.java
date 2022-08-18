/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import java.util.Collection;
import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteOpenMode;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.strategy.ComparisonStrategy;

/**
 * A static class that provides various utility methods to calculate similarity between different kinds of objects. This
 * class statically keeps a reference to a fallback {@link ComparisonStrategy} and a fallback list of word similarity
 * measures. These fallbacks can be changed with the {@link #setMeasures(Collection)} and
 * {@link #setStrategy(ComparisonStrategy)} methods. Any calls to methods that provide their own measures or strategies
 * will not utilize these fallbacks. Any calls that do not provide their own measures or strategies will utilize them.
 * As of right now, no protections against simultaneous write access from multiple threads exist. Therefore, this class
 * is not threadsafe.
 */
public class WordSimUtils {

    private static ImmutableList<WordSimMeasure> measures = WordSimLoader.loadUsingProperties();
    private static ComparisonStrategy strategy = ComparisonStrategy.AT_LEAST_ONE;

    private WordSimUtils() {
    }

    /**
     * Sets which measures should be used for similarity comparison. The specified collection of measures will be used
     * for all subsequent comparisons.
     *
     * @param measures the measures to use
     */
    public static void setMeasures(Collection<WordSimMeasure> measures) {
        WordSimUtils.measures = Lists.immutable.withAll(measures);
    }

    /**
     * Sets the default comparison strategy. The specified strategy will be used for all subsequent comparisons that
     * themselves do not specify a strategy.
     *
     * @param strategy the new default strategy
     */
    public static void setStrategy(ComparisonStrategy strategy) {
        WordSimUtils.strategy = strategy;
    }

    /**
     * Evaluates whether the words from the given {@link ComparisonContext} are similar using the specified comparison
     * strategy.
     *
     * @param ctx      the context
     * @param strategy the strategy
     * @return Returns {@code true} if the given strategy considers the words similar enough.
     */
    public static boolean areWordsSimilar(ComparisonContext ctx, ComparisonStrategy strategy) {
        Objects.requireNonNull(ctx);
        Objects.requireNonNull(strategy);

        // TODO currently, we need the split test as it improves results by a lot. In future, we should try to avoid its requirement
        if (!splitLengthTest(ctx)) {
            return false;
        }

        return strategy.areWordsSimilar(ctx, measures.toList());
    }

    private static boolean splitLengthTest(ComparisonContext ctx) {
        var first = ctx.firstTerm().toLowerCase();
        var second = ctx.secondTerm().toLowerCase();
        return (first.split(" ").length == second.split(" ").length);
    }

    /**
     * Evaluates whether the words from the given {@link ComparisonContext} are similar using the default comparison
     * strategy. The default strategy can be changed with the {@link #setStrategy(ComparisonStrategy)} method.
     *
     * @param ctx the context
     * @return Returns {@code true} if the default strategy considers the words similar enough.
     */
    public static boolean areWordsSimilar(ComparisonContext ctx) {
        Objects.requireNonNull(ctx);
        return areWordsSimilar(ctx, strategy);
    }

    /**
     * Evaluates whether the given words are similar using the default comparison strategy. The default strategy can be
     * changed with the {@link #setStrategy(ComparisonStrategy)} method.
     *
     * @param firstWord  the first word
     * @param secondWord the second word
     * @return Returns {@code true} if the default strategy considers the words similar enough.
     */
    public static boolean areWordsSimilar(String firstWord, String secondWord) {
        return areWordsSimilar(new ComparisonContext(firstWord, secondWord, false), strategy);
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
     * Evaluates whether the given words are similar using the default comparison strategy. The default strategy can be
     * changed with the {@link #setStrategy(ComparisonStrategy)} method.
     *
     * @param firstWord  the first word
     * @param secondWord the second word
     * @return Returns {@code true} if the default strategy considers the words similar enough.
     */
    public static boolean areWordsSimilar(Word firstWord, Word secondWord) {
        return areWordsSimilar(new ComparisonContext(firstWord, secondWord, false), strategy);
    }

    /**
     * Evaluates whether the given words are similar using the specified comparison strategy.
     *
     * @param firstWord  the first word
     * @param secondWord the second word
     * @param strategy   the strategy to use
     * @return Returns {@code true} if the given strategy considers the words similar enough.
     */
    public static boolean areWordsSimilar(Word firstWord, Word secondWord, ComparisonStrategy strategy) {
        return areWordsSimilar(new ComparisonContext(firstWord, secondWord, false), strategy);
    }

    /**
     * Evaluates whether the given words are similar using the default comparison strategy. The default strategy can be
     * changed with the {@link #setStrategy(ComparisonStrategy)} method.
     *
     * @param firstWord  the first word
     * @param secondWord the second word
     * @return Returns {@code true} if the default strategy considers the words similar enough.
     */
    public static boolean areWordsSimilar(String firstWord, Word secondWord) {
        return areWordsSimilar(new ComparisonContext(firstWord, secondWord.getText(), null, secondWord, false), strategy);
    }

    /**
     * Evaluates whether the given words are similar using the specified comparison strategy.
     *
     * @param firstWord  the first word
     * @param secondWord the second word
     * @param strategy   the strategy to use
     * @return Returns {@code true} if the given strategy considers the words similar enough.
     */
    public static boolean areWordsSimilar(String firstWord, Word secondWord, ComparisonStrategy strategy) {
        return areWordsSimilar(new ComparisonContext(firstWord, secondWord.getText(), null, secondWord, false), strategy);
    }

    public static SQLiteConfig getSqLiteConfig() {
        var cfg = new SQLiteConfig();
        cfg.setReadOnly(true);
        cfg.setLockingMode(SQLiteConfig.LockingMode.EXCLUSIVE);
        cfg.setJournalMode(SQLiteConfig.JournalMode.OFF);
        cfg.setSynchronous(SQLiteConfig.SynchronousMode.OFF);
        cfg.setOpenMode(SQLiteOpenMode.NOMUTEX);
        return cfg;
    }
}
