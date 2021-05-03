package edu.kit.kastel.mcse.ardoco.core.datastructures;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.common.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMappingWithDistribution;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.MappingKind;

public class NounMappingWithDistribution extends AbstractNounMapping implements INounMappingWithDistribution {

    private MappingKind mostProbableKind;
    private Double highestProbability;
    private Map<MappingKind, Double> distribution;

    public NounMappingWithDistribution(List<IWord> words, Map<MappingKind, Double> distribution, String reference, List<String> occurrences) {
        this.words = new ArrayList<>(words);
        initializeDistribution(distribution);
        this.reference = reference;
        this.occurrences = new ArrayList<>(occurrences);
        mostProbableKind = distribution.keySet().stream().max((p1, p2) -> distribution.get(p1).compareTo(distribution.get(p2))).orElse(null);
        highestProbability = mostProbableKind != null ? distribution.get(mostProbableKind) : 0.0;
    }

    private void initializeDistribution(Map<MappingKind, Double> distribution) {

        this.distribution = new EnumMap<>(distribution);

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

    public NounMappingWithDistribution(List<IWord> words, MappingKind kind, double probability, String reference, List<String> occurrences) {
        Map<MappingKind, Double> distribution = new HashMap<>();
        distribution.put(kind, probability);

        this.words = new ArrayList<>(words);
        initializeDistribution(distribution);
        this.reference = reference;
        this.occurrences = new ArrayList<>(occurrences);
        mostProbableKind = distribution.keySet().stream().max((p1, p2) -> distribution.get(p1).compareTo(distribution.get(p2))).orElse(null);
        highestProbability = mostProbableKind != null ? distribution.get(mostProbableKind) : 0.0;
    }

    public void addKindWithProbability(MappingKind kind, double probability) {
        recalculateProbability(kind, probability);
    }

    @Override
    public NounMappingWithDistribution createCopy() {
        return new NounMappingWithDistribution(words, distribution, reference, occurrences);
    }

    @Override
    public Map<MappingKind, Double> getDistribution() {
        return new EnumMap<>(distribution);
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
            } else if (SimilarityUtils.areWordsSimilar(reference, occ)) {
                comparables.add(occ);
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

        mostProbableKind = distribution.keySet().stream().max((p1, p2) -> distribution.get(p1).compareTo(distribution.get(p2))).orElse(null);
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
        return Objects.hash(reference);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        NounMappingWithDistribution other = (NounMappingWithDistribution) obj;
        if (!Objects.equals(reference, other.reference)) {
            return false;
        }
        return true;
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
