/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.text.providers;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.text.ISentence;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.text.POSTag;

/**
 * @author Jan Keim
 *
 */
public class Sentence implements ISentence {

    private int sentenceNumber;
    private ImmutableList<IWord> words;
    private String text = null;

    public Sentence(int sentenceNumber, ImmutableList<IWord> words) {
        this.sentenceNumber = sentenceNumber;
        this.words = words;
    }

    @Override
    public int getSentenceNumber() {
        return sentenceNumber;
    }

    @Override
    public ImmutableList<IWord> getWords() {
        return words;
    }

    @Override
    public synchronized String getText() {
        if (text == null) {
            text = constructTextFromWords(words);
        }
        return text;
    }

    private static String constructTextFromWords(ImmutableList<IWord> words) {
        if (words.isEmpty()) {
            return "";
        }
        StringBuilder textBuilder = new StringBuilder();

        boolean firstIteration = true;
        for (var word : words) {
            String wordText = word.getText();
            if (!firstIteration && !needsNoSpaceAhead(word) && !needsNoSpaceAfter(word.getPreWord())) {
                textBuilder.append(" ");
            }
            textBuilder.append(wordText);
            firstIteration = false;
        }

        return textBuilder.toString();
    }

    private static boolean needsNoSpaceAhead(IWord word) {
        var pos = word.getPosTag();
        return pos == POSTag.RIGHT_PAREN || pos == POSTag.CLOSER || pos == POSTag.COMMA || pos == POSTag.COLON || pos == POSTag.HYPH
                || pos == POSTag.CLOSE_QUOTE || pos == POSTag.POSSESSIVE_ENDING;
    }

    private static boolean needsNoSpaceAfter(IWord word) {
        var pos = word.getPosTag();
        return pos == POSTag.LEFT_PAREN || pos == POSTag.HYPH;
    }

    public static ImmutableList<ISentence> createSentenceListFromWords(ImmutableList<IWord> words) {
        MutableList<ISentence> sentences = Lists.mutable.empty();
        MutableList<IWord> currSentence = Lists.mutable.empty();
        int currSentenceNo = -1;

        for (var word : words) {
            int wordSentenceNo = word.getSentenceNo();
            if (wordSentenceNo > currSentenceNo) {
                // new sentence
                if (currSentenceNo >= 0) {
                    var sentence = new Sentence(currSentenceNo, currSentence.toImmutable());
                    sentences.add(sentence);
                }
                currSentence = Lists.mutable.empty();
                currSentenceNo = wordSentenceNo;
            }
            currSentence.add(word);
        }
        return sentences.toImmutable();
    }

}
