package modelconnector.textExtractor.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import edu.kit.ipd.parse.luna.graph.INode;
import modelconnector.helpers.GraphUtils;
import modelconnector.helpers.SimilarityUtils;

/**
 * This class represents a noun, which is supposed to be a name or type of an instance. It is just textual related. The
 * identifier of such a mapping is the reference, containing the word all occurrences are familiar with. The nodes are
 * referencing all occurrences of the reference. Thus, the reference contains the name or type of the mapping. The
 * probability is the probability, that this mapping is of its MappingType. The MappingType declares what the mapping is
 * supposed to be: A name, type or both (if its not decided yet).
 *
 * @author Sophie
 *
 */
public final class NounMapping {

    private List<INode> nodes;
    private double probability;
    private MappingKind kind;
    private String reference;
    private List<String> occurrences;

    /**
     * The kind of word defines if the mapping is one word of two graph nodes or if it contains words represented by a
     * single node.
     *
     * @author Sophie
     *
     */

    /**
     * Creates a new mapping.
     *
     * @param nodes
     *            nodes representing the mapping.
     * @param probability
     *            of being of that mappingType.
     * @param kind
     *            the kind of the mapping
     * @param reference
     *            the reference, the unique name, of the mapping. This reference is used for comparison and should be
     *            mostly general.
     * @param occurrences
     *            the occurrences, e.g. values of the nodes, that appears at the nodes listed in this mapping.
     */
    private NounMapping(List<INode> nodes, double probability, MappingKind kind, String reference,
            List<String> occurrences) {

        this.nodes = new ArrayList<>(nodes);
        this.probability = probability;
        this.kind = kind;
        this.reference = reference;
        this.occurrences = new ArrayList<>(occurrences);
    }

    /**
     * Splits all occurrences with a whitespace in it at their spaces and returns all parts that are similar to the
     * reference. If it contains a separator or similar to the reference it is added to the comparables as a whole.
     *
     * @return all parts of occurrences (splitted at their spaces) that are similar to the reference.
     */
    public List<String> getRepresentativeComparables() {
        List<String> comparables = new ArrayList<>();
        for (String occ : occurrences) {
            if (SimilarityUtils.containsSeparator(occ)) {
                List<String> parts = List.of(SimilarityUtils.splitAtSeparators(occ)
                                                            .split(" "));
                for (String part : parts) {
                    if (SimilarityUtils.areWordsSimilar(reference, part)) {
                        comparables.add(part);
                    }
                }
                comparables.add(occ);
            } else {
                if (SimilarityUtils.areWordsSimilar(reference, occ)) {
                    comparables.add(occ);
                }
            }
        }
        return comparables;
    }

    /**
     * Sets the probability of the mapping
     *
     * @param probability
     *            probability to set on
     */
    public void hardSetProbability(double probability) {
        this.probability = probability;
    }

    /**
     * Returns the occurrences of this mapping.
     *
     * @return all appearances of the mapping
     */
    public List<String> getOccurrences() {
        return occurrences;
    }

    /**
     * Creates a mapping dependent on the kind and a single node
     *
     * @param n
     *            node for the mapping
     * @param probability
     *            probability of being a mapping of the kind
     * @param kind
     *            the kind
     * @param reference
     *            the reference for this mapping
     * @param occurrences
     *            the appearances of the mapping
     * @return the created mapping
     */
    public static NounMapping createMappingTypeNode(INode n, String reference, MappingKind kind, double probability,
            List<String> occurrences) {
        NounMapping nnm;
        if (kind.equals(MappingKind.NAME)) {
            nnm = createNameNode(List.of(n), probability, reference, occurrences);
        } else if (kind.equals(MappingKind.TYPE)) {
            nnm = createTypeNode(List.of(n), probability, reference, occurrences);
        } else {
            nnm = createNortNode(List.of(n), probability, reference, occurrences);
        }
        return nnm;
    }

    /**
     * Creates a name mapping
     *
     * @param nodes
     *            nodes for the mapping
     * @param probability
     *            probability of being a name mapping
     * @param name
     *            the reference for this mapping
     * @param occurrences
     *            the appearances of the mapping
     * @return the created name mapping
     */
    public static NounMapping createNameNode(List<INode> nodes, double probability, String name,
            List<String> occurrences) {
        return new NounMapping(nodes, probability, MappingKind.NAME, name, occurrences);
    }

    /**
     * Creates a name mapping, based on a single node
     *
     * @param node
     *            node for the mapping
     * @param probability
     *            probability of being a name mapping
     * @param type
     *            the reference for this mapping
     * @param occurrences
     *            the appearances of the mapping
     * @return the created name mapping
     */
    public static NounMapping createNameMapping(INode node, double probability, String type, List<String> occurrences) {
        return new NounMapping(List.of(node), probability, MappingKind.NAME, type, occurrences);
    }

    /**
     * Creates a type mapping
     *
     * @param node
     *            node for the mapping
     * @param probability
     *            probability of being a type mapping
     * @param type
     *            the reference for this mapping
     * @param occurrences
     *            the appearances of the mapping
     * @return the created type mapping
     */
    public static NounMapping createTypeMapping(INode node, double probability, String type, List<String> occurrences) {
        return new NounMapping(List.of(node), probability, MappingKind.TYPE, type, occurrences);
    }

    /**
     * Creates a type mapping, based on a single node
     *
     * @param nodes
     *            nodes for the mapping
     * @param probability
     *            probability of being a type mapping
     * @param type
     *            the reference for this mapping
     * @param occurrences
     *            the appearances of the mapping
     * @return the created type mapping
     */
    public static NounMapping createTypeNode(List<INode> nodes, double probability, String type,
            List<String> occurrences) {
        return new NounMapping(nodes, probability, MappingKind.TYPE, type, occurrences);
    }

