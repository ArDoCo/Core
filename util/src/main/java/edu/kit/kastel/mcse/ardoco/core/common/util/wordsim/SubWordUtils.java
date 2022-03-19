package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides utility methods for recognizing sub words in strings.
 */
public class SubWordUtils {

    /**
     * TODO
     * @param string
     * @return
     */
    public static boolean hasSubWords(String string) {
        // 1.) Check if string has any normal chars
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

        // 2.) Check for split characters
        boolean previousCharWasLowercase = false;

        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);

            if (c == ' ' || c == '.' || c == '-') {
                return true;
            }

            if (Character.isUpperCase(c) && previousCharWasLowercase) {
                return true;
            }

            previousCharWasLowercase = !Character.isUpperCase(c);
        }

        return false;
    }

    /**
     * TODO
     * @param string
     * @return
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

            if (c == ' ' || c == '.' || c == '-') {
                if (!currentSubWord.isEmpty()) {
                    subWordList.add(currentSubWord.toString());
                    currentSubWord.delete(0, currentSubWord.length());
                } else {
                    currentSubWord.append(c);
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
                    previousCharWasLowercase = !Character.isUpperCase(c);
                }
            }
        }

        if (!currentSubWord.isEmpty()) {
            subWordList.add(currentSubWord.toString());
        }

        return subWordList;
    }

}
