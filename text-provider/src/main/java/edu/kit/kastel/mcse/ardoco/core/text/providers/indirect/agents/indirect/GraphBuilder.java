/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.indirect;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.parse.luna.data.AbstractPipelineData;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PipelineDataCastException;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.INodeType;
import edu.kit.ipd.parse.luna.graph.ParseGraph;
import edu.kit.ipd.parse.luna.pipeline.IPipelineStage;
import edu.kit.ipd.parse.luna.pipeline.PipelineStageException;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.pronat.prepipedatamodel.PrePipelineData;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.pronat.prepipedatamodel.token.SRLToken;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.pronat.prepipedatamodel.token.Token;

@MetaInfServices(IPipelineStage.class)
public class GraphBuilder implements IPipelineStage {

    private static final String INT = "int";
    private static final String DOUBLE_LOWERCASE = "double";
    private static final String DOUBLE = "Double";
    private static final String STRING = "String";
    private static final String EVENT_TYPES_ATTRIBUTE = "eventTypes";
    private static final String FRAME_NET_FRAMES_ATTRIBUTE = "frameNetFrames";
    private static final String VERB_NET_FRAMES_ATTRIBUTE = "verbNetFrames";
    private static final String PROP_BANK_ROLESET_DESCR_ATTRIBUTE = "propBankRolesetDescr";
    private static final String PROP_BANK_ROLESET_ID_ATTRIBUTE = "propBankRolesetID";
    private static final String CORRESPONDING_VERB_ATTRIBUTE = "correspondingVerb";
    private static final String ROLE_CONFIDENCE_ATTRIBUTE = "roleConfidence";
    private static final String FN_ROLE_ATTRIBUTE = "fnRole";
    private static final String VN_ROLE_ATTRIBUTE = "vnRole";
    private static final String PB_ROLE_ATTRIBUTE = "pbRole";
    private static final String ROLE_ATTRIBUTE = "role";
    private static final String NUMBER_ATTRIBUTE = "number";
    private static final String SENTENCE_NUMBER_ATTRIBUTE = "sentenceNumber";
    private static final String VERIFIED_BY_DIALOG_AGENT_ATTRIBUTE = "verifiedByDialogAgent";
    private static final String STEM_ATTRIBUTE = "stem";
    private static final String LEMMA_ATTRIBUTE = "lemma";
    private static final String NER_ATTRIBUTE = "ner";
    private static final String END_TIME_ATTRIBUTE = "endTime";
    private static final String START_TIME_ATTRIBUTE = "startTime";
    private static final String ALTERNATIVES_COUNT_ATTRIBUTE = "alternativesCount";
    private static final String ASR_CONFIDENCE_ATTRIBUTE = "asrConfidence";
    private static final String POSITION_ATTRIBUTE = "position";
    private static final String INSTRUCTION_NUMBER_ATTRIBUTE = "instructionNumber";
    private static final String SUCCESSORS_ATTRIBUTE = "successors";
    private static final String PREDECESSORS_ATTRIBUTE = "predecessors";
    private static final String TYPE_ATTRIBUTE = "type";
    private static final String CHUNK_ATTRIBUTE = "chunkName";
    private static final String CHUNK_IOB_ATTRIBUTE = "chunkIOB";
    private static final String POS_ATTRIBUTE = "pos";
    private static final String WORD_ATTRIBUTE = "value";
    private static final String VALUE_ATTRIBUTE = "value";

    private static final String ID = "graphBuilder";

    private static final String RELATION_ARC_TYPE = "relation";
    private static final String TOKEN_NODE_TYPE = "token";
    private static final String ALTERNATIVE_ARC_TYPE = "alternative";
    private static final String ALTERNATIVE_TOKEN_NODE_TYPE = "alternative_token";
    private static final String SRL_ARC_TYPE = "srl";

    @Override
    public void init() {
        // Empty
    }

    @Override
    public void exec(AbstractPipelineData data) throws PipelineStageException {
        // try to get data as pre pipeline data. If this fails, return
        PrePipelineData prePipeData;
        try {
            prePipeData = (PrePipelineData) data.asPrePipelineData();
        } catch (PipelineDataCastException e) {
            var msg = "Cannot process on data - PipelineData unreadable";
            throw new PipelineStageException(msg, e);
        }

        try {
            List<Token> tokens = prePipeData.getTaggedHypothesis(0);
            prePipeData.setGraph(GraphBuilder.generateGraphFromTokens(tokens));
        } catch (MissingDataException e) {
            var msg = "No main tagged hypothesis given!";
            throw new PipelineStageException(msg, e);
        }

    }

