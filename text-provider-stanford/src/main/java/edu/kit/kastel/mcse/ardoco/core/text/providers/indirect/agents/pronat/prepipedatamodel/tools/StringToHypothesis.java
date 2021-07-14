package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.pronat.prepipedatamodel.tools;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.pronat.prepipedatamodel.PrePipelineData;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.pronat.prepipedatamodel.token.MainHypothesisToken;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;

/**
 * Utility Class for Transforming Strings into Hypothesis for Testing the {@link PrePipelineData}
 *
 * @author Sebastian Weigelt
 * @author Tobias Hey
 * @author Jan Keim
 *
 */
public class StringToHypothesis {

    private StringToHypothesis() {
        throw new IllegalAccessError();
    }

    private static TokenizerFactory<CoreLabel> tokenizerFactory;

    static {
        tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
    }

    /**
     * Returns the specified Transcription as MainHypothesis, by separating the words at whitespaces.
     *
     * @param transcription The whitespace separated Transcription to be transformed
     * @return the list of MainHypothesisToken that represents the transcript
     */
    @Deprecated
    public static List<MainHypothesisToken> stringToMainHypothesis(String transcription) {
        List<MainHypothesisToken> result = new ArrayList<>();
        String[] whiteSpaceSeperated = transcription.trim().split(" ");
        var position = 0;
        for (String element : whiteSpaceSeperated) {
            if (!element.equals("")) {
                MainHypothesisToken token = new MainHypothesisToken(element, position);
                result.add(token);
                position++;
            }

        }
        return result;

    }

    /**
     * Returns the specified Transcription as MainHypothesis, by either separating the words at whitespaces or using the
     * stanford tokenizer.
     *
     * @param transcription The Transcription to be transformed
     * @param withStanford  flag specifying whether stanford is used for tokenizing
     * @return the list of MainHypothesisToken that represents the transcript
     */
    public static List<MainHypothesisToken> stringToMainHypothesis(String transcription, boolean withStanford) {
        List<MainHypothesisToken> result = new ArrayList<>();
        if (!withStanford) {
            String[] whiteSpaceSeperated = transcription.trim().split(" ");
            int position = 0;
            for (String element : whiteSpaceSeperated) {
                if (!element.equals("")) {
                    MainHypothesisToken token = new MainHypothesisToken(element, position);
                    result.add(token);
                    position++;
                }

            }
        } else {
            List<List<String>> tokenized = processSentence(transcription);
            int position = 0;
            for (List<String> sentence : tokenized) {
                for (String word : sentence) {
                    MainHypothesisToken token = new MainHypothesisToken(word, position);
                    result.add(token);
                    position++;
                }
            }
        }
        return result;

    }

    private static List<List<String>> processSentence(String sentence) {
        final List<List<String>> sentences = new ArrayList<>();
        List<String> currSentence = new ArrayList<>();
        final List<CoreLabel> rawWords = tokenizerFactory.getTokenizer(new StringReader(sentence)).tokenize();
        for (final CoreLabel cl : rawWords) {
            currSentence.add(cl.word());
            // TODO only works for the most common sentence splits :-/
            if (cl.word().equals(".") || cl.word().equals("?") || cl.word().equals("!")) {
                sentences.add(currSentence);
                currSentence = new ArrayList<>();
            }
        }
        if (sentences.isEmpty()) {
            sentences.add(currSentence);
        }
        return sentences;
    }

}
