/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import static edu.kit.kastel.informalin.framework.common.AggregationFunctions.AVERAGE;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.informalin.framework.common.AggregationFunctions;
import edu.kit.kastel.informalin.framework.common.JavaUtils;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

/**
 * The Class NounMapping is a basic realization of {@link NounMapping}.
 */
public class NounMappingImpl implements NounMapping {

    /**
     * Minimum difference that need to shall not be reached to identify a NounMapping as NameOrType.
     */
    private static final double MAPPINGKIND_MAX_DIFF = 0.1;

    /* Main reference */
    private final ImmutableList<Word> referenceWords;

    /* Words are the references within the text */
    private final MutableList<Word> words;

    private final MutableList<Word> coreferences = Lists.mutable.empty();

    /* the different surface forms */
    private final MutableList<String> surfaceForms;

    private Map<MappingKind, Confidence> distribution;

    private static final AggregationFunctions DEFAULT_AGGREGATOR = AVERAGE;

    /**
     * Instantiates a new noun mapping.
     */
    public NounMappingImpl(ImmutableList<Word> words, Map<MappingKind, Confidence> distribution, ImmutableList<Word> referenceWords,
            ImmutableList<String> surfaceForms) {
        this.words = Lists.mutable.withAll(words);
        initializeDistribution(distribution);
        this.referenceWords = Lists.immutable.withAll(referenceWords);
        this.surfaceForms = Lists.mutable.withAll(surfaceForms);
    }

    /**
     * Instantiates a new noun mapping.
     */
    public NounMappingImpl(ImmutableList<Word> words, MappingKind kind, Claimant claimant, double probability, ImmutableList<Word> referenceWords,
            ImmutableList<String> occurrences) {
        Objects.requireNonNull(claimant);

        distribution = new EnumMap<>(MappingKind.class);
        distribution.put(kind, new Confidence(claimant, probability, DEFAULT_AGGREGATOR));

        this.words = Lists.mutable.withAll(words);
        initializeDistribution(distribution);
        this.referenceWords = Lists.immutable.withAll(referenceWords);
        surfaceForms = Lists.mutable.withAll(occurrences);
    }

