package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.ipd.parse.luna.agent.AbstractAgent;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.tools.ConfigManager;
import edu.kit.kastel.mcse.ardoco.core.parse.ParseUtil;
import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.CorefChain.CorefMention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.DocIDAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.IsNewlineAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentenceIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.DeterministicCorefAnnotator;
import edu.stanford.nlp.pipeline.NERCombinerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.util.CoreMap;

@MetaInfServices(AbstractAgent.class)
public class CoreferenceResolution extends AbstractAgent {
    private static final String ID = "coref";
    private static final Logger logger = LoggerFactory.getLogger(CoreferenceResolution.class);

    private static final String TOKEN_TYPE_NAME = "token";
    private static final String TOKEN_WORD_ATTRIBUTE_NAME = "value";
    private static final String TOKEN_POS_ATTRIBUTE_NAME = "pos";
    private static final String SENTENCE_NUMBER = "sentenceNumber";
    private static final String ANNOTATOR_NAME = "BasicDependenciesAnnotation";
    private static final String TOKEN_LEMMA_ATTRIBUTE_NAME = "lemma";

    private WordsToSentencesAnnotator ssplit = null;
    private NERCombinerAnnotator nerAnnotator;
    private ParserAnnotator parserAnnotator;
    private DeterministicCorefAnnotator corefAnnotator;

    @Override
    public void init() {
        Properties props = new Properties();
        props = ConfigManager.getConfiguration(CoreferenceResolution.class);

        try {
            nerAnnotator = new NERCombinerAnnotator();
        } catch (ClassNotFoundException | IOException e) {
            logger.warn(e.getMessage(), e.getCause());
        }
        parserAnnotator = new ParserAnnotator(ANNOTATOR_NAME, props);
        corefAnnotator = new DeterministicCorefAnnotator(props);

        super.setId(ID);
    }

    private boolean checkMandatoryPreconditions() {
        return graph.hasNodeType(TOKEN_TYPE_NAME);
    }

    private synchronized WordsToSentencesAnnotator getSentenceSplit() {
        if (ssplit == null) {
            ssplit = new WordsToSentencesAnnotator();
        }
        return ssplit;
    }

    @Override
    protected void exec() {
        if (!checkMandatoryPreconditions()) {
            return;
        }

        System.out.println(corefAnnotator.requires());

        List<INode> textNodes = ParseUtil.getINodesInOrder(graph);
        Annotation text = prepareDocAnnotation(textNodes);
        try {
            nerAnnotator.annotate(text);
        } catch (NumberFormatException e) {
            logger.info("NER had problem with NumberFormat: {}", e.getMessage());
        }

        parserAnnotator.annotate(text); // TODO currently theoretically runs twice.
        // ^ Run or skip this dynamically by loading from the graph and only execute,
        // ^ if you cannot rebuild the annotations (because they do not exist)
        corefAnnotator.annotate(text);
        addToGraph(text, textNodes);
    }

    private Annotation prepareDocAnnotation(List<INode> textNodes) {
        int charBegin = 0;
        int begin = 0;
        int end = 0;
        List<CoreLabel> instruction = new ArrayList<>();
        StringBuilder input = new StringBuilder();
        for (INode node : textNodes) {
            CoreLabel clToken = new CoreLabel();
            String word = (String) node.getAttributeValue(TOKEN_WORD_ATTRIBUTE_NAME);
            input.append(word).append(" ");
            String pos = (String) node.getAttributeValue(TOKEN_POS_ATTRIBUTE_NAME);
            end = begin + word.length() - 1;
            String lemma = (String) node.getAttributeValue(TOKEN_LEMMA_ATTRIBUTE_NAME);
            clToken.setValue(word);
            clToken.setWord(word);
            clToken.setOriginalText(word);
            clToken.setDocID("0");
            clToken.setLemma(lemma);
            clToken.set(PartOfSpeechAnnotation.class, pos);
            clToken.set(CharacterOffsetBeginAnnotation.class, begin);
            clToken.set(CharacterOffsetEndAnnotation.class, end);
            clToken.set(IsNewlineAnnotation.class, false);
            instruction.add(clToken);
            begin += word.length() + 1;
        }
        Annotation doc = new Annotation(input.toString().trim());
        doc.set(DocIDAnnotation.class, "0");
        doc.set(TokensAnnotation.class, instruction);
        doc.set(CharacterOffsetBeginAnnotation.class, charBegin);
        doc.set(CharacterOffsetEndAnnotation.class, end);
        doc.set(CoreAnnotations.TokenBeginAnnotation.class, 0);
        doc.set(CoreAnnotations.TokenEndAnnotation.class, instruction.size());
        addSentenceAnnotation(doc, textNodes);
        return doc;
    }

