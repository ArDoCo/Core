/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.informalin.framework.common.AggregationFunctions;
import edu.kit.kastel.informalin.framework.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.api.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.PhraseMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.ElementWrapper;

/**
 * The Class TextState defines the basic implementation of a {@link TextState}.
 *
 */
public class TextStateImpl extends AbstractState implements TextState {
    private static final Function<NounMapping, Integer> NOUN_MAPPING_HASH = nm -> Objects.hash(nm.getReference(), nm.getPhrases());
    private static final BiPredicate<NounMapping, NounMapping> NOUN_MAPPING_EQUALS = (nm1,
            nm2) -> (Objects.equals(nm1.getPhrases(), nm2.getPhrases()) && Objects.equals(nm1.getReference(), nm2.getReference()));

    private MutableSet<ElementWrapper<NounMapping>> nounMappings;
    private MutableSet<PhraseMapping> phraseMappings;

    /**
     * Creates a new name type relation state
     */
    public TextStateImpl() {
        nounMappings = Sets.mutable.empty();
        phraseMappings = Sets.mutable.empty();
    }

    @Override
    public NounMapping addNounMapping(Word word, MappingKind kind, Claimant claimant, double probability, ImmutableSet<String> surfaceForms) {
        assert (getNounMappingByWord(word) == null) : "You should not add a noun mapping that is already contained by the text state";
        ImmutableSet words = Sets.immutable.with(word);
        NounMapping nounMapping = new NounMappingImpl(words, kind, claimant, probability, words.toImmutableList(), surfaceForms);
        addNounMappingAddPhraseMapping(nounMapping);
        return nounMapping;
    }

    @Override
    public NounMapping addNounMapping(ImmutableSet<Word> words, MappingKind kind, Claimant claimant, double probability, ImmutableList<Word> referenceWords,
            ImmutableSet<String> surfaceForms, String reference, ImmutableList<Word> coreferences) {
        NounMapping nounMapping = new NounMappingImpl(words, kind, claimant, probability, referenceWords, surfaceForms, reference, coreferences);
        assert (!getNounMappings().contains(nounMapping)) : "You should not add a noun mapping that is already contained by the text state";
        addNounMappingAddPhraseMapping(nounMapping);
        return nounMapping;
    }

    @Override
    public NounMapping addNounMapping(ImmutableSet<Word> words, MutableMap<MappingKind, Confidence> distribution, ImmutableList<Word> referenceWords,
            ImmutableSet<String> surfaceForms, String reference, ImmutableList<Word> coreferences) {

        if (reference == null) {
            reference = calculateNounMappingReference(referenceWords);
        }

        NounMapping nounMapping = new NounMappingImpl(words, distribution, referenceWords, surfaceForms, reference, coreferences);
        assert (!getNounMappings().contains(nounMapping)) : "You should not add a noun mapping that is already contained by the text state";
        addNounMappingAddPhraseMapping(nounMapping);
        return nounMapping;
    }

    /***
     * Adds a name mapping to the state
     *
     * @param word        word of the mapping
     * @param kind        mapping kind of the future noun mapping
     * @param probability probability to be a name mapping
     */
    @Override
    public NounMapping addNounMapping(Word word, MappingKind kind, Claimant claimant, double probability) {
        assert (getNounMappingByWord(word) == null) : "You should not add a noun mapping that is already contained by the text state";
        return addNounMapping(word, kind, claimant, probability, Sets.immutable.with(word.getText()));
    }

    @Override
    public ImmutableList<NounMapping> getNounMappings() {
        return this.nounMappings.toImmutableList().collect(ElementWrapper::getElement);

    }

    @Override
    public ImmutableList<PhraseMapping> getPhraseMappings() {
        return phraseMappings.toImmutableList();
    }

    public ImmutableList<PhraseMapping> getPhraseMappingsByNounMapping(NounMapping nounMapping) {

        MutableList<PhraseMapping> result = Lists.mutable.empty();

        for (Phrase phrase : nounMapping.getPhrases()) {
            result.addAll(phraseMappings.select(pm -> pm.getPhrases().contains(phrase)));
        }

        return result.toImmutable();
    }

