package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.ipd.parse.luna.agent.AbstractAgent;
import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.INodeType;
import edu.kit.ipd.parse.luna.tools.ConfigManager;
import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.CorefChain.CorefMention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.DocIDAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.IsNewlineAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CorefAnnotator;
import edu.stanford.nlp.pipeline.NERCombinerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.util.CoreMap;

@MetaInfServices(AbstractAgent.class)
public class CoreferenceResolution extends AbstractAgent {
    private static final String COREF_ALGORITHM_ATTRIBUTE = "coref.algorithm";
    private static final Logger logger = LoggerFactory.getLogger(CoreferenceResolution.class);
    private static final String ID = "coref";

    private static final int STANFORD_OFFSET = 1;

    private static final String STRING_TYPE = "String";
    private static final String INT_TYPE = "int";
    private static final String COREFERENCE_ARC_TYPE = "coreference";
    private static final String COREF_CLUSTER_NODE_TYPE = "CorefCluster";
    private static final String REPRESENTATIVE_MENTION_ATTRIBUTE = "representativeMention";
    private static final String CLUSTER_ID_ATTRIBUTE = "clusterId";
    private static final String TOKEN_TYPE_NAME = "token";
    private static final String TOKEN_WORD_ATTRIBUTE_NAME = "value";
    private static final String TOKEN_POS_ATTRIBUTE_NAME = "pos";
    private static final String SENTENCE_NUMBER = "sentenceNumber";
    private static final String ANNOTATOR_NAME = "BasicDependenciesAnnotation";
    private static final String TOKEN_LEMMA_ATTRIBUTE_NAME = "lemma";
    private static final List<String> COREF_ALGORITHMS = List.of("neural", "statistical", "clustering");

    private WordsToSentencesAnnotator ssplit = null;
    private NERCombinerAnnotator nerAnnotator;
    private ParserAnnotator parserAnnotator;
    private CorefAnnotator corefAnnotator;

    private INodeType corefClusterNodeType = null;
    private IArcType corefArcType = null;

    @Override
    public void init() {
        // TODO add proper properties
        Properties props = ConfigManager.getConfiguration(CoreferenceResolution.class);
        String corefAlgorithm = props.getProperty(COREF_ALGORITHM_ATTRIBUTE, COREF_ALGORITHMS.get(0));
        if (!COREF_ALGORITHMS.contains(corefAlgorithm)) {
            logger.warn("Provided CoRef-Algorithm not found. Selecting default.");
        }

        try {
            nerAnnotator = new NERCombinerAnnotator();
        } catch (ClassNotFoundException | IOException e) {
            logger.warn(e.getMessage(), e.getCause());
        }
        Properties parserProperties = new Properties();
        parserAnnotator = new ParserAnnotator(ANNOTATOR_NAME, parserProperties);

        Properties corefProperties = new Properties();
        corefProperties.put(COREF_ALGORITHM_ATTRIBUTE, corefAlgorithm);
        corefAnnotator = new CorefAnnotator(corefProperties);

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

        // TODO We need to execute NER and (Dep)Parse before CoRef. Write these infos also in Graph? Create own agents?

        List<INode> textNodes = ParseUtil.getINodesInOrder(graph);
        Annotation text = prepareDocAnnotation(textNodes);
        try {
            nerAnnotator.annotate(text);
        } catch (NumberFormatException e) {
            logger.info("NER had problem with NumberFormat: {}", e.getMessage());
        }

        parserAnnotator.annotate(text); // TODO currently theoretically runs twice, because also executed as agent
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

    private void addToGraph(Annotation document, List<INode> text) {
        for (CorefChain cc : document.get(CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
            addCorefClusterToGraph(cc, text);
        }
    }

    private Map<Integer, List<INode>> createSentencesMap(List<INode> text) {
        Map<Integer, List<INode>> sentences = new HashMap<>();

        for (INode node : text) {
            Integer nodeSentenceNumber = (Integer) node.getAttributeValue(SENTENCE_NUMBER);
            sentences.putIfAbsent(nodeSentenceNumber, new ArrayList<>());
            sentences.get(nodeSentenceNumber).add(node);
        }
        return sentences;
    }

    private void addCorefClusterToGraph(CorefChain corefChain, List<INode> text) {
        Map<Integer, List<INode>> sentences = createSentencesMap(text);

        int chainId = corefChain.getChainID();
        CorefMention representativeMention = corefChain.getRepresentativeMention();
        String representativeMentionSpan = representativeMention.mentionSpan;
        INode corefClusterNode = createCorefClusterNode(chainId, representativeMentionSpan);

        for (CorefMention mention : corefChain.getMentionsInTextualOrder()) {
            int sentenceNumber = mention.sentNum;
            int positionStart = mention.startIndex;
            int positionEnd = mention.endIndex;
            for (int i = positionStart; i < positionEnd; i++) {
                INode node = sentences.get(sentenceNumber - STANFORD_OFFSET).get(i - STANFORD_OFFSET);
                createCorefClusterArc(node, corefClusterNode);
            }
            if (logger.isInfoEnabled()) {
                String logText = "\tMention: " + mention.mentionSpan;
                logger.info(logText);
            }
        }
    }

    private void createCorefClusterArc(INode node, INode corefClusterNode) {
        graph.createArc(node, corefClusterNode, getCorefArcType());
    }

    private INode createCorefClusterNode(int chainId, String representativeMentionSpan) {
        if (logger.isInfoEnabled()) {
            String logText = "Cluster with ID " + chainId + " and Representative Mention \"" + representativeMentionSpan + "\"";
            logger.info(logText);
        }

        INodeType nodeType = getCorefClusterNodeType();
        INode clusterNode = graph.createNode(nodeType);
        clusterNode.setAttributeValue(CLUSTER_ID_ATTRIBUTE, chainId);
        clusterNode.setAttributeValue(REPRESENTATIVE_MENTION_ATTRIBUTE, representativeMentionSpan);

        return clusterNode;
    }

    private IArcType getCorefArcType() {
        if (corefArcType == null) {
            corefArcType = graph.createArcType(COREFERENCE_ARC_TYPE);
        }
        return corefArcType;
    }

    private INodeType getCorefClusterNodeType() {
        if (corefClusterNodeType == null) {
            corefClusterNodeType = graph.createNodeType(COREF_CLUSTER_NODE_TYPE);
            if (!corefClusterNodeType.containsAttribute(CLUSTER_ID_ATTRIBUTE, INT_TYPE)) {
                corefClusterNodeType.addAttributeToType(INT_TYPE, CLUSTER_ID_ATTRIBUTE);
            }
            if (!corefClusterNodeType.containsAttribute(REPRESENTATIVE_MENTION_ATTRIBUTE, STRING_TYPE)) {
                corefClusterNodeType.addAttributeToType(STRING_TYPE, REPRESENTATIVE_MENTION_ATTRIBUTE);
            }
        }
        return corefClusterNodeType;
    }
}
