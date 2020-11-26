package modelconnector.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.Pair;
import modelconnector.textExtractor.state.NounMapping;

/**
 * This helper class encapsulates multiple methods, that are used to move on a graph or access node entries.
 *
 * @author Sophie
 *
 */
public final class GraphUtils {

    private static final String VALUE = "value";

    private GraphUtils() {
        throw new IllegalAccessError();
    }

    /**
     * Checks if the previous node is a determiner.
     *
     * @param n
     *            node
     * @param relArcType
     *            the arctype of the edge to follow
     * @return true, if previous node token is a determiner
     */
    public static boolean checkIfPrevNodeIsDt(INode n, IArcType relArcType) {
        INode prevNode = getPreviousNode(n, relArcType);
        return (prevNode != null && prevNode.getAttributeValue("pos")
                                            .toString()
                                            .equals("DT"));
    }

    /**
     * Checks if the previous node is an indirect determiner.
     *
     * @param n
     *            node
     * @param relArcType
     *            the arctype of the edge to follow
     * @return true, if previous node is an indirect determiner
     */
    public static boolean checkForIndirectPrevDt(INode n, IArcType relArcType) {

        if (checkIfPrevNodeIsDt(n, relArcType)) {
            INode previousNode = getPreviousNode(n, relArcType);
            if (previousNode == null) {
                return false;
            }
            String prevVal = getNodeValue(previousNode);
            return ("a".equalsIgnoreCase(prevVal) || "an".equalsIgnoreCase(prevVal));
        }
        return false;
    }

    /**
     * Returns the previous node. The previous node is the source node of the incoming rel-Arc.
     *
     * @param n
     *            node
     * @param relArcType
     *            the arctype of the edge to follow
     * @return previous node
     */
    public static INode getPreviousNode(INode n, IArcType relArcType) {
        List<? extends IArc> inRelArcs = n.getIncomingArcsOfType(relArcType);
        Optional<? extends IArc> inNextArc = inRelArcs.stream()
                                                      .filter(a -> a.getAttributeValue(VALUE)
                                                                    .toString()
                                                                    .equals("NEXT"))
                                                      .findFirst();

        if (!inNextArc.isEmpty()) {
            return inNextArc.get()
                            .getSourceNode();
        }
        return null;
    }

    /**
     * Returns the word contained by a node.
     *
     * @param n
     *            node
     * @return word (value of node)
     */
    public static String getNodeValue(INode n) {
        if (n == null) {
            throw new IllegalArgumentException("Provided node cannot be null!");
        }
        ArrayList<Pair<String, Object>> attributeNameValuePairs = n.getAllAttributeNamesAndValuesAsPair();
        for (Pair<String, Object> pair : attributeNameValuePairs) {
            if (pair.getLeft()
                    .equals(VALUE)) {
                return pair.getRight()
                           .toString();
            }
        }
        return null;
    }

    /**
     * Returns the lemma to a given node.
     *
     * @param n
     *            node
     * @return "lemma" field of the given node
     */
    private static String getNodeLemma(INode n) {
        ArrayList<Pair<String, Object>> attributeNameValuePairs = n.getAllAttributeNamesAndValuesAsPair();
        for (Pair<String, Object> pair : attributeNameValuePairs) {
            if (pair.getLeft()
                    .equals("lemma")) {
                return pair.getRight()
                           .toString();
            }
        }
        return null;
    }

    /**
     * Returns a list of all node lemmas encapsulated by a mapping.
     *
     * @param mapping
     *            the given mapping
     * @return list of containing node lemmas
     */
    public static Map<INode, String> getMappingLemmas(NounMapping mapping) {
        Map<INode, String> lemmas = new HashMap<>();
        for (INode n : mapping.getNodes()) {
            lemmas.put(n, getNodeLemma(n));
        }
        return lemmas;
    }

    /**
     * Returns the position field of a given node.
     *
     * @param n
     *            given node
     * @return position as string
     */
    private static String getNodePosition(INode n) {
        ArrayList<Pair<String, Object>> attributeNameValuePairs = n.getAllAttributeNamesAndValuesAsPair();
        for (Pair<String, Object> pair : attributeNameValuePairs) {
            if (pair.getLeft()
                    .equals("position")) {
                return pair.getRight()
                           .toString();
            }
        }
        return null;
    }

    /**
     * Returns a list of all occurrences of the mapping nodes
     *
     * @param mapping
     *            given mapping
     * @return Map of nodes and positions
     */
    public static Map<INode, String> getMappingPosition(NounMapping mapping) {
        Map<INode, String> positions = new HashMap<>();
        for (INode n : mapping.getNodes()) {
            positions.put(n, getNodePosition(n));
        }
        return positions;
    }

    /**
     * Returns the next PARSE node following the sentence structure.
     *
     * @param n
     *            the current node
     * @param relArcType
     *            has to be the relation arc type of PARSE
     * @return the next node.
     */
    public static INode getNextNode(INode n, IArcType relArcType) {
        List<? extends IArc> outRelArcs = n.getOutgoingArcsOfType(relArcType);
        Optional<? extends IArc> outNextArc = outRelArcs.stream()
                                                        .filter(a -> a.getAttributeValue(VALUE)
                                                                      .toString()
                                                                      .equals("NEXT"))
                                                        .findFirst();

        if (!outNextArc.isEmpty()) {
            return outNextArc.get()
                             .getTargetNode();
        }
        return n;
    }

}
