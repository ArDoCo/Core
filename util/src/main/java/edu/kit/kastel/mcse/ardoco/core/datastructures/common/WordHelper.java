package edu.kit.kastel.mcse.ardoco.core.datastructures.common;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.POSTag;

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
        return POSTag.DETERMINER.equals(prePosTag);

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
    public static List<DependencyTag> getIncomingDependencyTags(IWord word) {
        return Arrays.stream(DependencyTag.values()).filter(d -> !word.getWordsThatAreDependentOnThis(d).isEmpty()).collect(Collectors.toList());
    }

    /**
     * Gets the outgoing dependency tags.
     *
     * @param word the word
     * @return the outgoing dependency tags
     */
    public static List<DependencyTag> getOutgoingDependencyTags(IWord word) {
        return Arrays.stream(DependencyTag.values()).filter(d -> !word.getWordsThatAreDependencyOfThis(d).isEmpty()).collect(Collectors.toList());
    }
}
