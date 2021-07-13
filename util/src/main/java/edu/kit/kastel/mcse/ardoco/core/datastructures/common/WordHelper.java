package edu.kit.kastel.mcse.ardoco.core.datastructures.common;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.POSTag;

public final class WordHelper {

    private WordHelper() {
        throw new IllegalAccessError();
    }

    public static boolean hasDeterminerAsPreWord(IWord word) {

        IWord preWord = word.getPreWord();
        if (preWord == null) {
            return false;
        }

        var prePosTag = preWord.getPosTag();
        return POSTag.DETERMINER.equals(prePosTag);

    }

    public static boolean hasIndirectDeterminerAsPreWord(IWord word) {
        return hasDeterminerAsPreWord(word) && (word.getText().equalsIgnoreCase("a") || word.getText().equalsIgnoreCase("an"));
    }

    public static List<DependencyTag> getIncomingDependencyTags(IWord word) {
        return Arrays.stream(DependencyTag.values()).filter(d -> !word.getWordsThatAreDependentOnThis(d).isEmpty()).collect(Collectors.toList());
    }

    public static List<DependencyTag> getOutgoingDependencyTags(IWord word) {
        return Arrays.stream(DependencyTag.values()).filter(d -> !word.getWordsThatAreDependencyOfThis(d).isEmpty()).collect(Collectors.toList());
    }
}
