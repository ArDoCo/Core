/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.jetbrains.annotations.NotNull;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteOpenMode;

import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.common.util.AbbreviationDisambiguationHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.equality.EqualityMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.strategy.ComparisonStrategy;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.strategy.SimilarityStrategy;

/**
 * A static class that provides various utility methods to calculate similarity between different kinds of objects. This class statically keeps a reference to a
 * fallback {@link ComparisonStrategy} and a fallback list of word similarity measures. These fallbacks can be changed with the {@link #setMeasures(Collection)}
 * and {@link #setStrategy(ComparisonStrategy)} methods. Any calls to methods that provide their own measures or strategies will not utilize these fallbacks.
 * Any calls that do not provide their own measures or strategies will utilize them. As of right now, no protections against simultaneous write access from
 * multiple threads exist. Therefore, this class is not threadsafe.
 */
public class WordSimUtils implements Serializable {

    private MutableList<WordSimMeasure> measures = Lists.mutable.withAll(WordSimLoader.loadUsingProperties());
    private ComparisonStrategy strategy = ComparisonStrategy.AT_LEAST_ONE;
    private SimilarityStrategy similarityStrategy = SimilarityStrategy.AVERAGE;
    private UnicodeCharacterMatchFunctions characterMatch = UnicodeCharacterMatchFunctions.EQUAL;
    private boolean considerAbbreviations = CommonTextToolsConfig.CONSIDER_ABBREVIATIONS;

    public WordSimUtils() {
    }

    /**
     * Sets which measures should be used for similarity comparison. The specified collection of measures will be used for all subsequent comparisons.
     *
     * @param measures the measures to use
     */
    public void setMeasures(Collection<WordSimMeasure> measures) {
        this.measures = Lists.mutable.withAll(measures);
    }

    /**
     * Adds the specified measure to the measures, which should be used for similarity comparison.
     *
     * @param measure the measure to add
     * @return Whether the measure was added successfully
     */
    public boolean addMeasure(WordSimMeasure measure) {
        return this.measures.add(measure);
    }

