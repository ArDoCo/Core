package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.ipd.parse.luna.agent.AbstractAgent;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.INodeType;
import edu.kit.ipd.parse.luna.tools.ConfigManager;
import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.CorefChain.CorefMention;
import edu.stanford.nlp.ling.CoreAnnotation;
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
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotatorImplementations;
import edu.stanford.nlp.pipeline.AnnotatorPool;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.EnhancedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;

/**
 * Agent to process the input text with Stanford CoreNLP. Processes different Stanford pipeline processes like NER,
 * DepParse, and Coref.
 *
 * The DepParse was originally created by Tobias Hey.
 *
 * @author Jan Keim, Tobias Hey
 *
 */
@MetaInfServices(AbstractAgent.class)
public class StanfordCoreNLPProcessorAgent extends AbstractAgent {

    private static final Logger logger = LoggerFactory.getLogger(StanfordCoreNLPProcessorAgent.class);
    private static final String ID = "stanfordAgent";

    private static final int STANFORD_OFFSET = 1;
    private static final String[] STANFORD_PIPELINE = { "ner", "parse", "depparse", "coref" };

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
    private static final String NER_ATTRIBUTE_NAME = "ner";
    private static final String NO_NER = "O";
    private static final String TOKEN_LEMMA_ATTRIBUTE_NAME = "lemma";

    private static final String COREF_ALGORITHM_ATTRIBUTE = "coref.algorithm";
    private static final List<String> COREF_ALGORITHMS = List.of("neural", "statistical", "clustering");
    private static final String BASIC_DEP_ANNOTATIONS = "BasicDependenciesAnnotation";
    private static final Map<String, Class<? extends CoreAnnotation<SemanticGraph>>> DEP_ANN_TYPES = Map.ofEntries(
            Map.entry(BASIC_DEP_ANNOTATIONS, BasicDependenciesAnnotation.class),
            Map.entry("EnhancedDependenciesAnnotation", EnhancedDependenciesAnnotation.class),
            Map.entry("EnhancedPlusPlusDependenciesAnnotation", EnhancedPlusPlusDependenciesAnnotation.class));

    static final String RELATION_TYPE_LONG = "relationLong";
    static final String RELATION_TYPE_SHORT = "relationShort";
    static final String DEPENDENCY_ARC_TYPE = "typedDependency";

    private WordsToSentencesAnnotator ssplit = null;
    private AnnotatorPool annotatorPool;

    private Class<? extends CoreAnnotation<SemanticGraph>> chosenAnnType;
    private INodeType corefClusterNodeType = null;
    private IArcType corefArcType = null;
    private IArcType dependencyArcType;

    @Override
    public void init() {
        Properties props = ConfigManager.getConfiguration(StanfordCoreNLPProcessorAgent.class);

        chosenAnnType = DEP_ANN_TYPES.getOrDefault(props.getOrDefault("DEPENDENCY_ANNOTATION_TYPE", "EnhancedPlusPlusDependenciesAnnotation"),
                EnhancedPlusPlusDependenciesAnnotation.class);

        Properties stanfordProperties = getStanfordProperties(props);
        annotatorPool = StanfordCoreNLP.getDefaultAnnotatorPool(stanfordProperties, new AnnotatorImplementations());

        super.setId(ID);
    }

