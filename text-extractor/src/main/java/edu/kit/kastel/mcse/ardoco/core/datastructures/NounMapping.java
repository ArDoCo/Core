package edu.kit.kastel.mcse.ardoco.core.datastructures;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.util.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.util.CommonUtilities;

/**
 * The Class NounMapping is a basic realization of {@link INounMapping}.
 */
public class NounMapping implements INounMapping {

    /* Main reference */
    private String reference;

    /* Words are the references within the text */
    private MutableList<IWord> words;

    private MutableList<IWord> coreferences = Lists.mutable.empty();

    /* the different surface forms */
    private MutableList<String> surfaceForms;

    private MappingKind mostProbableKind;
    private Double highestProbability;
    private Map<MappingKind, Double> distribution;

    /**
     * Instantiates a new noun mapping.
     *
     * @param words        the words
     * @param distribution the distribution
     * @param reference    the reference
     * @param surfaceForm  the occurrences
     */
    public NounMapping(ImmutableList<IWord> words, Map<MappingKind, Double> distribution, String reference, ImmutableList<String> surfaceForms) {
        this.words = Lists.mutable.withAll(words);
        initializeDistribution(distribution);
        this.reference = reference;
        this.surfaceForms = Lists.mutable.withAll(surfaceForms);
        mostProbableKind = distribution.keySet().stream().max((p1, p2) -> distribution.get(p1).compareTo(distribution.get(p2))).orElse(null);
        highestProbability = mostProbableKind != null ? distribution.get(mostProbableKind) : 0.0;
    }

    private NounMapping(INounMapping nm) {
        words = Lists.mutable.withAll(nm.getWords());
        initializeDistribution(nm.getDistribution());
        reference = nm.getReference();
        surfaceForms = Lists.mutable.withAll(nm.getSurfaceForms());
        mostProbableKind = distribution.keySet().stream().max((p1, p2) -> distribution.get(p1).compareTo(distribution.get(p2))).orElse(null);
        highestProbability = mostProbableKind != null ? distribution.get(mostProbableKind) : 0.0;
    }

    private void initializeDistribution(Map<MappingKind, Double> distribution) {

        this.distribution = new EnumMap<>(distribution);

        this.distribution.putIfAbsent(MappingKind.NAME, 0.0);
        this.distribution.putIfAbsent(MappingKind.TYPE, 0.0);
        this.distribution.putIfAbsent(MappingKind.NAME_OR_TYPE, 0.0);
    }

    /**
     * Instantiates a new noun mapping.
     *
     * @param words       the words
     * @param kind        the kind
     * @param probability the probability
     * @param reference   the reference
     * @param occurrences the occurrences
     */
    public NounMapping(ImmutableList<IWord> words, MappingKind kind, double probability, String reference, ImmutableList<String> occurrences) {
        distribution = new EnumMap<>(MappingKind.class);
        distribution.put(kind, probability);

        this.words = Lists.mutable.withAll(words);
        initializeDistribution(distribution);
        this.reference = reference;
        surfaceForms = Lists.mutable.withAll(occurrences);
        mostProbableKind = distribution.keySet().stream().max((p1, p2) -> distribution.get(p1).compareTo(distribution.get(p2))).orElse(null);
        highestProbability = mostProbableKind != null ? distribution.get(mostProbableKind) : 0.0;
    }

    /**
     * Returns the surface forms (previously called occurrences) of this mapping.
     *
     * @return all appearances of the mapping
     */
    @Override
    public final ImmutableList<String> getSurfaceForms() {
        return Lists.immutable.withAll(surfaceForms);
    }

    /**
     * Returns all words that are contained by the mapping. This should include coreferences.
     *
     * @return all words that are referenced with this mapping
     */
    @Override
    public final ImmutableList<IWord> getWords() {
        return Lists.immutable.withAll(words);
    }

    /**
     * Adds nodes to the mapping, if they are not already contained.
     *
     * @param words graph nodes to add to the mapping
     */
    @Override
    public final void addWords(ImmutableList<IWord> words) {
        for (var word : words) {
            addWord(word);
        }
    }

    /**
     * Adds a node to the mapping, it its not already contained.
     *
     * @param word graph node to add.
     */
    @Override
    public final void addWord(IWord word) {
        if (!words.contains(word)) {
            words.add(word);
        }
    }

    /**
     * Returns the reference, the comparable and naming attribute of this mapping.
     *
     * @return the reference
     */
    @Override
    public final String getReference() {
        return reference;
    }

