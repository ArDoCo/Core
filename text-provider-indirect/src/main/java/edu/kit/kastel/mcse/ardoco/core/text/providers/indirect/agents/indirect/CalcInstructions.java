package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.indirect;

import java.util.List;

/**
 * This class contains the heuristic to calculate the instruction number for each word
 *
 * @author Markus Kocybik
 * @author Tobias Hey - extended Boundary Keywords for temporal Keywords (2016-07-28) - added punctuation marks
 */
class CalcInstruction {
    private static final List<String> PUNCTUATION_MARKS = List.of(".", ":", ",", ";", "'", "´", "`", "!", "?", "\"");
    private static final List<String> IF_KEYWORDS = List.of("if", "when", "whenever", "unless");
    private static final List<String> THEN_KEYWORDS = List.of("then");
    private static final List<String> ELSE_KEYWORDS = List.of("else", "otherwise", "elseways", "alternatively", "instead", "either", "rather", "oppositely");

    private static final List<String> TEMPORAL_KEYWORDS = List.of("before", "after", "finally", "when", "afterwards", "then", "later", "thereupon", "whereupon",
            "hereupon", "as", "previously");

    private static final List<String> FORMS_OF_TO_BE = List.of("be", "am", "'m", "is", "'s", "are", "'re");

    /**
     * This method calculates the instruction number for each word of the input text.
     *
     * @param words each element represents one word of the input text
     * @param pos   the pos tags for each word
     * @return the instruction number for each word
     *
     * @throws IllegalArgumentException throws an exception if word array and pos array have different lengths
     */
    static int[] calculateInstructionNumber(List<String> words, String[] pos) throws IllegalArgumentException {
        if (words.size() == pos.length) {
            var list = new int[words.size()];
            var instrNr = 0;
            var verbCounter = 0;
            var lastWasPunctuation = false;
            for (var i = 0; i < words.size(); i++) {

                if (isInstructionBoundary(words.get(i))) {
                    // no verb in between boundaries resets instructionNumber
                    // and extends previous instruction
                    if (verbCounter == 0) {
                        if (!lastWasPunctuation) {
                            resetLastInstruction(list, instrNr);
                            list[i] = instrNr;
                        } else {
                            list[i] = instrNr;
                            lastWasPunctuation = false;
                        }

                    } else if (i == words.size() - 1) {
                        list[i] = instrNr;
                        lastWasPunctuation = false;
                    } else if (PUNCTUATION_MARKS.contains(words.get(i).toLowerCase())) {
                        verbCounter = 0;
                        list[i] = instrNr;
                        lastWasPunctuation = true;
                        instrNr++;
                    } else {
                        verbCounter = 0;
                        instrNr++;
                        list[i] = instrNr;
                        lastWasPunctuation = false;
                    }
                } else {
                    // search Verb
                    if (verbCounter == 0) {
                        // special case: two verbs in a row
                        if (pos[i].startsWith("VB") && i < words.size() - 1 && pos[i + 1].startsWith("VB")) {
                            list[i] = instrNr;
                            list[i + 1] = instrNr;
                            i++;
                            verbCounter++;
                        }
                        // verb found
                        else if (pos[i].startsWith("VB")) {
                            list[i] = instrNr;
                            if (!isGerund(words, i)) {
                                verbCounter++;
                            }
                        }
                        // no verb found
                        else {
                            list[i] = instrNr;
                        }
                    } else {
                        // another verb also initiates a new instruction
                        // (imperative sentence)
                        if (pos[i].startsWith("VB") && !isGerund(words, i) && !isToInfinitive(pos, i)) {
                            instrNr++;
                            verbCounter = 0;
                            i--; // repeat loop
                        } else {
                            list[i] = instrNr;
                        }
                    }
                }

            }
            return list;
        } else {
            throw new IllegalArgumentException("word array and pos array have different lengths");
        }

    }

    private static boolean isToInfinitive(String[] pos, int position) {
        if (position > 0) {
            return pos[position].equals("VB") && (pos[position - 1].equals("TO"));
        } else {
            return false;
        }
    }

    private static boolean isGerund(List<String> words, int position) {
        if (position > 0) {
            return words.get(position).endsWith("ing") && !FORMS_OF_TO_BE.contains(words.get(position - 1).toLowerCase());
        } else {
            return words.get(position).endsWith("ing");
        }
    }

    private static boolean isInstructionBoundary(String word) {
        return word.equalsIgnoreCase("and") || word.equalsIgnoreCase("or") || word.equalsIgnoreCase("but") || TEMPORAL_KEYWORDS.contains(word.toLowerCase())
                || PUNCTUATION_MARKS.contains(word.toLowerCase()) || IF_KEYWORDS.contains(word.toLowerCase()) || THEN_KEYWORDS.contains(word.toLowerCase())
                || ELSE_KEYWORDS.contains(word.toLowerCase());
    }

    private static void resetLastInstruction(int[] list, int instrNum) {
        var number = 0;
        for (var i = 0; i < list.length; i++) {
            if (list[i] > number && list[i] < instrNum) {
                number = list[i];
            }
            if (list[i] == instrNum) {
                list[i] = number;
            }
        }

    }
}
