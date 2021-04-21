package edu.kit.ipd.consistency_analyzer.datastructures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.kit.ipd.consistency_analyzer.common.SimilarityUtils;

public class NounMappingForEagle implements INounMappingWithDistribution {

    private List<IWord> words;
    private MappingKind mostProbableKind;
    private Double highestProbability;
    private Map<MappingKind, Double> distribution;
    private String reference;
    private List<String> occurrences;

    public NounMappingForEagle(List<IWord> words, Map<MappingKind, Double> distribution, String reference, List<String> occurrences) {
        this.words = new ArrayList<>(words);
        initializeDistribution(distribution);
        this.reference = reference;
        this.occurrences = new ArrayList<>(occurrences);
        mostProbableKind = distribution.keySet().stream().max((p1, p2) -> distribution.get(p1).compareTo(distribution.get(p2))).orElseGet(null);
        highestProbability = mostProbableKind != null ? distribution.get(mostProbableKind) : 0.0;

    }

    private void initializeDistribution(Map<MappingKind, Double> distribution) {

        this.distribution = new HashMap<>(distribution);

        if (!distribution.containsKey(MappingKind.NAME)) {
            this.distribution.put(MappingKind.NAME, 0.0);
        }
        if (!distribution.containsKey(MappingKind.TYPE)) {
            this.distribution.put(MappingKind.TYPE, 0.0);
        }
        if (!distribution.containsKey(MappingKind.NAME_OR_TYPE)) {
            this.distribution.put(MappingKind.NAME_OR_TYPE, 0.0);
        }

    }

    public NounMappingForEagle(List<IWord> words, MappingKind kind, double probability, String reference, List<String> occurrences) {
        Map<MappingKind, Double> distribution = new HashMap<>();
        distribution.put(kind, probability);

        this.words = new ArrayList<>(words);
        initializeDistribution(distribution);
        this.reference = reference;
        this.occurrences = new ArrayList<>(occurrences);
        mostProbableKind = distribution.keySet().stream().max((p1, p2) -> distribution.get(p1).compareTo(distribution.get(p2))).orElseGet(null);
        highestProbability = mostProbableKind != null ? distribution.get(mostProbableKind) : 0.0;
    }

    public void addKindWithProbability(MappingKind kind, double probability) {
        recalculateProbability(kind, probability);
    }

    private void updateBestValues() {
        mostProbableKind = distribution.keySet().stream().max((p1, p2) -> distribution.get(p1).compareTo(distribution.get(p2))).orElseGet(null);
        if (mostProbableKind != null) {
            highestProbability = distribution.get(mostProbableKind);
        }
    }

    @Override
    public NounMappingForEagle createCopy() {
        return new NounMappingForEagle(words, distribution, reference, occurrences);
    }

    public void hardsetProbabilities(double name, double type, double nort) {
        distribution.put(MappingKind.NAME, name);
        distribution.put(MappingKind.TYPE, type);
        distribution.put(MappingKind.NAME_OR_TYPE, nort);
    }

    @Override
    public Map<MappingKind, Double> getDistribution() {
        return distribution;
    }