    /**
     * Creates a name or type mapping, based on a single node
     *
     * @param node
     *            node for the mapping
     * @param probability
     *            probability of being a name or type mapping
     * @param type
     *            the reference for this mapping
     * @param occurrences
     *            the appearances of the mapping
     * @return the created name or type mapping
     */
    public static NounMapping createNortMapping(INode node, double probability, String type, List<String> occurrences) {
        return new NounMapping(List.of(node), probability, MappingKind.NAME_OR_TYPE, type, occurrences);
    }

    /**
     * Creates a name or type mapping
     *
     * @param nodes
     *            nodes for the mapping
     * @param probability
     *            probability of being a name or type mapping
     * @param ref
     *            the reference for this mapping
     * @param occurrences
     *            the appearances of the mapping
     * @return the created name or type mapping
     */
    public static NounMapping createNortNode(List<INode> nodes, double probability, String ref,
            List<String> occurrences) {
        return new NounMapping(nodes, probability, MappingKind.NAME_OR_TYPE, ref, occurrences);
    }

    /**
     * Returns all nodes contained by the mapping
     *
     * @return all mapping nodes
     */
    public List<INode> getNodes() {
        return nodes;
    }

    /**
     * Adds nodes to the mapping, if they are not already contained.
     *
     * @param nodes
     *            graph nodes to add to the mapping
     */
    public void addNodes(List<INode> nodes) {
        for (INode n : nodes) {
            addNode(n);
        }
    }

    /**
     * Adds a node to the mapping, it its not already contained.
     *
     * @param n
     *            graph node to add.
     */
    public void addNode(INode n) {
        if (!nodes.contains(n)) {
            nodes.add(n);
        }
    }

    /**
     * Returns the probability of being a mapping of its kind.
     *
     * @return probability of being a mapping of its kind.
     */
    public double getProbability() {
        return probability;
    }

    /**
     * Returns the kind: name, type, name_or_type.
     *
     * @return the kind
     */
    public MappingKind getKind() {
        return kind;
    }

    /**
     * Returns the reference, the comparable and naming attribute of this mapping.
     *
     * @return the reference
     */
    public String getReference() {
        return reference;
    }

    /**
     * Changes the kind to another one and recalculates the probability with
     * {@link #recalculateProbability(double, double)}.
     *
     * @param kind
     *            the new kind
     * @param probability
     *            the probability of the new mappingTzpe
     */
    public void changeMappingTypeTo(MappingKind kind, double probability) {
        this.kind = kind;
        this.probability = recalculateProbability(this.probability, probability);
    }

    /**
     * Recalculates the probability.
     *
     * @param beforeProbability
     *            the probability of the mapping before the change
     * @param afterProbability
     *            the probability of the mapping after the change
     * @return the resulting probability.
     */
    private double recalculateProbability(double beforeProbability, double afterProbability) {
        return beforeProbability * afterProbability;
    }

    /**
     * Returns the sentence numbers of occurrences, sorted.
     *
     * @return sentence numbers of the occurrences of this mapping.
     */
    public List<Integer> getMappingSentenceNo() {
        List<Integer> positions = new ArrayList<>();
        for (INode n : nodes) {
            positions.add(Integer.valueOf(n.getAttributeValue("sentenceNumber")
                                           .toString())
                    + 1);
        }
        Collections.sort(positions);
        return positions;
    }

    /**
     * Updates the reference if the probability is high enough.
     *
     * @param ref
     *            new reference
     * @param probability
     *            probability for the new reference.
     */
    public void updateReference(String ref, double probability) {
        if (probability > this.probability * 4) {
            reference = ref;
        }
    }

    @Override
    public String toString() {
        return "NounMapping [" + "mappingTypea=" + kind + //
                ", reference=" + reference + //
                ", node=" + String.join(", ", occurrences) + //
                ", position=" + String.join(", ", GraphUtils.getMappingPosition(this)
                                                            .values())
                + //
                ", probability=" + probability + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, nodes, occurrences);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NounMapping other = (NounMapping) obj;

        if (!SimilarityUtils.areWordsSimilar(reference, other.reference) && !reference.contentEquals("")
                && !other.reference.contentEquals("")) {
            return false;
        }
        return kind == other.kind && Objects.equals(nodes, other.nodes) && //
                Objects.equals(occurrences, other.occurrences)
                && SimilarityUtils.areWordsSimilar(reference, other.reference);
    }

    /**
     * Adds occurrences to the mapping
     *
     * @param occurrences2
     *            occurrences to add
     */
    public void addOccurrence(List<String> occurrences2) {
        for (String occ2 : occurrences2) {
            if (!occurrences.contains(occ2)) {
                occurrences.add(occ2);
            }
        }
    }

    /**
     * Copies all nodes and occurrences matching the occurrence to another mapping
     *
     * @param occurrence
     *            the occurrence to copy
     * @param createdMapping
     *            the other mapping
     */
    public void copyOccurrencesAndNodesTo(String occurrence, NounMapping createdMapping) {
        List<INode> occNodes = nodes.stream()
                                    .filter(n -> GraphUtils.getNodeValue(n)
                                                           .equals(occurrence))
                                    .collect(Collectors.toList());
        createdMapping.addNodes(occNodes);
        createdMapping.addOccurrence(List.of(occurrence));

    }

    /**
     * Updates the probability
     *
     * @param newProbability
     *            the probability to update with.
     */
    public void updateProbability(double newProbability) {
        if (probability == 1.0) {
            // do nothing
        } else if (newProbability == 1.0) {
            probability = 1.0;
        } else if (probability >= newProbability) {
            probability += newProbability * (1 - probability);
        } else {
            probability += newProbability;
            probability = probability * 0.5;
        }
    }

}