    @Override
    public PhraseMapping getPhraseMappingByNounMapping(NounMapping nounMapping) {
        ImmutableList<PhraseMapping> phraseMappingsByNounMapping = getPhraseMappingsByNounMapping(nounMapping);
        assert (phraseMappingsByNounMapping.size() >= 1) : "Every noun mapping should be connected to a phrase mapping";
        return phraseMappingsByNounMapping.get(0);

    }

    @Override
    public ImmutableList<NounMapping> getNounMappingsByPhraseMapping(PhraseMapping phraseMapping) {
        return getNounMappings().select(nm -> phraseMapping.getPhrases().toImmutableSet().equals(nm.getPhrases()));
    }

    /**
     * Returns all type mappings.
     *
     * @param kind searched mappingKind
     * @return all type mappings as list
     */
    @Override
    public ImmutableList<NounMapping> getNounMappingsOfKind(MappingKind kind) {
        return getNounMappings().select(nounMappingIsOfKind(kind)).toImmutable();
    }

    @Override
    public ImmutableList<NounMapping> getNounMappingsThatBelongToTheSamePhraseMapping(NounMapping nounMapping) {

        return this.getNounMappingsByPhraseMapping(this.getPhraseMappingByNounMapping(nounMapping)).select(nm -> !nm.equals(nounMapping));
    }

    public NounMapping setReferenceOfNounMapping(NounMapping nounMapping, ImmutableList<Word> referenceWords, String reference) {

        assert (nounMapping.getWords().containsAllIterable(referenceWords)) : "The reference words should be contained by the noun mapping";

        return this.addNounMapping(nounMapping.getWords(), nounMapping.getDistribution().toMap(), referenceWords, nounMapping.getSurfaceForms(), reference,
                nounMapping.getCoreferences());

    }

    @Override
    public void mergeNounMappings(NounMapping nounMapping, NounMapping textuallyEqualNounMapping) {
        this.mergeNounMappings(nounMapping, textuallyEqualNounMapping, null, null, nounMapping.getKind(), null,
                nounMapping.getProbabilityForKind(nounMapping.getKind()));
    }

    private NounMapping mergeNounMappings(NounMapping nounMapping, NounMapping nounMapping2, ImmutableList<Word> referenceWords, String reference,
            MappingKind mappingKind, Claimant claimant, double probability) {

        if (!getNounMappings().contains(nounMapping) || !getNounMappings().contains(nounMapping2)) {
            throw new IllegalArgumentException("The noun mappings that are merged should be in the current text state!");
        }

        MutableSet<Word> mergedWords = nounMapping.getWords().toSet();
        mergedWords.addAllIterable(nounMapping2.getWords());

        MutableMap<MappingKind, Confidence> mergedDistribution = nounMapping.getDistribution().toMap();
        AggregationFunctions globalAggregationFunc = nounMapping.getGlobalAggregationFunction();
        AggregationFunctions localAggregationFunc = nounMapping.getLocalAggregationFunction();
        var distribution2 = nounMapping2.getDistribution().toMap();
        mergedDistribution.keySet()
                .forEach(kind -> Confidence.merge(distribution2.get(kind), distribution2.get(kind), globalAggregationFunc, localAggregationFunc));

        MutableSet<String> mergedSurfaceForms = nounMapping.getSurfaceForms().toSet();
        mergedSurfaceForms.addAllIterable(nounMapping2.getSurfaceForms());

        ImmutableList<Word> mergedReferenceWords = referenceWords;

        if (mergedReferenceWords == null) {
            MutableList<Word> mergedRefWords = Lists.mutable.withAll(nounMapping.getReferenceWords());
            mergedRefWords.addAllIterable(nounMapping2.getReferenceWords());
            mergedReferenceWords = mergedRefWords.toImmutable();
        }

        String mergedReference = reference;

        if (mergedReference == null) {
            mergedReference = calculateNounMappingReference(mergedReferenceWords);
        }

        MutableList<Word> mergedCoreferences = nounMapping.getCoreferences().toList();
        mergedCoreferences.addAllIterable(nounMapping2.getCoreferences());

        NounMapping mergedNounMapping = new NounMappingImpl(mergedWords.toImmutable(), mergedDistribution, mergedReferenceWords.toImmutable(),
                mergedSurfaceForms.toImmutable(), mergedReference, mergedCoreferences.toImmutable());
        mergedNounMapping.addKindWithProbability(mappingKind, claimant, probability);

        removeNounMappingFromState(nounMapping);
        removeNounMappingFromState(nounMapping2);

        addNounMappingAddPhraseMapping(mergedNounMapping);

        return mergedNounMapping;
    }

