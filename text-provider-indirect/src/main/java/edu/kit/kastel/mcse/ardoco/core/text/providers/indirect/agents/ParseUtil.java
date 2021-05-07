package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents;

import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.INodeType;
import edu.kit.ipd.parse.luna.graph.ParseGraph;

public class ParseUtil {
    private static final Logger logger = LoggerFactory.getLogger(ParseUtil.class);

    private static final String TOKEN_NODE_TYPE = "token";
    private static final String VALUE = "value";
    private static final String TOKEN_POS_ATTRIBUTE_NAME = "pos";

    private static final String ARC_TYPE_NAME = "relation";
    private static final String ARC_TYPE_VALUE = "NEXT";

    private ParseUtil() {
        throw new IllegalAccessError();
    }

    /**
     * Returns the "main" INodes in order. This means that "token"-type INodes are collected and returned in a sorted
     * list. Sorting is based on the "next"-arc, starting with the first utterance node.
     *
     * @param graph Graph representing an input
     * @return Sorted List of "token"-INodes
     */
    public static List<INode> getINodesInOrder(IGraph graph) {
        if (!(graph instanceof ParseGraph)) {
            logger.error("Graph cannot be processed!");
            return Lists.mutable.empty();
        }
        ParseGraph parseGraph = (ParseGraph) graph;
        IArcType arcType = graph.getArcType(ARC_TYPE_NAME);

        MutableList<INode> orderedNodes = Lists.mutable.empty();
        INode node = parseGraph.getFirstUtteranceNode();
        if (node == null) {
            return orderedNodes;
        }

        while (orderedNodes.add(node)) {
            if (node.getNumberOfOutgoingArcs() < 1) {
                break;
            }
            for (IArc arc : node.getOutgoingArcsOfType(arcType)) {
                Object value = arc.getAttributeValue(VALUE);
                if (value.equals(ARC_TYPE_VALUE)) {
                    node = arc.getTargetNode();
                    break;
                }
            }
        }
        return orderedNodes;
    }

    public static String recreateText(List<INode> textNodes) {
        StringBuilder textBuilder = new StringBuilder();
        List<String> specialPos = List.of("-RRB-", ".", ",", ":", "POS");
        for (int i = 0; i < textNodes.size(); i++) {
            INode node = textNodes.get(i);
            String word = (String) node.getAttributeValue(VALUE);
            String pos = (String) node.getAttributeValue(TOKEN_POS_ATTRIBUTE_NAME);

            if (word.equals("-LRB-")) {
                textBuilder.append("(");
            } else if (word.equals("-RRB-")) {
                textBuilder.append(")");
            } else {
                textBuilder.append(word);
            }

            // Add a space after each word, but only, if the word is not a opening bracket "(" or the next word is
            // special (like "." or ",").
            if (i + 1 < textNodes.size()) {
                INode nextNode = textNodes.get(i + 1);
                String nextPos = (String) nextNode.getAttributeValue(TOKEN_POS_ATTRIBUTE_NAME);
                if (specialPos.contains(nextPos)) {
                    continue;
                }
            }
            if (!"-LRB-".equals(pos)) {
                textBuilder.append(" ");
            }
        }
        return textBuilder.toString().trim();
    }

    /**
     * Returns the String value of the attribute "value" for a given node.
     *
     * @param node the node
     * @return the String value of the attribute "value" for a given node.
     */
    public static String getINodeValue(INode node) {
        return String.valueOf(node.getAttributeValue(VALUE));
    }

    /**
     * Returns whether a given node has type "token" within the given graph
     *
     * @param graph the Graph
     * @param node  the Node that should be checked
     * @return true if the node has type "token"
     */
    public static boolean nodeHasTokenTypeInIGraph(IGraph graph, INode node) {
        return getTokenINodeType(graph).equals(node.getType());
    }

    /**
     * Returns the {@link INodeType} for "token". If the given graph does not have such a type yet, the token is created
     *
     * @param graph Graph to operate on
     * @return The {@link INodeType} for "token".
     */
    public static INodeType getTokenINodeType(IGraph graph) {
        return graph.hasNodeType(TOKEN_NODE_TYPE) ? graph.getNodeType(TOKEN_NODE_TYPE) : graph.createNodeType(TOKEN_NODE_TYPE);
    }

}
