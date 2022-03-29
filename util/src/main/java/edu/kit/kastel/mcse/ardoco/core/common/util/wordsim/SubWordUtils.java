package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides utility methods for recognizing sub words in strings.
 */
public class SubWordUtils {

    /**
     * Checks whether the given string contains sub words.
     *
     * @param string the string to check
     * @return Returns {@code true}, if the strings contains sub words.
     */
    public static boolean hasSubWords(String string) {
        // 1. Check if string has any normal chars
        boolean hasNormalChars = false;

        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);

            if (c != ' ' && c != '.' && c != '-') {
                hasNormalChars = true;
                break;
            }
        }

        if (!hasNormalChars) {
            return false;
        }

        // 2. Check for split characters
        boolean previousCharWasLowercase = false;

        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);

            if (c == ' ' || c == '.' || c == '-') {
                return true;
            }

            if (Character.isUpperCase(c) && previousCharWasLowercase) {
                return true;
            }

            previousCharWasLowercase = !Character.isUpperCase(c) && !Character.isDigit(c);
        }

        return false;
    }

    /**
     * Retrieves a list of sub words that are contained in the given string.
     *
     * @param string the string that maybe contains sub words
     * @return the list of sub words, or a list containing the string itself if the string does not contain any
     */
    public static List<String> getSubWords(String string) {
        if (!hasSubWords(string)) {
            return List.of(string);
        }

        var subWordList = new ArrayList<String>();
        var currentSubWord = new StringBuilder();
        var previousCharWasLowercase = false;

        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);

            if (c == ' ' || c == '.' || c == '-') { // TODO: Make this configurable?
                if (!currentSubWord.isEmpty()) {
                    subWordList.add(currentSubWord.toString());
                    currentSubWord.delete(0, currentSubWord.length());
                    previousCharWasLowercase = false;
                } else {
                    previousCharWasLowercase = false;
                }
            } else {
                if (Character.isUpperCase(c) && previousCharWasLowercase) {
                    if (!currentSubWord.isEmpty()) {
                        subWordList.add(currentSubWord.toString());
                        currentSubWord.delete(0, currentSubWord.length());

                        currentSubWord.append(c);
                        previousCharWasLowercase = !Character.isUpperCase(c);
                    } else {
                        // this case should be impossible
                        throw new IllegalStateException("impossible: " + string);
                    }
                } else {
                    currentSubWord.append(c);
                    previousCharWasLowercase = !Character.isUpperCase(c) && !Character.isDigit(c);
                }
            }
        }

        if (!currentSubWord.isEmpty()) {
            subWordList.add(currentSubWord.toString());
        }

        return subWordList;
    }

}
