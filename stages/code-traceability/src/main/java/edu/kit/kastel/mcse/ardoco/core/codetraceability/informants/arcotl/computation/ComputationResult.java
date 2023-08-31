/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.computation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.EndpointTuple;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SamCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.computation.computationtree.Node;

/**
 * The result of a computation. A computation's final result maps every
 * combination of computation node and endpoint tuple to exactly one
 * {@link Confidence confidence}.
 */
@Deterministic
public class ComputationResult {

    private final Map<Node, NodeResult> resultMap;

    /**
     * Creates a new computation result. It is initially empty, so the confidences
     * still need to be added after they have been calculated.
     */
    public ComputationResult() {
        resultMap = new LinkedHashMap<>();
    }

    /**
     * Returns the calculated {@link Confidence confidence} of the specified
     * combination of computation node and endpoint tuple. Returns null if no
     * confidence for the specified combination has been calculated and added to
     * this result yet.
     *
     * @param node          the computation node for which a confidence is to be
     *                      returned
     * @param endpointTuple the endpoint tuple for which a confidence is to be
     *                      returned
     * @return the confidence of the combination of computation node and endpoint
     *         tuple, or null if it doesn't exist yet
     */
    public Confidence getConfidence(Node node, EndpointTuple endpointTuple) {
        if (!exists(node)) {
            return null;
        }
        return resultMap.get(node).getConfidence(endpointTuple);
    }

    /**
     * Returns the {@link NodeResult result} of the specified computation node or
     * null if it doesn't exist yet. A computation node's final result are the
     * calculated confidences of every endpoint tuple.
     *
     * @param node the computation node for which the result is to be returned
     * @return the result of the computation node, or null if it doesn't exist yet
     */
    public NodeResult getNodeResult(Node node) {
        return resultMap.get(node);
    }

    /**
     * Returns trace links based on the specified computation node's result. Every
     * endpoint tuple for which a confidence has been added to the specified
     * computation node's result gets considered. Only returns a trace link for an
     * endpoint tuple if its confidence in the computation node's result has a
     * value.
     *
     * @param node the computation node for which the trace links are to be returned
     * @return trace links for every endpoint tuple whose confidence in the
     *         specified computation node's result has a value
     */
    public Set<SamCodeTraceLink> getTraceLinks(Node node) {
        if (!exists(node)) {
            return new java.util.LinkedHashSet<>();
        }
        NodeResult nodeResult = resultMap.get(node);
        return nodeResult.getTraceLinks();
    }

    /**
     * Adds a mapping of the specified combination of computation node and endpoint
     * tuple to the specified confidence. If the specified combination was
     * previously mapped to a confidence, the old confidence is replaced.
     *
     * @param node          the computation node for which a confidence is to be
     *                      added
     * @param endpointTuple the endpoint tuple for which a confidence is to be added
     * @param confidence    the confidence of the combination of computation node
     *                      and endpoint tuple
     */
    public void add(Node node, SamCodeEndpointTuple endpointTuple, Confidence confidence) {
        NodeResult nodeResult = resultMap.getOrDefault(node, new NodeResult());
        nodeResult.add(endpointTuple, confidence);
        resultMap.put(node, nodeResult);
    }

    /**
     * Adds the specified partial computation result to this computation result.
     * Specifically, all confidences of the partial computation result are added to
     * this computation result.
     *
     * @param partialResult the partial computation result to be added to this
     *                      computation result
     */
    public void addAll(ComputationResult partialResult) {
        resultMap.putAll(partialResult.resultMap);
    }

    public void addNodeResult(Node node, NodeResult nodeResult) {
        resultMap.put(node, nodeResult);
    }

    /**
     * Returns true if and only if the specified computation node already has a
     * result.
     *
     * @param node the computation node whose result's existence is checked
     * @return true if the specified computation node already has a result; false
     *         otherwise
     */
    public boolean exists(Node node) {
        return resultMap.containsKey(node);
    }

    public List<String> getConfidenceStrings(Node node, EndpointTuple endpointTuple) {
        Map<String, Integer> levelToConfidences = getConfidenceStringsToLevelMap(node, endpointTuple, 1);
        return levelToConfidences.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(Map.Entry::getKey).toList();
    }

    private Map<String, Integer> getConfidenceStringsToLevelMap(Node node, EndpointTuple endpointTuple, int level) {
        Map<String, Integer> confidenceToLevel = new LinkedHashMap<>();
        Confidence confidence = getNodeResult(node).getConfidence(endpointTuple);
        String confidenceString = "Level " + level + ": " + node.toString() + ", Confidence: " + confidence;
        confidenceToLevel.put(confidenceString, level);
        for (Node child : node.getChildren()) {
            confidenceToLevel.putAll(getConfidenceStringsToLevelMap(child, endpointTuple, level + 1));
        }
        return confidenceToLevel;
    }
}