    private static void createTokenNodes(IGraph graph, INodeType wordType, IArcType arcType, Map<Token, INode> nodesForTokens, HashSet<SRLToken> srlTokens,
            List<Token> tokens) {
        INode lastNode = null;
        for (Token tok : tokens) {
            INode node = graph.createNode(wordType);
            node.setAttributeValue(GraphBuilder.WORD_ATTRIBUTE, tok.getWord());
            node.setAttributeValue(GraphBuilder.POS_ATTRIBUTE, tok.getPos().toString());
            node.setAttributeValue(GraphBuilder.CHUNK_IOB_ATTRIBUTE, tok.getChunkIOB().toString());
            node.setAttributeValue(GraphBuilder.CHUNK_ATTRIBUTE, tok.getChunk().getName());
            node.setAttributeValue(GraphBuilder.PREDECESSORS_ATTRIBUTE, tok.getChunk().getPredecessor());
            node.setAttributeValue(GraphBuilder.SUCCESSORS_ATTRIBUTE, tok.getChunk().getSuccessor());
            node.setAttributeValue(GraphBuilder.INSTRUCTION_NUMBER_ATTRIBUTE, tok.getInstructionNumber());
            node.setAttributeValue(GraphBuilder.POSITION_ATTRIBUTE, tok.getPosition());
            node.setAttributeValue(GraphBuilder.TYPE_ATTRIBUTE, tok.getType().toString());
            node.setAttributeValue(GraphBuilder.NER_ATTRIBUTE, tok.getNer());
            node.setAttributeValue(GraphBuilder.LEMMA_ATTRIBUTE, tok.getLemma());
            node.setAttributeValue(GraphBuilder.STEM_ATTRIBUTE, tok.getStem());
            node.setAttributeValue(GraphBuilder.VERIFIED_BY_DIALOG_AGENT_ATTRIBUTE, false);
            node.setAttributeValue(GraphBuilder.SENTENCE_NUMBER_ATTRIBUTE, tok.getSentenceNumber());

            nodesForTokens.put(tok, node);

            // add arcs between main hyp tokens
            if (lastNode != null) {
                IArc arc = graph.createArc(lastNode, node, arcType);
                arc.setAttributeValue(GraphBuilder.WORD_ATTRIBUTE, "NEXT");
            }

            if (tok instanceof SRLToken srlToken) {
                srlTokens.add(srlToken);
            }

            lastNode = node;
        }

        if (!srlTokens.isEmpty()) {
            IArcType srlArcType;

            if (graph.hasArcType(GraphBuilder.SRL_ARC_TYPE)) {
                srlArcType = graph.getArcType(GraphBuilder.SRL_ARC_TYPE);
            } else {
                srlArcType = graph.createArcType(GraphBuilder.SRL_ARC_TYPE);
                srlArcType.addAttributeToType(STRING, GraphBuilder.ROLE_ATTRIBUTE);
                srlArcType.addAttributeToType(STRING, GraphBuilder.PB_ROLE_ATTRIBUTE);
                srlArcType.addAttributeToType(STRING, GraphBuilder.VN_ROLE_ATTRIBUTE);
                srlArcType.addAttributeToType(STRING, GraphBuilder.FN_ROLE_ATTRIBUTE);
                srlArcType.addAttributeToType(DOUBLE, GraphBuilder.ROLE_CONFIDENCE_ATTRIBUTE);
                srlArcType.addAttributeToType(STRING, GraphBuilder.CORRESPONDING_VERB_ATTRIBUTE);
                srlArcType.addAttributeToType(STRING, GraphBuilder.PROP_BANK_ROLESET_ID_ATTRIBUTE);
                srlArcType.addAttributeToType(STRING, GraphBuilder.PROP_BANK_ROLESET_DESCR_ATTRIBUTE);
                srlArcType.addAttributeToType(STRING, GraphBuilder.VERB_NET_FRAMES_ATTRIBUTE);
                srlArcType.addAttributeToType(STRING, GraphBuilder.FRAME_NET_FRAMES_ATTRIBUTE);
                srlArcType.addAttributeToType(STRING, GraphBuilder.EVENT_TYPES_ATTRIBUTE);
            }

            for (SRLToken srlToken : srlTokens) {
                GraphBuilder.createSRLArcs(srlToken, srlArcType, nodesForTokens, graph);
            }
        }
    }