    private String calculateNounMappingReference(ImmutableList<Word> referenceWords) {
        StringBuilder refBuilder = new StringBuilder();
        referenceWords.toSortedListBy(Word::getPosition);
        referenceWords.toSortedListBy(Word::getSentenceNo);

        for (int i = 0; i < referenceWords.size() - 1; i++) {
            refBuilder.append(referenceWords.get(i).getText()).append(" ");
        }
        refBuilder.append(referenceWords.get(referenceWords.size() - 1).getText());
        return refBuilder.toString();
    }

    @Override
    public void mergePhraseMappingsAndNounMappings(PhraseMapping phraseMapping, PhraseMapping similarPhraseMapping,
            MutableList<Pair<NounMapping, NounMapping>> similarNounMappings) {
        mergePhraseMappings(phraseMapping, similarPhraseMapping);
        for (Pair<NounMapping, NounMapping> nounMappingPair : similarNounMappings) {
            this.mergeNounMappings(nounMappingPair.first(), nounMappingPair.second());
        }
    }

    @Override
    public void mergePhraseMappings(PhraseMapping phraseMapping, PhraseMapping similarPhraseMapping) {
        phraseMapping.merge(similarPhraseMapping);
        this.phraseMappings.remove(similarPhraseMapping);
    }

    @Override
    public NounMapping getNounMappingByWord(Word word) {
        var result = getNounMappings().select(nMapping -> nMapping.getWords().contains(word)).toImmutable();
        assert (result.size() <= 1) : "A word should only contained by one noun mapping";
        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    /**
     * Returns a list of all references of kind mappings.
     *
     * @return all references of type mappings as list.
     */
    @Override
    public ImmutableList<String> getListOfReferences(MappingKind kind) {
        Set<String> referencesOfKind = new HashSet<>();
        var kindMappings = getNounMappingsOfKind(kind);
        for (NounMapping nnm : kindMappings) {
            referencesOfKind.add(nnm.getReference());
        }
        return Lists.immutable.withAll(referencesOfKind);
    }

    private Predicate<? super NounMapping> nounMappingIsOfKind(MappingKind mappingKind) {
        return n -> n.getKind() == mappingKind;
    }

    @Override
    public TextState createCopy() {
        var textExtractionState = new TextStateImpl();
        textExtractionState.nounMappings = nounMappings.toList()
                .collect(ElementWrapper::getElement)
                .collect(NounMapping::createCopy)
                .collect(this::wrap)
                .toSet();
        textExtractionState.phraseMappings = phraseMappings.collect(PhraseMapping::createCopy);
        return textExtractionState;
    }

    private void addNounMappingAddPhraseMapping(NounMapping nounMapping) {
        addNounMappingToState(nounMapping);
        phraseMappings.add(new PhraseMappingImpl(nounMapping.getPhrases()));
    }

    @Override
    public void removeNounMapping(NounMapping nounMapping) {
        PhraseMapping phraseMapping = getPhraseMappingByNounMapping(nounMapping);

        var otherNounMappings = getNounMappingsThatBelongToTheSamePhraseMapping(nounMapping);
        if (!otherNounMappings.isEmpty()) {

            var phrases = nounMapping.getPhrases().select(p -> !otherNounMappings.flatCollect(NounMapping::getPhrases).contains(p));
            phrases.forEach(phraseMapping::removePhrase);
        }
        removeNounMappingFromState(nounMapping);
    }

    private void addNounMappingToState(NounMapping nounMapping) {
        this.nounMappings.add(wrap(nounMapping));
    }

    private void removeNounMappingFromState(NounMapping nounMapping) {
        this.nounMappings.remove(wrap(nounMapping));
    }

    private ElementWrapper<NounMapping> wrap(NounMapping nounMapping) {

        return new ElementWrapper<>(NounMapping.class, nounMapping, NOUN_MAPPING_HASH, NOUN_MAPPING_EQUALS);
    }

    @Override
    public String toString() {
        return "TextExtractionState [NounMappings: \n" + getNounMappings() + "\n PhraseMappings: \n" + getPhraseMappings() + "]";
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // handle additional configuration
    }

}
