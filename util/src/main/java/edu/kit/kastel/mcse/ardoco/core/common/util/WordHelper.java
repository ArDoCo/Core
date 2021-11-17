package edu.kit.kastel.mcse.ardoco.core.common.util;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.text.POSTag;

/**
 * The Class WordHelper contains some helper methods to work with words.
 */
public final class WordHelper {

    private WordHelper() {
        throw new IllegalAccessError();
    }

    /**
     * Checks for determiner as previous word.
     *
     * @param word the word
     * @return true, if found
     */
    public static boolean hasDeterminerAsPreWord(IWord word) {

        IWord preWord = word.getPreWord();
        if (preWord == null) {
            return false;
        }

        var prePosTag = preWord.getPosTag();
        return POSTag.DETERMINER == prePosTag;

    }

    /**
     * Checks for indirect determiner as previous word.
     *
     * @param word the word
     * @return true, if found
     */
    public static boolean hasIndirectDeterminerAsPreWord(IWord word) {
        return hasDeterminerAsPreWord(word) && ("a".equalsIgnoreCase(word.getText()) || "an".equalsIgnoreCase(word.getText()));
    }

    /**
     * Gets the incoming dependency tags.
     *
     * @param word the word
     * @return the incoming dependency tags
     */
    public static ImmutableList<DependencyTag> getIncomingDependencyTags(IWord word) {
        return Lists.immutable.with(DependencyTag.values()).select(d -> !word.getWordsThatAreDependentOnThis(d).isEmpty());
    }

    /**
     * Gets the outgoing dependency tags.
     *
     * @param word the word
     * @return the outgoing dependency tags
     */
    public static ImmutableList<DependencyTag> getOutgoingDependencyTags(IWord word) {
        return Lists.immutable.with(DependencyTag.values()).select(d -> !word.getWordsThatAreDependencyOfThis(d).isEmpty());
    }

    public static boolean isVerb(IWord word) {
        String tag = word.getPosTag().getTag();
        return tag.startsWith("VB") || tag.startsWith("MD");
    }

    public static boolean isAdjective(IWord word) {
        String tag = word.getPosTag().getTag();
        return tag.startsWith("JJ");
    }

    public static boolean isAdverb(IWord word) {
        String tag = word.getPosTag().getTag();
        return tag.startsWith("RB");
    }

    public static boolean isNoun(IWord word) {
        String tag = word.getPosTag().getTag();
        return tag.startsWith("NN");
    }

    public static boolean isPronoun(IWord word) {
        String tag = word.getPosTag().getTag();
        return tag.startsWith("PR") || tag.startsWith("WP");
    }
}