    /**
     * Splits all occurrences with a whitespace in it at their spaces and returns all parts that are similar to the
     * reference. If it contains a separator or similar to the reference it is added to the comparables as a whole.
     *
     * @return all parts of occurrences (splitted at their spaces) that are similar to the reference.
     */
    @Override
    public List<String> getRepresentativeComparables() {
        List<String> comparables = new ArrayList<>();
        for (String occ : occurrences) {
            if (SimilarityUtils.containsSeparator(occ)) {
                List<String> parts = SimilarityUtils.splitAtSeparators(occ);
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
     * @param newProbability probability to set on
     */
    @Override
    public void hardSetProbability(double newProbability) {

        recalculateProbability(mostProbableKind, newProbability);
    }

    /**
     * Returns the occurrences of this mapping.
     *
     * @return all appearances of the mapping
     */
    @Override
    public List<String> getOccurrences() {
        return occurrences;
    }

    /**
     * Returns all nodes contained by the mapping
     *
     * @return all mapping nodes
     */
    @Override
    public List<IWord> getNodes() {
        return words;
    }

    /**
     * Adds nodes to the mapping, if they are not already contained.
     *
     * @param nodes graph nodes to add to the mapping
     */
    @Override
    public void addNodes(List<IWord> nodes) {
        for (IWord n : nodes) {
            addNode(n);
        }
    }

    /**
     * Adds a node to the mapping, it its not already contained.
     *
     * @param n graph node to add.
     */
    @Override
    public void addNode(IWord n) {
        if (!words.contains(n)) {
            words.add(n);
        }
    }

    /**
     * Returns the probability of being a mapping of its kind.
     *
     * @return probability of being a mapping of its kind.
     */
    @Override
    public double getProbability() {
        return highestProbability;
    }

    /**
     * Returns the kind: name, type, name_or_type.
     *
     * @return the kind
     */
    @Override
    public MappingKind getKind() {
        return mostProbableKind;
    }

    /**
     * Returns the reference, the comparable and naming attribute of this mapping.
     *
     * @return the reference
     */
    @Override
    public String getReference() {
        return reference;
    }

    /**
     *
     * @param kind        the new kind
     * @param probability the probability of the new mappingType
     */
    @Override
    public void changeMappingType(MappingKind kind, double probability) {
        recalculateProbability(kind, highestProbability * probability);
    }

    private void recalculateProbability(MappingKind kind, double newProbability) {

        double currentProbability = distribution.get(kind);
        distribution.put(kind, currentProbability + newProbability);

        mostProbableKind = distribution.keySet().stream().max((p1, p2) -> distribution.get(p1).compareTo(distribution.get(p2))).orElseGet(null);
        if (mostProbableKind != null) {
            if (mostProbableKind == MappingKind.NAME_OR_TYPE && (distribution.get(MappingKind.NAME) > 0 || distribution.get(MappingKind.TYPE) > 0)) {

                if (distribution.get(MappingKind.NAME) >= distribution.get(MappingKind.TYPE)) {
                    mostProbableKind = MappingKind.NAME;
                } else {
                    mostProbableKind = MappingKind.TYPE;
                }
            }
            highestProbability = distribution.get(mostProbableKind);
        }
    }

    /*
     * private void recalculateProbability(MappingKind kind, double newProbability) {
     *
     * double name = distribution.get(MappingKind.NAME); double type = distribution.get(MappingKind.TYPE); double nort =
     * distribution.get(MappingKind.NAME_OR_TYPE);
     *
     * double loss = 0.8; double currentProbability = distribution.get(kind);
     *
     *
     * if (kind.equals(MappingKind.NAME_OR_TYPE) && (name > 0 || type > 0)) { double probabilityToSetName = loss * ;
     *
     *
     *
     *
     * } else { double probabilityToSet = loss * (newProbability + currentProbability);
     *
     * if (name + type + nort + probabilityToSet - distribution.get(kind) <= 1) { distribution.put(kind,
     * probabilityToSet); } else { double scale = 1 / (name + type + nort - distribution.get(kind) + probabilityToSet);
     * rescaleProbabilities(scale); } } updateBestValues(); }
     */

    private void rescaleProbabilities(double scale) {
        double name = distribution.get(MappingKind.NAME);
        double type = distribution.get(MappingKind.TYPE);
        double nort = distribution.get(MappingKind.NAME_OR_TYPE);
        distribution.put(MappingKind.NAME, name * scale);
        distribution.put(MappingKind.TYPE, type * scale);
        distribution.put(MappingKind.NAME_OR_TYPE, nort * scale);
    }

    /**
     * Returns the sentence numbers of occurrences, sorted.
     *
     * @return sentence numbers of the occurrences of this mapping.
     */
    @Override
    public List<Integer> getMappingSentenceNo() {
        List<Integer> positions = new ArrayList<>();
        for (IWord n : words) {
            positions.add(n.getSentenceNo() + 1);
        }
        Collections.sort(positions);
        return positions;
    }

    /**
     * Updates the reference if the probability is high enough.
     *
     * @param ref         new reference
     * @param probability probability for the new reference.
     */
    @Override
    public void updateReference(String ref, double probability) {
        if (probability > highestProbability * 4) {
            reference = ref;
        }
    }

    @Override
    public String toString() {
        return "NounMapping [" + "distribution="
                + distribution.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).collect(Collectors.joining(",")) + //
                ", reference=" + reference + //
                ", node=" + String.join(", ", occurrences) + //
                ", position=" + String.join(", ", words.stream().map(word -> String.valueOf(word.getPosition())).collect(Collectors.toList())) + //
                ", probability=" + highestProbability + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((reference == null) ? 0 : reference.hashCode());
        return result;
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
        NounMappingForEagle other = (NounMappingForEagle) obj;
        if (reference == null) {
            if (other.reference != null) {
                return false;
            }
        } else if (!reference.equals(other.reference)) {
            return false;
        }
        return true;
    }

    /**
     * Adds occurrences to the mapping
     *
     * @param occurrences2 occurrences to add
     */
    @Override
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
     * @param occurrence     the occurrence to copy
     * @param createdMapping the other mapping
     */
    @Override
    public void copyOccurrencesAndNodesTo(String occurrence, INounMapping createdMapping) {
        List<IWord> occNodes = words.stream().filter(n -> n.getText().equals(occurrence)).collect(Collectors.toList());
        createdMapping.addNodes(occNodes);
        createdMapping.addOccurrence(List.of(occurrence));

    }

    /**
     * Returns a list of all node lemmas encapsulated by a mapping.
     *
     * @return list of containing node lemmas
     */
    public List<String> getMappingLemmas() {
        return words.stream().map(IWord::getLemma).collect(Collectors.toList());
    }

    /**
     * Updates the probability
     *
     * @param newProbability the probability to update with.
     */
    @Override
    public void updateProbability(double newProbability) {

        if (highestProbability == 1.0) {
            // do nothing
        } else if (newProbability == 1.0) {
            highestProbability = newProbability;
            distribution.put(mostProbableKind, newProbability);
        } else if (highestProbability >= newProbability) {
            double porbabilityToSet = highestProbability + newProbability * (1 - highestProbability);
            recalculateProbability(mostProbableKind, porbabilityToSet);
        } else {
            double porbabilityToSet = (highestProbability + newProbability) * 0.5;
            recalculateProbability(mostProbableKind, porbabilityToSet);
        }
    }

    @Override
    public double getProbabilityForName() {
        return distribution.get(MappingKind.NAME);
    }

    @Override
    public double getProbabilityForType() {
        return distribution.get(MappingKind.TYPE);
    }

    @Override
    public double getProbabilityForNort() {
        return distribution.get(MappingKind.NAME_OR_TYPE);
    }

}
