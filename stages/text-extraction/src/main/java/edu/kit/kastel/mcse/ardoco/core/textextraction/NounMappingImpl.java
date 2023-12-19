/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import static edu.kit.kastel.mcse.ardoco.core.common.AggregationFunctions.AVERAGE;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.NounMappingChangeListener;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.architecture.NoHashCodeEquals;
import edu.kit.kastel.mcse.ardoco.core.common.AggregationFunctions;
import edu.kit.kastel.mcse.ardoco.core.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * The Class NounMapping is a basic realization of {@link NounMapping}.
 */
@Deterministic
@NoHashCodeEquals
public class NounMappingImpl implements NounMapping {

    protected static final AtomicLong CREATION_TIME_COUNTER = new AtomicLong(0);
    private static final AggregationFunctions DEFAULT_AGGREGATOR = AVERAGE;
    private final Long earliestCreationTime;
    private final MutableSortedSet<Word> words;
    private MutableSortedSet<Phrase> phrases;
    private final MutableSortedMap<MappingKind, Confidence> distribution;
    private final MutableList<Word> referenceWords;
    private final MutableList<String> surfaceForms;
    private final String reference;
    private boolean isDefinedAsCompound;
    private final Set<NounMappingChangeListener> changeListeners;

    /**
     * Instantiates a new noun mapping. A new creation time will be generated.
     *
     * @param words          the list of words for this nounmapping
     * @param kind           the kind of mapping
     * @param claimant       the claimant that created this mapping
     * @param probability    the confidence
     * @param referenceWords the reference words
     * @param surfaceForms   the surface forms
     */
    public NounMappingImpl(ImmutableSortedSet<Word> words, MappingKind kind, Claimant claimant, double probability, ImmutableList<Word> referenceWords,
            ImmutableList<String> surfaceForms) {
        this(CREATION_TIME_COUNTER.incrementAndGet(), words, kind, claimant, probability, referenceWords, surfaceForms);
    }

    /**
     * Constructor. A new creation time will be generated.
     *
     * @param words          the words
     * @param distribution   the distribution map (kind to confidence)
     * @param referenceWords the reference words
     * @param surfaceForms   the surface forms
     * @param reference      the String reference
     */

    public NounMappingImpl(ImmutableSortedSet<Word> words, ImmutableSortedMap<MappingKind, Confidence> distribution, ImmutableList<Word> referenceWords,
            ImmutableList<String> surfaceForms, String reference) {
        this(CREATION_TIME_COUNTER.incrementAndGet(), words, distribution, referenceWords, surfaceForms, reference);
    }

    /**
     * Constructor
     *
     * @param earliestCreationTime the earliest creation time
     * @param words                the words
     * @param distribution         the distribution map (kind to confidence)
     * @param referenceWords       the reference words
     * @param surfaceForms         the surface forms
     * @param reference            the String reference
     */

    public NounMappingImpl(Long earliestCreationTime, ImmutableSortedSet<Word> words, ImmutableSortedMap<MappingKind, Confidence> distribution,
            ImmutableList<Word> referenceWords, ImmutableList<String> surfaceForms, String reference) {
        this.earliestCreationTime = earliestCreationTime;
        this.words = words.toSortedSet();
        this.distribution = distribution.toSortedMap();
        this.referenceWords = referenceWords.toList();
        this.surfaceForms = surfaceForms.toList();
        this.reference = reference;
        this.isDefinedAsCompound = false;
        this.changeListeners = Collections.newSetFromMap(new IdentityHashMap<>());
    }

    /**
     * Instantiates a new noun mapping.
     *
     * @param earliestCreationTime the earliest creation time
     * @param words                the list of words for this nounmapping
     * @param kind                 the kind of mapping
     * @param claimant             the claimant that created this mapping
     * @param probability          the confidence
     * @param referenceWords       the reference words
     * @param surfaceForms         the surface forms
     */
    public NounMappingImpl(Long earliestCreationTime, ImmutableSortedSet<Word> words, MappingKind kind, Claimant claimant, double probability,
            ImmutableList<Word> referenceWords, ImmutableList<String> surfaceForms) {
        this(earliestCreationTime, words.toSortedSet().toImmutable(), SortedMaps.immutable.empty(), referenceWords, surfaceForms, calculateReference(
                referenceWords));

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
        return words.toImmutable();
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
        return referenceWords.toImmutable();
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
    public ImmutableSortedSet<Phrase> getPhrases() {
        if (phrases == null) {
            this.phrases = SortedSets.mutable.empty();
            for (Word word : words) {
                if (phrases.contains(word.getPhrase()))
                    continue;
                phrases.add(word.getPhrase());
            }
        }
        return this.phrases.toImmutable();
    }

    @Override
    public void addKindWithProbability(MappingKind kind, Claimant claimant, double probability) {
        var currentProbability = distribution.get(kind);
        Objects.requireNonNull(claimant);
        currentProbability.addAgentConfidence(claimant, probability);
    }

    @Override
    public ImmutableSortedMap<MappingKind, Confidence> getDistribution() {
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
        return "NounMapping [" + "distribution=" + distribution.keyValuesView().collect(entry -> entry.getOne() + ":" + entry.getTwo()).makeString(",") + //
                ", reference=" + getReference() + //
                ", node=" + String.join(", ", surfaceForms) + //
                ", position=" + String.join(", ", getWords().collect(word -> String.valueOf(word.getPosition()))) + //
                ", probability=" + getProbability() + ", isCompound=" + isCompound() + "]";
    }

    @Override
    public double getProbabilityForKind(MappingKind mappingKind) {
        return distribution.get(mappingKind).getConfidence();
    }

    @Override
    public ImmutableList<String> getSurfaceForms() {
        return this.surfaceForms.toImmutable();
    }

    @Override
    public ImmutableList<Claimant> getClaimants() {
        Set<Claimant> identitySet = new LinkedHashSet<>();
        for (var claimant : this.distribution.valuesView().flatCollect(Confidence::getClaimants))
            identitySet.add(claimant);
        return Lists.immutable.withAll(identitySet);
    }

    public static Long earliestCreationTime(NounMapping... nounMappings) {
        Long earliest = Long.MAX_VALUE;
        for (var mapping : nounMappings) {
            if (mapping instanceof NounMappingImpl impl && impl.earliestCreationTime() < earliest)
                earliest = impl.earliestCreationTime();
        }
        return earliest == Long.MAX_VALUE ? null : earliest;
    }

    public Long earliestCreationTime() {
        return earliestCreationTime;
    }

    public ImmutableSortedSet<Word> words() {
        return words.toImmutable();
    }

    public MutableSortedMap<MappingKind, Confidence> distribution() {
        return distribution;
    }

    public ImmutableList<Word> referenceWords() {
        return referenceWords.toImmutable();
    }

    public ImmutableList<String> surfaceForms() {
        return surfaceForms.toImmutable();
    }

    public String reference() {
        return reference;
    }

    public void setIsDefinedAsCompound(boolean isDefinedAsCompound) {
        this.isDefinedAsCompound = isDefinedAsCompound;
    }
}