    /**
     * Sets the default comparison strategy. The specified strategy will be used for all subsequent comparisons that themselves do not specify a strategy.
     *
     * @param strategy the new default strategy
     */
    public void setStrategy(ComparisonStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Sets the default similarity strategy. The specified strategy will be used for all subsequent comparisons that themselves do not specify a strategy.
     *
     * @param strategy the new default strategy
     */
    public void setStrategy(SimilarityStrategy strategy) {
        this.similarityStrategy = strategy;
    }

    public void setCharacterMatchFunction(@NotNull UnicodeCharacterMatchFunctions characterMatch) {
        this.characterMatch = characterMatch;
    }

    public @NotNull UnicodeCharacterMatchFunctions getCharacterMatchFunction() {
        return this.characterMatch;
    }

    public void setConsiderAbbreviations(boolean considerAbbreviations) {
        this.considerAbbreviations = considerAbbreviations;
    }

    public boolean getConsiderAbbreviations() {
        return this.considerAbbreviations;
    }

    /**
     * Evaluates whether the words from the given {@link ComparisonContext} are similar using the specified comparison strategy.
     *
     * @param ctx      the context
     * @param strategy the strategy
     * @return Returns {@code true} if the given strategy considers the words similar enough.
     */
    public boolean areWordsSimilar(ComparisonContext ctx, ComparisonStrategy strategy) {
        Objects.requireNonNull(ctx);
        Objects.requireNonNull(strategy);

        var firstTerm = ctx.firstTerm();
        var secondTerm = ctx.secondTerm();

        if (getConsiderAbbreviations()) {
            var ambiguatedFirstTerm = AbbreviationDisambiguationHelper.ambiguateAll(firstTerm, true);
            var ambiguatedSecondTerm = AbbreviationDisambiguationHelper.ambiguateAll(secondTerm, true);

            if (!ambiguatedFirstTerm.equals(firstTerm) || !ambiguatedSecondTerm.equals(secondTerm)) {
                if (areWordsSimilar(new ComparisonContext(ambiguatedFirstTerm, ambiguatedSecondTerm, null, null, false, ctx.characterMatch()))) {
                    return true;
                }
            }
        }

        // Currently, we need the split test as it improves results by a lot. In the future, we should try to avoid its requirement
        if (!splitLengthTest(ctx)) {
            return false;
        }

        return strategy.areWordsSimilar(ctx, measures.toList());
    }

    private boolean splitLengthTest(ComparisonContext ctx) {
        var first = ctx.firstTerm().toLowerCase();
        var second = ctx.secondTerm().toLowerCase();
        return (first.split(" ").length == second.split(" ").length);
    }

    /**
     * Evaluates whether the words from the given {@link ComparisonContext} are similar using the default comparison strategy. The default strategy can be
     * changed with the {@link #setStrategy(ComparisonStrategy)} method.
     *
     * @param ctx the context
     * @return Returns {@code true} if the default strategy considers the words similar enough.
     */
    public boolean areWordsSimilar(ComparisonContext ctx) {
        Objects.requireNonNull(ctx);
        return areWordsSimilar(ctx, strategy);
    }

    /**
     * Evaluates whether the given words are similar using the default comparison strategy. The default strategy can be changed with the
     * {@link #setStrategy(ComparisonStrategy)} method.
     *
     * @param firstWord  the first word
     * @param secondWord the second word
     * @return Returns {@code true} if the default strategy considers the words similar enough.
     */
    public boolean areWordsSimilar(String firstWord, String secondWord) {
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
    public boolean areWordsSimilar(String firstWord, String secondWord, ComparisonStrategy strategy) {
        return areWordsSimilar(new ComparisonContext(firstWord, secondWord, false), strategy);
    }

    /**
     * Evaluates whether the given words are similar using the default comparison strategy. The default strategy can be changed with the
     * {@link #setStrategy(ComparisonStrategy)} method.
     *
     * @param firstWord  the first word
     * @param secondWord the second word
     * @return Returns {@code true} if the default strategy considers the words similar enough.
     */
    public boolean areWordsSimilar(Word firstWord, Word secondWord) {
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
    public boolean areWordsSimilar(Word firstWord, Word secondWord, ComparisonStrategy strategy) {
        return areWordsSimilar(new ComparisonContext(firstWord, secondWord, false), strategy);
    }

    /**
     * Evaluates whether the given words are similar using the default comparison strategy. The default strategy can be changed with the
     * {@link #setStrategy(ComparisonStrategy)} method.
     *
     * @param firstWord  the first word
     * @param secondWord the second word
     * @return Returns {@code true} if the default strategy considers the words similar enough.
     */
    public boolean areWordsSimilar(String firstWord, Word secondWord) {
        return areWordsSimilar(new ComparisonContext(firstWord, secondWord.getText(), null, secondWord, false, characterMatch), strategy);
    }

    /**
     * Evaluates whether the given words are similar using the specified comparison strategy.
     *
     * @param firstWord  the first word
     * @param secondWord the second word
     * @param strategy   the strategy to use
     * @return Returns {@code true} if the given strategy considers the words similar enough.
     */
    public boolean areWordsSimilar(String firstWord, Word secondWord, ComparisonStrategy strategy) {
        return areWordsSimilar(new ComparisonContext(firstWord, secondWord.getText(), null, secondWord, false, characterMatch), strategy);
    }

    /**
     * Evaluates the similarity of the given words using the specified similarity strategy.
     *
     * @param firstWord  the first word
     * @param secondWord the second word
     * @param strategy   the strategy to use
     * @param ignoreCase whether to ignore the case during comparison
     * @return Returns similarity in range [0,1]
     */
    public double getSimilarity(String firstWord, String secondWord, SimilarityStrategy strategy, boolean ignoreCase) {
        var measures = this.measures.stream().filter(m -> !(m instanceof EqualityMeasure)).collect(Collectors.toCollection(ArrayList::new));
        if (measures.isEmpty())
            measures.add(new EqualityMeasure());

        return strategy.getSimilarity(new ComparisonContext(ignoreCase ? firstWord.toLowerCase() : firstWord, ignoreCase ?
                secondWord.toLowerCase() :
                secondWord, null, null, false, characterMatch), measures);
    }

    /**
     * Evaluates the similarity of the given words.
     *
     * @param firstWord  the first word
     * @param secondWord the second word
     * @return Returns similarity in range [0,1]
     */
    public double getSimilarity(String firstWord, String secondWord) {
        return getSimilarity(firstWord, secondWord, false);
    }

    /**
     * Evaluates the similarity of the given words.
     *
     * @param firstWord  the first word
     * @param secondWord the second word
     * @param ignoreCase whether to ignore the case during comparison
     * @return Returns similarity in range [0,1]
     */
    public double getSimilarity(String firstWord, String secondWord, boolean ignoreCase) {
        return getSimilarity(firstWord, secondWord, similarityStrategy, ignoreCase);
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