    private Properties getStanfordProperties(Properties properties) {
        Properties allStanfordProperties = new Properties(properties);

        if (!allStanfordProperties.contains("parse.type")) {
            allStanfordProperties.put("parse.type", "stanford");
        }

        if (!allStanfordProperties.containsKey("depparse.model")) {
            allStanfordProperties.put("depparse.model", "edu/stanford/nlp/models/parser/nndep/english_UD.gz");
        }

        String corefAlgorithm = allStanfordProperties.getProperty(COREF_ALGORITHM_ATTRIBUTE, COREF_ALGORITHMS.get(0));
        if (!COREF_ALGORITHMS.contains(corefAlgorithm)) {
            logger.warn("Provided CoRef-Algorithm not found. Selecting default.");
        }
        allStanfordProperties.put(COREF_ALGORITHM_ATTRIBUTE, corefAlgorithm);

        return allStanfordProperties;
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

        List<INode> textNodes = ParseUtil.getINodesInOrder(graph);
        Annotation text = prepareDocAnnotation(textNodes);

        for (String pipelineStep : STANFORD_PIPELINE) {
            annotatorPool.get(pipelineStep).annotate(text);
        }

        // TODO currently "parse" is not saved to graph, but might want to do that!

        addDependenciesAndNERToGraph(text, textNodes);
        addCorefToGraph(text, textNodes);
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

    private IArcType createDependencyArcType() {
        IArcType arcType;
        if (!graph.hasArcType(DEPENDENCY_ARC_TYPE)) {
            arcType = graph.createArcType(DEPENDENCY_ARC_TYPE);
            arcType.addAttributeToType(STRING_TYPE, RELATION_TYPE_LONG);
            arcType.addAttributeToType(STRING_TYPE, RELATION_TYPE_SHORT);

        } else {
            arcType = graph.getArcType(DEPENDENCY_ARC_TYPE);
        }
        return arcType;
    }

    private void extendTokenNodeType() {
        INodeType nodeType = null;
        if (graph.hasNodeType(TOKEN_TYPE_NAME)) {
            nodeType = graph.getNodeType(TOKEN_TYPE_NAME);
            if (!nodeType.containsAttribute(SENTENCE_NUMBER, INT_TYPE)) {
                nodeType.addAttributeToType(INT_TYPE, SENTENCE_NUMBER);
            }
        }
    }

    private void prepareGraphForNER() {
        INodeType tokenType = graph.getNodeType(TOKEN_TYPE_NAME);
        if (!tokenType.containsAttribute(NER_ATTRIBUTE_NAME, STRING_TYPE)) {
            tokenType.addAttributeToType(STRING_TYPE, NER_ATTRIBUTE_NAME);
        }
    }

    private void addDependenciesAndNERToGraph(Annotation doc, List<INode> textNodes) {
        prepareGraphForNER();
        dependencyArcType = createDependencyArcType();
        extendTokenNodeType();
        // delete outdated Info
        graph.getArcsOfType(dependencyArcType).forEach(arc -> graph.deleteArc(arc));

        List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
        sentences.sort(Comparator.comparingInt(cm -> cm.get(SentenceIndexAnnotation.class)));
        int offset = 0;
        for (CoreMap sentence : sentences) {
            List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);

            addNERToGraph(textNodes, offset, tokens);

            // add dependencies
            SemanticGraph dependencies = sentence.get(chosenAnnType);
            addDependenciesToGraph(dependencies, offset, textNodes);

            if (tokens.size() <= textNodes.size()) {

                offset += tokens.size();
            }
        }
    }

    private int addNERToGraph(List<INode> textNodes, int startIndex, List<CoreLabel> tokens) {
        int index = startIndex;
        for (CoreLabel token : tokens) {
            INode node = textNodes.get(index);
            String ner = token.ner();
            if (ner == null) {
                ner = NO_NER;
            }
            node.setAttributeValue(NER_ATTRIBUTE_NAME, ner);
            index++;
        }
        return index;
    }

    private void addDependenciesToGraph(SemanticGraph dependencies, int sentenceOffset, List<INode> textNodes) {
        Collection<TypedDependency> typed = dependencies.typedDependencies();
        for (TypedDependency dep : typed) {
            IndexedWord dest = dep.dep();
            IndexedWord src = dep.gov();
            GrammaticalRelation rel = dep.reln();
            INode destNode;
            INode srcNode;
            if (src.index() == 0) {
                // Root Arc
                srcNode = textNodes.get(sentenceOffset + dest.index() - 1);
                destNode = srcNode;
            } else if (dest.index() == 0) {
                // Arc to Root
                srcNode = textNodes.get(sentenceOffset + src.index() - 1);
                destNode = srcNode;

            } else {
                srcNode = textNodes.get(sentenceOffset + src.index() - 1);
                destNode = textNodes.get(sentenceOffset + dest.index() - 1);
            }
            IArc arc = graph.createArc(srcNode, destNode, dependencyArcType);
            arc.setAttributeValue(RELATION_TYPE_SHORT, rel.getShortName());
            arc.setAttributeValue(RELATION_TYPE_LONG, rel.getLongName());
        }
    }

    private void addCorefToGraph(Annotation document, List<INode> text) {
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