    private static INodeType getOrCreateINodeType(IGraph graph, String requirementNodeType) {
        INodeType reqType;
        if (graph.hasNodeType(requirementNodeType)) {
            reqType = graph.getNodeType(requirementNodeType);
        } else {
            reqType = graph.createNodeType(requirementNodeType);
        }
        return reqType;
    }

    private static IGraph generateGraphFromTokens(List<Token> tokens) {
        var indirectGraph = new IndirectGraph().invoke();
        IGraph graph = indirectGraph.getGraph();
        INodeType wordType = indirectGraph.getWordType();
        IArcType arcType = indirectGraph.getArcType();
        IArcType altRelType;

        var altType = GraphBuilder.getOrCreateINodeType(graph, GraphBuilder.ALTERNATIVE_TOKEN_NODE_TYPE);

        altRelType = GraphBuilder.getOrCreateIArcType(graph, GraphBuilder.ALTERNATIVE_ARC_TYPE);

        altType.addAttributeToType(STRING, GraphBuilder.WORD_ATTRIBUTE);
        altType.addAttributeToType(STRING, GraphBuilder.TYPE_ATTRIBUTE);
        altType.addAttributeToType(INT, GraphBuilder.POSITION_ATTRIBUTE);
        altType.addAttributeToType(DOUBLE_LOWERCASE, GraphBuilder.ASR_CONFIDENCE_ATTRIBUTE);
        altType.addAttributeToType(DOUBLE_LOWERCASE, GraphBuilder.START_TIME_ATTRIBUTE);
        altType.addAttributeToType(DOUBLE_LOWERCASE, GraphBuilder.END_TIME_ATTRIBUTE);

        altRelType.addAttributeToType(INT, GraphBuilder.NUMBER_ATTRIBUTE);

        Map<Token, INode> nodesForTokens = new TreeMap<>();
        HashSet<SRLToken> srlTokens = new HashSet<>();

        GraphBuilder.createTokenNodes(graph, wordType, arcType, nodesForTokens, srlTokens, tokens);
        return graph;
    }

    private static IArcType getOrCreateIArcType(IGraph graph, String alternativeArcType) {
        IArcType altRelType;
        if (graph.hasArcType(alternativeArcType)) {
            altRelType = graph.getArcType(alternativeArcType);
        } else {
            altRelType = graph.createArcType(alternativeArcType);
        }
        return altRelType;
    }

    private static void createSRLArcs(SRLToken srlToken, IArcType srlArcType, Map<Token, INode> nodesForTokens, IGraph graph) {
        Token last = srlToken;
        for (Token verb : srlToken.getVerbTokens()) {
            IArc arc = graph.createArc(nodesForTokens.get(last), nodesForTokens.get(verb), srlArcType);
            GraphBuilder.setSharedSRLArcInfos(srlToken, arc, "V");
            last = verb;
        }
        for (String role : srlToken.getDependentRoles()) {
            if (!role.equals("V")) {
                last = srlToken;
                for (Token roleToken : srlToken.getTokensOfRole(role)) {
                    IArc arc = graph.createArc(nodesForTokens.get(last), nodesForTokens.get(roleToken), srlArcType);
                    GraphBuilder.setSharedSRLArcInfos(srlToken, arc, role);
                    String[] roleDescription;
                    if ((roleDescription = srlToken.getRoleDescriptions(role)) != null) {

                        arc.setAttributeValue(GraphBuilder.PB_ROLE_ATTRIBUTE, roleDescription[0]);
                        arc.setAttributeValue(GraphBuilder.VN_ROLE_ATTRIBUTE, roleDescription[1]);
                        arc.setAttributeValue(GraphBuilder.FN_ROLE_ATTRIBUTE, roleDescription[2]);

                    }
                    last = roleToken;
                }
            }
        }
    }