    /**
     * Returns the sentence numbers of occurrences, sorted.
     *
     * @return sentence numbers of the occurrences of this mapping.
     */
    @Override
    public final ImmutableList<Integer> getMappingSentenceNo() {
        MutableList<Integer> positions = Lists.mutable.empty();
        for (IWord word : words) {
            positions.add(word.getSentenceNo() + 1);
        }
        return positions.toSortedList().toImmutable();
    }

    /**
     * Adds occurrences to the mapping
     *
     * @param newOccurances occurrences to add
     */
    @Override
    public final void addOccurrence(ImmutableList<String> newOccurances) {
        for (String o : newOccurances) {
            if (!surfaceForms.contains(o)) {
                surfaceForms.add(o);
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
    public final void copyOccurrencesAndNodesTo(String occurrence, INounMapping createdMapping) {
        ImmutableList<IWord> occNodes = words.select(n -> n.getText().equals(occurrence)).toImmutable();
        createdMapping.addWords(occNodes);
        createdMapping.addOccurrence(Lists.immutable.with(occurrence));
    }

    /**
     * Returns a list of all node lemmas encapsulated by a mapping.
     *
     * @return list of containing node lemmas
     */
    public final ImmutableList<String> getMappingLemmas() {
        return words.collect(IWord::getLemma).toImmutable();
    }

    /**
     * Adds the kind with probability.
     *
     * @param kind        the kind
     * @param probability the probability
     */
    @Override
    public void addKindWithProbability(MappingKind kind, double probability) {
        recalculateProbability(kind, probability);
    }

    @Override
    public INounMapping createCopy() {
        return new NounMapping(words.toImmutable(), distribution, reference, surfaceForms.toImmutable());
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
    public ImmutableList<String> getRepresentativeComparables() {
        MutableList<String> comparables = Lists.mutable.empty();
        for (String occ : surfaceForms) {
            if (CommonUtilities.containsSeparator(occ)) {
                ImmutableList<String> parts = CommonUtilities.splitAtSeparators(occ);
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
        return comparables.toImmutable();
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

    /**
     * @return the coreferences
     */
    @Override
    public ImmutableList<IWord> getCoreferences() {
        return coreferences.toImmutable();
    }

    /**
     * @param coreferences the coreferences to add
     */
    @Override
    public void addCoreferences(Collection<IWord> coreferences) {
        this.coreferences.addAll(coreferences);
    }

    /**
     * @param coreference the coreference to add
     */
    @Override
    public void addCoreference(IWord coreference) {
        coreferences.add(coreference);
    }

    @Override
    public String toString() {
        return "NounMapping [" + "distribution="
                + distribution.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).collect(Collectors.joining(",")) + //
                ", reference=" + reference + //
                ", node=" + String.join(", ", surfaceForms) + //
                ", position=" + String.join(", ", words.collect(word -> String.valueOf(word.getPosition()))) + //
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
        NounMapping other = (NounMapping) obj;
        return Objects.equals(reference, other.reference);
    }

    /**
     * Updates the probability
     *
     * @param newProbability the probability to update with.
     */
    @Override
    public void updateProbability(double newProbability) {
        if (CommonUtilities.valueEqual(highestProbability, 1.0)) {
            return;
        }

        if (CommonUtilities.valueEqual(newProbability, 1.0)) {
            highestProbability = newProbability;
            distribution.put(mostProbableKind, newProbability);
        } else if (highestProbability >= newProbability) {
            double probabilityToSet = highestProbability + newProbability * (1 - highestProbability);
            recalculateProbability(mostProbableKind, probabilityToSet);
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

    @Override
    public INounMapping merge(INounMapping other) {
        if (other == null) {
            return new NounMapping(this);
        }
        var newWords = Lists.mutable.ofAll(words);
        newWords.addAll(other.getWords().castToCollection());
        Map<MappingKind, Double> newDistribution = new EnumMap<>(distribution);
        for (var entry : other.getDistribution().entrySet()) {
            if (newDistribution.containsKey(entry.getKey())) {
                var thisVal = newDistribution.get(entry.getKey());
                var maxValue = Double.max(thisVal, entry.getValue());
                newDistribution.put(entry.getKey(), maxValue);
            } else {
                newDistribution.put(entry.getKey(), entry.getValue());
            }
        }
        var newSurfaceForms = Lists.mutable.ofAll(surfaceForms);
        newSurfaceForms.addAll(other.getSurfaceForms().castToCollection());

        INounMapping newNounMapping = new NounMapping(newWords.toImmutable(), newDistribution, reference, newSurfaceForms.toImmutable());
        newNounMapping.addCoreferences(coreferences);
        newNounMapping.addCoreferences(other.getCoreferences().castToCollection());

        return newNounMapping;
    }

}