    private void initializeDistribution(Map<MappingKind, Confidence> distribution) {
        this.distribution = new EnumMap<>(distribution);
        this.distribution.putIfAbsent(MappingKind.NAME, new Confidence(DEFAULT_AGGREGATOR));
        this.distribution.putIfAbsent(MappingKind.TYPE, new Confidence(DEFAULT_AGGREGATOR));
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
    public final ImmutableList<Word> getWords() {
        return Lists.immutable.withAll(words);
    }

    /**
     * Adds nodes to the mapping, if they are not already contained.
     *
     * @param words graph nodes to add to the mapping
     */
    @Override
    public final void addWords(ImmutableList<Word> words) {
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
    public final void addWord(Word word) {
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
        if (referenceWords.size() == 1) {
            return referenceWords.get(0).getText();
        }
        return CommonUtilities.createReferenceForPhrase(referenceWords);
    }

    /**
     * Returns the reference words
     *
     * @return the reference words
     */
    @Override
    public final ImmutableList<Word> getReferenceWords() {
        return referenceWords;
    }

    /**
     * Returns the sentence numbers of occurrences, sorted.
     *
     * @return sentence numbers of the occurrences of this mapping.
     */
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
    public NounMapping splitByPhrase(Phrase phrase) {

        MutableList<Word> wordsToRemove = Lists.mutable.empty();

        for (Word word : words) {
            if (word.getPhrase().equals(phrase)) {
                wordsToRemove.add(word);
            }
        }

        words.removeAll(wordsToRemove);

        // TODO: Recalculate confidence

        // return noun mapping out of removed words
        return new NounMappingImpl(wordsToRemove.toImmutable(), getDistribution(), getReferenceWords(), getSurfaceForms());

    }

    /**
     * Adds occurrences to the mapping
     *
     * @param newOccurrences occurrences to add
     */
    @Override
    public final void addOccurrence(ImmutableList<String> newOccurrences) {
        for (String o : newOccurrences) {
            if (!surfaceForms.contains(o)) {
                surfaceForms.add(o);
            }
        }
    }

    /**
     * Adds the kind with probability.
     *
     * @param kind        the kind
     * @param probability the probability
     */
    @Override
    public void addKindWithProbability(MappingKind kind, Claimant claimant, double probability) {
        var currentProbability = distribution.get(kind);
        currentProbability.addAgentConfidence(claimant, probability);
    }

    @Override
    public NounMapping createCopy() {
        return new NounMappingImpl(words.toImmutable(), JavaUtils.copyMap(this.distribution, Confidence::createCopy), Lists.immutable.withAll(referenceWords),
                surfaceForms.toImmutable());
    }

    @Override
    public Map<MappingKind, Confidence> getDistribution() {
        return new EnumMap<>(distribution);
    }

    /**
     * Splits all occurrences with a whitespace in it at their spaces and returns all parts that are similar to the
     * reference. If it contains a separator or similar to the reference it is added to the comparables as a whole.
     *
     * @return all parts of occurrences (split at their spaces) that are similar to the reference.
     */
    @Override
    public ImmutableList<String> getRepresentativeComparables() {
        MutableList<String> comparables = Lists.mutable.empty();
        for (String occ : surfaceForms) {
            if (CommonUtilities.containsSeparator(occ)) {
                var parts = CommonUtilities.splitAtSeparators(occ);
                for (String part : parts) {
                    if (SimilarityUtils.areWordsSimilar(getReference(), part)) {
                        comparables.add(part);
                    }
                }
                comparables.add(occ);
            } else if (SimilarityUtils.areWordsSimilar(getReference(), occ)) {
                comparables.add(occ);
            }
        }
        return comparables.toImmutable();
    }

    /**
     * Returns the probability of being a mapping of its kind.
     *
     * @return probability of being a mapping of its kind.
     */
    @Override
    public double getProbability() {
        return distribution.get(getKind()).getConfidence();
    }

    /**
     * Returns the kind: name, type.
     *
     * @return the kind
     */
    @Override
    public MappingKind getKind() {
        var probName = distribution.get(MappingKind.NAME).getConfidence();
        var probType = distribution.get(MappingKind.TYPE).getConfidence();
        if (probName >= probType) {
            return MappingKind.NAME;
        }
        return MappingKind.TYPE;
    }

    /**
     * @return the coreferences
     */
    @Override
    public ImmutableList<Word> getCoreferences() {
        return coreferences.toImmutable();
    }

    @Override
    public AggregationFunctions getAggregationFunction() {
        return DEFAULT_AGGREGATOR;
    }

    /**
     * @param coreferences the coreferences to add
     */
    @Override
    public void addCoreferences(Collection<Word> coreferences) {
        this.coreferences.addAll(coreferences);
    }

    /**
     * @param coreference the coreference to add
     */
    @Override
    public void addCoreference(Word coreference) {
        coreferences.add(coreference);
    }

    @Override
    public NounMapping merge(NounMapping other) {
        Map<MappingKind, Confidence> otherDistribution = other.getDistribution();

        otherDistribution.keySet()
                .forEach(kind -> Confidence.merge(distribution.get(kind), otherDistribution.get(kind), DEFAULT_AGGREGATOR, DEFAULT_AGGREGATOR));

        this.addOccurrence(other.getSurfaceForms());
        other.getWords().forEach(this::addWord);

        return this;
    }

    @Override
    public NounMapping split(ImmutableList<Word> words) {
        var sharedWords = this.words.select(words::contains);
        this.words.removeAll(sharedWords.toList());
        return new NounMappingImpl(sharedWords.toImmutable(), distribution, referenceWords, surfaceForms.toImmutable());
    }

    @Override
    public boolean couldBeMultipleKinds(MappingKind... kinds) {
        ImmutableList<Double> probabilities = Lists.immutable.with(kinds).collect(this::getProbabilityForKind);
        if (probabilities.anySatisfy(p -> p <= 0)) {
            return false;
        }

        return probabilities.allSatisfy(p1 -> probabilities.allSatisfy(p2 -> Math.abs(p1 - p2) < MAPPINGKIND_MAX_DIFF));
    }

    @Override
    public String toString() {
        return "NounMapping [" + "distribution="
                + distribution.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).collect(Collectors.joining(",")) + //
                ", reference=" + getReference() + //
                ", node=" + String.join(", ", surfaceForms) + //
                ", position=" + String.join(", ", getWords().collect(word -> String.valueOf(word.getPosition()))) + //
                ", probability=" + getProbability() + "]";
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
        return Objects.equals(getReference(), other.getReference()) && Objects.equals(getWords(), other.getWords())
                && Objects.equals(getKind(), other.getKind()) && Objects.equals(getPhrases(), other.getPhrases());
    }

    @Override
    public boolean containsSameWordsAs(NounMapping nounMapping) {
        // getWords().anySatisfy(w -> w.getPosition() == nounMapping.getWords().get(0).getPosition()
        // && w.getSentenceNo() == nounMapping.getWords().get(0).getSentenceNo())
        return words.size() == nounMapping.getWords().size() && this.words.containsAllIterable(nounMapping.getWords());
    }

    @Override
    public boolean sharesTextualWordRepresentation(NounMapping nounMapping) {
        return nounMapping.getWords().allSatisfy(w -> this.getWords().anySatisfy(thisW -> thisW.getText().equals(w.getText())));
    }

    @Override
    public double getProbabilityForKind(MappingKind mappingKind) {
        return distribution.get(mappingKind).getConfidence();
    }
}