    private static void setSharedSRLArcInfos(SRLToken srlToken, IArc arc, String role) {
        arc.setAttributeValue(GraphBuilder.ROLE_ATTRIBUTE, role);
        arc.setAttributeValue(GraphBuilder.CORRESPONDING_VERB_ATTRIBUTE, srlToken.getCorrespondingVerb());
        arc.setAttributeValue(GraphBuilder.ROLE_CONFIDENCE_ATTRIBUTE, srlToken.getRoleConfidence());
        arc.setAttributeValue(GraphBuilder.PROP_BANK_ROLESET_ID_ATTRIBUTE, srlToken.getPbRolesetID());
        arc.setAttributeValue(GraphBuilder.PROP_BANK_ROLESET_DESCR_ATTRIBUTE, srlToken.getPbRolesetDescr());
        arc.setAttributeValue(GraphBuilder.VERB_NET_FRAMES_ATTRIBUTE, Arrays.toString(srlToken.getVnFrames()));
        arc.setAttributeValue(GraphBuilder.FRAME_NET_FRAMES_ATTRIBUTE, Arrays.toString(srlToken.getFnFrames()));
        arc.setAttributeValue(GraphBuilder.EVENT_TYPES_ATTRIBUTE, Arrays.toString(srlToken.getEventTypes()));
    }

    @Override
    public String getID() {
        return GraphBuilder.ID;
    }

    private static class IndirectGraph {
        private static final String BOOLEAN = "boolean";
        private IGraph graph = null;
        private INodeType wordType = null;
        private IArcType arcType = null;

        IGraph getGraph() {
            return graph;
        }

        INodeType getWordType() {
            return wordType;
        }

        IArcType getArcType() {
            return arcType;
        }

        IndirectGraph invoke() {
            graph = new ParseGraph();

            // get or create the types
            wordType = GraphBuilder.getOrCreateINodeType(graph, GraphBuilder.TOKEN_NODE_TYPE);

            arcType = GraphBuilder.getOrCreateIArcType(graph, GraphBuilder.RELATION_ARC_TYPE);

            wordType.addAttributeToType(STRING, GraphBuilder.WORD_ATTRIBUTE);
            wordType.addAttributeToType(STRING, GraphBuilder.POS_ATTRIBUTE);
            wordType.addAttributeToType(STRING, GraphBuilder.CHUNK_IOB_ATTRIBUTE);
            wordType.addAttributeToType(STRING, GraphBuilder.CHUNK_ATTRIBUTE);
            wordType.addAttributeToType(STRING, GraphBuilder.TYPE_ATTRIBUTE);
            wordType.addAttributeToType(INT, GraphBuilder.PREDECESSORS_ATTRIBUTE);
            wordType.addAttributeToType(INT, GraphBuilder.SUCCESSORS_ATTRIBUTE);
            wordType.addAttributeToType(INT, GraphBuilder.INSTRUCTION_NUMBER_ATTRIBUTE);
            wordType.addAttributeToType(INT, GraphBuilder.POSITION_ATTRIBUTE);
            wordType.addAttributeToType(DOUBLE_LOWERCASE, GraphBuilder.ASR_CONFIDENCE_ATTRIBUTE);
            wordType.addAttributeToType(INT, GraphBuilder.ALTERNATIVES_COUNT_ATTRIBUTE);
            wordType.addAttributeToType(DOUBLE_LOWERCASE, GraphBuilder.START_TIME_ATTRIBUTE);
            wordType.addAttributeToType(DOUBLE_LOWERCASE, GraphBuilder.END_TIME_ATTRIBUTE);
            wordType.addAttributeToType(STRING, GraphBuilder.NER_ATTRIBUTE);
            wordType.addAttributeToType(STRING, GraphBuilder.LEMMA_ATTRIBUTE);
            wordType.addAttributeToType(STRING, GraphBuilder.STEM_ATTRIBUTE);
            wordType.addAttributeToType(BOOLEAN, GraphBuilder.VERIFIED_BY_DIALOG_AGENT_ATTRIBUTE);
            wordType.addAttributeToType(INT, GraphBuilder.SENTENCE_NUMBER_ATTRIBUTE);

            arcType.addAttributeToType(STRING, GraphBuilder.VALUE_ATTRIBUTE);
            return this;
        }
    }
}
