/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import static edu.kit.kastel.informalin.framework.common.AggregationFunctions.AVERAGE;

import java.util.*;
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
import edu.kit.kastel.mcse.ardoco.core.api.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMappingChangeListener;

/**
 * The Class NounMapping is a basic realization of {@link NounMapping}.
 */
public final class NounMappingImpl implements NounMapping, Comparable<NounMappingImpl> {

    private static final AggregationFunctions DEFAULT_AGGREGATOR = AVERAGE;
    private final Long earliestCreationTime;
    private final ImmutableSortedSet<Word> words;
    private final MutableMap<MappingKind, Confidence> distribution;
    private final ImmutableList<Word> referenceWords;
    private final ImmutableList<String> surfaceForms;
    private final String reference;
    private boolean isDefinedAsCompound;
    private final Set<NounMappingChangeListener> changeListeners;

    /**
     *
     */
    public NounMappingImpl(Long earliestCreationTime, ImmutableSortedSet<Word> words, MutableMap<MappingKind, Confidence> distribution,
            ImmutableList<Word> referenceWords, ImmutableList<String> surfaceForms, String reference) {
        this.earliestCreationTime = earliestCreationTime;
        this.words = words;
        this.distribution = distribution;
        this.referenceWords = referenceWords;
        this.surfaceForms = surfaceForms;
        this.reference = reference;
        this.isDefinedAsCompound = false;
        this.changeListeners = Collections.newSetFromMap(new IdentityHashMap<>());
    }

    /**
     * Instantiates a new noun mapping.
     */
    private NounMappingImpl(Long earliestCreationTime, ImmutableSet<Word> words, Map<MappingKind, Confidence> distribution, ImmutableList<Word> referenceWords,
            ImmutableList<String> surfaceForms) {

        this(earliestCreationTime, words.toSortedSet().toImmutable(), Maps.mutable.ofMap(distribution), referenceWords, surfaceForms, calculateReference(
                referenceWords));

        this.distribution.putIfAbsent(MappingKind.NAME, new Confidence(DEFAULT_AGGREGATOR));
        this.distribution.putIfAbsent(MappingKind.TYPE, new Confidence(DEFAULT_AGGREGATOR));
    }

    /**
     * Instantiates a new noun mapping.
     */
    public NounMappingImpl(Long earliestCreationTime, ImmutableSet<Word> words, MappingKind kind, Claimant claimant, double probability,
            ImmutableList<Word> referenceWords, ImmutableList<String> surfaceForms) {
        this(earliestCreationTime, words.toSortedSet().toImmutable(), Maps.mutable.empty(), referenceWords, surfaceForms, calculateReference(referenceWords));

        Objects.requireNonNull(claimant);
        this.distribution.putIfAbsent(MappingKind.NAME, new Confidence(DEFAULT_AGGREGATOR));
        this.distribution.putIfAbsent(MappingKind.TYPE, new Confidence(DEFAULT_AGGREGATOR));
        this.addKindWithProbability(kind, claimant, probability);
    }

    public NounMappingImpl(Long earliestCreationTime, ImmutableSet<Word> words, MappingKind kind, Claimant claimant, double probability,
            ImmutableList<Word> referenceWords, ImmutableList<String> surfaceForms, String reference) {
        this(earliestCreationTime, words.toSortedSet().toImmutable(), Maps.mutable.empty(), referenceWords, surfaceForms, reference);

        Objects.requireNonNull(claimant);
        this.distribution.putIfAbsent(MappingKind.NAME, new Confidence(DEFAULT_AGGREGATOR));
        this.distribution.putIfAbsent(MappingKind.TYPE, new Confidence(DEFAULT_AGGREGATOR));
        this.addKindWithProbability(kind, claimant, probability);

    }

    @Override
    public void registerChangeListener(NounMappingChangeListener listener) {
        changeListeners.add(listener);
    }

    @Override
    public void onDelete(NounMapping replacement) {
        changeListeners.forEach(l -> l.onDelete(this, replacement));
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
        return words.collect(Word::getText).makeString(" ");
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
    public ImmutableMap<MappingKind, Confidence> getDistribution() {
        return distribution.toImmutable();
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

    @Override
    public boolean isCompound() {
        return isDefinedAsCompound;
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
                ", probability=" + getProbability() + ", isCompound=" + isCompound() + "]";
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
        for (var mapping : nounMappings) {
            if (mapping instanceof NounMappingImpl impl && impl.earliestCreationTime() < earliest)
                earliest = impl.earliestCreationTime();
        }
        return earliest == Long.MAX_VALUE ? null : earliest;
    }

    @Override
    public int compareTo(NounMappingImpl o) {
        return Long.compare(this.earliestCreationTime, o.earliestCreationTime);
    }

    public Long earliestCreationTime() {
        return earliestCreationTime;
    }

    public ImmutableSortedSet<Word> words() {
        return words;
    }

    public MutableMap<MappingKind, Confidence> distribution() {
        return distribution;
    }

    public ImmutableList<Word> referenceWords() {
        return referenceWords;
    }

    public ImmutableList<String> surfaceForms() {
        return surfaceForms;
    }

    public String reference() {
        return reference;
    }

    public void setIsDefinedAsCompound(boolean isDefinedAsCompound) {
        this.isDefinedAsCompound = isDefinedAsCompound;
    }
}