    private void addSentenceAnnotation(Annotation doc, List<INode> textNodes) {
        if (textNodes.isEmpty() || textNodes.get(0).getAttributeValue(SENTENCE_NUMBER) == null) {
            getSentenceSplit().annotate(doc);
        }

        List<CoreLabel> docTokens = doc.get(CoreAnnotations.TokensAnnotation.class);
        String docID = doc.get(CoreAnnotations.DocIDAnnotation.class);
        List<Sentence> sentences = new ArrayList<>();
        Integer currSentenceNumber = (Integer) textNodes.get(0).getAttributeValue(SENTENCE_NUMBER);
        StringBuilder currSentenceText = new StringBuilder();
        List<CoreLabel> sentenceTokens = new ArrayList<>();
        int index = 0;
        for (INode node : textNodes) {
            if (!node.getAttributeValue(SENTENCE_NUMBER).equals(currSentenceNumber)) {

                sentences.add(new Sentence(currSentenceText.toString(), sentenceTokens, currSentenceNumber));
                currSentenceText = new StringBuilder();
                sentenceTokens = new ArrayList<>();
                currSentenceNumber = (Integer) node.getAttributeValue(SENTENCE_NUMBER);
            }
            sentenceTokens.add(docTokens.get(index));
            currSentenceText.append(docTokens.get(index).word()).append(" ");
            index++;
        }
        if (!sentenceTokens.isEmpty()) {
            sentences.add(new Sentence(currSentenceText.toString(), sentenceTokens, currSentenceNumber));
        }
        int tokenOffset = 0;
        List<CoreMap> result = new ArrayList<>();
        for (Sentence sentence : sentences) {
            Annotation sentenceAnn = new Annotation(sentence.words);
            CoreLabel first = sentence.tokens.get(0);
            CoreLabel last = sentence.tokens.get(sentence.tokens.size() - 1);
            sentenceAnn.set(CoreAnnotations.CharacterOffsetBeginAnnotation.class, first.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class));
            sentenceAnn.set(CoreAnnotations.CharacterOffsetEndAnnotation.class, last.get(CoreAnnotations.CharacterOffsetEndAnnotation.class));
            sentenceAnn.set(CoreAnnotations.TokensAnnotation.class, sentence.tokens);
            sentenceAnn.set(CoreAnnotations.SentenceIndexAnnotation.class, sentence.sentenceIndex);
            sentenceAnn.set(CoreAnnotations.TokenBeginAnnotation.class, tokenOffset);
            tokenOffset += sentence.tokens.size();
            sentenceAnn.set(CoreAnnotations.TokenEndAnnotation.class, tokenOffset);

            if (docID != null) {
                sentenceAnn.set(CoreAnnotations.DocIDAnnotation.class, docID);
            }

            int i = 1;
            for (CoreLabel token : sentence.tokens) {
                token.setIndex(i);
                token.setSentIndex(result.size());
                if (docID != null) {
                    token.setDocID(docID);
                }
                i++;
            }
            result.add(sentenceAnn);
        }
        doc.set(CoreAnnotations.SentencesAnnotation.class, result);

    }

    private final class Sentence {
        final String words;
        final List<CoreLabel> tokens;
        final int sentenceIndex;

        Sentence(String words, List<CoreLabel> tokens, int sentenceIndex) {
            this.words = words;
            this.tokens = tokens;
            this.sentenceIndex = sentenceIndex;
        }
    }

    // TODO write Info to graph!
    private void addToGraph(Annotation document, List<INode> text) {
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        sentences.sort(Comparator.comparingInt(cm -> cm.get(SentenceIndexAnnotation.class)));

        for (CorefChain cc : document.get(CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
            System.out.println("\t" + cc);
            for (CorefMention mention : cc.getMentionsInTextualOrder()) {
                int sentenceNumber = mention.sentNum;
                int positionStart = mention.startIndex;
                int positionEnd = mention.endIndex;
                String mentionSpan = mention.mentionSpan;
                int corefClusterID = mention.corefClusterID;
                String out = String.format("Sentence: %d\nStart: %d\nEnd: %d\nSpan: %s\nClusterID: %d", sentenceNumber, positionStart, positionEnd, mentionSpan,
                        corefClusterID);
                System.out.println(out);
            }
        }
    }

}
