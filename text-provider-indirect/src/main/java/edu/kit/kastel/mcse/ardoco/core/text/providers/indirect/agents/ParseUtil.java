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
