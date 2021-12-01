/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.indirect;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.kit.ipd.parse.luna.tools.ConfigManager;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.ParseUtil;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.pronat.prepipedatamodel.token.Token;
import edu.stanford.nlp.ling.CoreAnnotations.SentenceIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.CoreMap;

/**
 * This class represents a facade for Stanford CoreNLP
 *
 * @author Markus Kocybik
 * @author Tobias Hey - MaxentTagger only loaded once (2016-07-28) - adapted interface and added stemming - add lemma
 *         replacement from config (2020-03-20)
 * @author Jan Keim - update model loading and minor style fixes
 */
public class Stanford {

    private final MaxentTagger tagger;
    private final Morphology morph;
    private final WordsToSentencesAnnotator ssplit;
    private final Map<String, Lemma> lemmas;

    Stanford() {
        Properties props = ConfigManager.getConfiguration(Stanford.class);
        var taggerModel = props.getProperty("TAGGER_MODEL");
        InputStream taggerModelStream = getClass().getResourceAsStream(taggerModel);
        tagger = new MaxentTagger(taggerModelStream);
        lemmas = initLemmas(props);
        morph = new Morphology();
        ssplit = new WordsToSentencesAnnotator();
    }

    private Map<String, Lemma> initLemmas(Properties props) {
        Map<String, Lemma> newLemmas = new HashMap<>();
        String[] lemmaProps = props.getProperty("LEMMAS", "").split(";");
        for (String lemmaProp : lemmaProps) {
            var newLemma = new Lemma(lemmaProp);
            newLemmas.put(newLemma.getPrev() + "/" + newLemma.getPos(), newLemma);
        }
        return newLemmas;
    }

    private class Lemma {
        private final String prev;
        private final String pos;
        private final String value;

        Lemma(String props) {
            String[] split = props.split("/");
            prev = split[0];
            pos = split[1];
            value = split[2];
        }

        String getPrev() {
            return prev;
        }

        public String getPos() {
            return pos;
        }

        public String getLemma() {
            return value;
        }
    }

    /**
     * This method realizes pos tagging with the Stanford POS Tagger
     *
     * @param text the input text. Each element in the array represents one word.
     * @return the pos tags
     */
    String[] posTag(List<String> text) {

        List<HasWord> sent = SentenceUtils.toWordList(text.toArray(new String[text.size()]));
        List<TaggedWord> taggedSent = tagger.tagSentence(sent);
        var result = new String[taggedSent.size()];
        for (var i = 0; i < taggedSent.size(); i++) {
            result[i] = taggedSent.get(i).tag();

        }
        return result;
    }

    int[] sentenceNumberEndIndices(List<String> text) {
        final var charBegin = 0;
        var begin = 0;
        var end = 0;
        List<CoreLabel> inputTokens = new ArrayList<>();
        var input = new StringBuilder();
        for (String word : text) {
            input.append(word).append(" ");
            end = begin + word.length() - 1;
            var clToken = ParseUtil.createCoreLabelToken(word, begin, end);
            inputTokens.add(clToken);
            begin += word.length() + 1;
        }
        var doc = ParseUtil.createDocument(charBegin, end, inputTokens, input.toString().trim());
        ssplit.annotate(doc);
        List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
        sentences.sort(Comparator.comparingInt(cm -> cm.get(SentenceIndexAnnotation.class)));
        var result = new int[sentences.size()];
        var offset = 0;
        for (var i = 0; i < sentences.size(); i++) {
            CoreMap sentence = sentences.get(i);
            List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
            result[i] = tokens.size() - 1 + offset;
            offset += tokens.size();
        }
        return result;
    }

    /**
     * adds stem and lemma to the specified {@link Token}s
     *
     * @param text the {@link Token} to stem and lemmatize
     */
    void stemAndLemmatize(List<Token> text) {
        for (Token token : text) {
            String stem = morph.stem(token.getWord());
            String lemma = morph.lemma(token.getWord(), token.getPos().getTag(), true);
            if (token.getWord().equals(lemma) && lemmas.get(token.getWord() + "/" + token.getPos().getTag()) != null) {
                lemma = lemmas.get(token.getWord() + "/" + token.getPos().getTag()).getLemma();
            }
            token.setLemma(lemma);
            token.setStem(stem);
        }
    }

}
