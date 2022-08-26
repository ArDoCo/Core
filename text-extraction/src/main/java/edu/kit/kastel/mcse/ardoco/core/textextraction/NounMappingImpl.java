/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import static edu.kit.kastel.informalin.framework.common.AggregationFunctions.AVERAGE;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;

import edu.kit.kastel.informalin.framework.common.AggregationFunctions;
import edu.kit.kastel.informalin.framework.common.JavaUtils;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;

/**
 * The Class NounMapping is a basic realization of {@link NounMapping}.
 */
public record NounMappingImpl(Long earliestCreationTime, ImmutableSortedSet<Word> words, MutableMap<MappingKind, Confidence> distribution,
                              ImmutableList<Word> referenceWords, ImmutableList<String> surfaceForms, String reference,
                              @Deprecated AtomicBoolean isDefinedAsTerm) implements NounMapping, Comparable<NounMappingImpl> {

    /**
     * Minimum difference that need to shall not be reached to identify a NounMapping as NameOrType.
     */
    private static final double MAPPINGKIND_MAX_DIFF = 0.1;

    private static final AggregationFunctions DEFAULT_AGGREGATOR = AVERAGE;

    /**
     * Instantiates a new noun mapping.
     */
    private NounMappingImpl(Long earliestCreationTime, ImmutableSet<Word> words, Map<MappingKind, Confidence> distribution, ImmutableList<Word> referenceWords,
            ImmutableList<String> surfaceForms) {

        this(earliestCreationTime, words.toSortedSet().toImmutable(), Maps.mutable.ofMap(distribution), referenceWords, surfaceForms, calculateReference(
                referenceWords), new AtomicBoolean(false));

        this.distribution.putIfAbsent(MappingKind.NAME, new Confidence(DEFAULT_AGGREGATOR));
        this.distribution.putIfAbsent(MappingKind.TYPE, new Confidence(DEFAULT_AGGREGATOR));
    }

    /**
     * Instantiates a new noun mapping.
     */
    public NounMappingImpl(Long earliestCreationTime, ImmutableSet<Word> words, MappingKind kind, Claimant claimant, double probability,
            ImmutableList<Word> referenceWords, ImmutableList<String> surfaceForms) {
        this(earliestCreationTime, words.toSortedSet().toImmutable(), Maps.mutable.empty(), referenceWords, surfaceForms, calculateReference(referenceWords),
                new AtomicBoolean(false));

        Objects.requireNonNull(claimant);
        this.distribution.putIfAbsent(MappingKind.NAME, new Confidence(DEFAULT_AGGREGATOR));
        this.distribution.putIfAbsent(MappingKind.TYPE, new Confidence(DEFAULT_AGGREGATOR));
        this.addKindWithProbability(kind, claimant, probability);
    }

    public NounMappingImpl(Long earliestCreationTime, ImmutableSet<Word> words, MappingKind kind, Claimant claimant, double probability,
            ImmutableList<Word> referenceWords, ImmutableList<String> surfaceForms, String reference) {
        this(earliestCreationTime, words.toSortedSet().toImmutable(), Maps.mutable.empty(), referenceWords, surfaceForms, reference, new AtomicBoolean(false));

        Objects.requireNonNull(claimant);
        this.distribution.putIfAbsent(MappingKind.NAME, new Confidence(DEFAULT_AGGREGATOR));
        this.distribution.putIfAbsent(MappingKind.TYPE, new Confidence(DEFAULT_AGGREGATOR));
        this.addKindWithProbability(kind, claimant, probability);

    }

    @Override
    public final ImmutableSortedSet<Word> getWords() {
        return words;
    }

    @Override
    public String getReference() {
        return this.reference;
    }

    private static String calculateReference(ImmutableList<Word> words) {
        words.toSortedListBy(Word::getPosition);
        words.toSortedListBy(Word::getSentenceNo);

        String reference = "";

        for (int i = 0; i < words.size() - 1; i++) {
            reference += words.get(i).getText() + " ";
        }
        reference += words.get(words.size() - 1).getText();
        return reference;
    }

    @Override
    public final ImmutableList<Word> getReferenceWords() {
        return referenceWords;
    }

    @Override
    public final ImmutableList<Integer> getMappingSentenceNo() {
        MutableList<Integer> positions = Lists.mutable.empty();
        for (Word word : words) {
            positions.add(word.getSentenceNo() + 1);
        }
        return positions.toSortedList().toImmutable();
    }

    @Override
    public ImmutableSet<Phrase> getPhrases() {
        MutableSet<Phrase> phrases = Sets.mutable.empty();
        for (Word word : this.words) {
            phrases.add(word.getPhrase());
        }
        return phrases.toImmutable();
    }

    @Override
    public void addKindWithProbability(MappingKind kind, Claimant claimant, double probability) {
        var currentProbability = distribution.get(kind);
        Objects.requireNonNull(claimant);
        currentProbability.addAgentConfidence(claimant, probability);
    }

    @Override
    public NounMapping createCopy() {

        return new NounMappingImpl(earliestCreationTime, words.toImmutableSet(), JavaUtils.copyMap(this.distribution, Confidence::createCopy), Lists.immutable
                .withAll(referenceWords), surfaceForms.toImmutable());
    }

    @Override
    public ImmutableMap<MappingKind, Confidence> getDistribution() {
        return distribution.toImmutable();
    }

    @Override
    public AggregationFunctions getGlobalAggregationFunction() {
        return this.getAggregationFunction();
    }

    @Override
    public AggregationFunctions getLocalAggregationFunction() {
        return this.getAggregationFunction();
    }

    @Override
    public double getProbability() {
        return distribution.get(getKind()).getConfidence();
    }

    @Override
    public MappingKind getKind() {
        var probName = distribution.get(MappingKind.NAME).getConfidence();
        var probType = distribution.get(MappingKind.TYPE).getConfidence();
        if (probName >= probType) {
            return MappingKind.NAME;
        }
        return MappingKind.TYPE;
    }

    private AggregationFunctions getAggregationFunction() {
        return DEFAULT_AGGREGATOR;
    }

    @Override
    public boolean couldBeMultipleKinds(MappingKind... kinds) {
        ImmutableList<Double> probabilities = Lists.immutable.with(kinds).collect(this::getProbabilityForKind);
        if (probabilities.anySatisfy(p -> p <= 0)) {
            return false;
        }

        return probabilities.allSatisfy(p1 -> probabilities.allSatisfy(p2 -> Math.abs(p1 - p2) < MAPPINGKIND_MAX_DIFF));
    }

    public boolean isTerm() {
        return isDefinedAsTerm.get();
    }

    @Override
    public String toString() {
        return "NounMapping [" + "distribution=" + distribution.entrySet()
                .stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .collect(Collectors.joining(",")) + //
                ", reference=" + getReference() + //
                ", node=" + String.join(", ", surfaceForms) + //
                ", position=" + String.join(", ", getWords().collect(word -> String.valueOf(word.getPosition()))) + //
                ", probability=" + getProbability() + ", hasPhrase=" + isTerm() + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(getReference());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NounMappingImpl)) {
            return false;
        }
        var other = (NounMapping) obj;
        return Objects.equals(getReference(), other.getReference());
    }

    @Override
    public boolean isTheSameAs(NounMapping other) {
        return Objects.equals(getReference(), other.getReference()) && Objects.equals(getWords(), other.getWords()) && Objects.equals(getKind(), other
                .getKind()) && Objects.equals(getPhrases(), other.getPhrases());
    }

    @Override
    public double getProbabilityForKind(MappingKind mappingKind) {
        return distribution.get(mappingKind).getConfidence();
    }

    @Override

    public ImmutableList<String> getSurfaceForms() {
        return this.surfaceForms;
    }

    @Override

    public ImmutableSet<Claimant> getClaimants() {
        return this.distribution.valuesView().flatCollect(Confidence::getClaimants).toImmutableSet();
    }

    public static Long earliestCreationTime(NounMapping... nounMappings) {
        Long earliest = Long.MAX_VALUE;
        for (var nounmapping : nounMappings) {
            if (nounmapping instanceof NounMappingImpl impl)
                if (impl.earliestCreationTime() < earliest)
                    earliest = impl.earliestCreationTime();
        }
        return earliest == Long.MAX_VALUE ? null : earliest;
    }

    @Override
    public int compareTo(NounMappingImpl o) {
        return Long.compare(this.earliestCreationTime, o.earliestCreationTime);
    }
}
