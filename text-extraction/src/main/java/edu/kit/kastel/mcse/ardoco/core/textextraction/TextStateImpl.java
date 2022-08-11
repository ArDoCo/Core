/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

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

    private MutableSet<ElementWrapper<NounMapping>> nounMappings;
    private MutableSet<PhraseMapping> phraseMappings;
    private final TextStateStrategy strategy;

    /**
     * Creates a new name type relation state
     */
    public TextStateImpl() {
        nounMappings = Sets.mutable.empty();
        phraseMappings = Sets.mutable.empty();
        strategy = new PhraseConcerningTextStateStrategy(this);
    }

    @Override
    public NounMapping addNounMapping(Word word, MappingKind kind, Claimant claimant, double probability) {
        return strategy.addOrExtendNounMapping(word, kind, claimant, probability, null);
    }

    @Override
    public NounMapping addNounMapping(Word word, MappingKind kind, Claimant claimant, double probability, ImmutableSet<String> surfaceForms) {
        return strategy.addOrExtendNounMapping(word, kind, claimant, probability, surfaceForms);
    }

    @Override
    public NounMapping addNounMapping(ImmutableSet<Word> words, MappingKind kind, Claimant claimant, double probability, ImmutableList<Word> referenceWords,
            ImmutableSet<String> surfaceForms, String reference, ImmutableList<Word> coreferences) {
        NounMapping nounMapping = new NounMappingImpl(words, kind, claimant, probability, referenceWords, surfaceForms, reference, coreferences);

        var nounMappings = getNounMappings();
        for (Word word : words) {
            assert (nounMappings.select(nm -> nm.getWords().contains(word)).size() == 0);
        }
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
        var nounMappings = getNounMappings();
        for (Word word : words) {
            assert (nounMappings.select(nm -> nm.getWords().contains(word)).size() == 0);
        }
        addNounMappingAddPhraseMapping(nounMapping);
        return nounMapping;
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

    @Override
    public void mergeNounMappings(NounMapping nounMapping, NounMapping otherNounMapping, Claimant claimant, ImmutableList<Word> referenceWords) {
        strategy.mergeNounMappings(nounMapping, otherNounMapping, referenceWords, null, nounMapping.getKind(), claimant,
                nounMapping.getProbabilityForKind(nounMapping.getKind()));
    }

    public NounMapping setReferenceOfNounMapping(NounMapping nounMapping, ImmutableList<Word> referenceWords, String reference) {

        assert (nounMapping.getWords().containsAllIterable(referenceWords)) : "The reference words should be contained by the noun mapping";

        return this.addNounMapping(nounMapping.getWords(), nounMapping.getDistribution().toMap(), referenceWords, nounMapping.getSurfaceForms(), reference,
                nounMapping.getCoreferences());

    }

    @Override
    public void mergeNounMappings(NounMapping nounMapping, MutableList<NounMapping> nounMappingsToMerge, Claimant claimant) {
        strategy.mergeNounMappings(nounMapping, nounMappingsToMerge, claimant);
    }

    @Override
    public void mergeNounMappings(NounMapping nounMapping, NounMapping textuallyEqualNounMapping, Claimant claimant) {
        strategy.mergeNounMappings(nounMapping, textuallyEqualNounMapping, null, null, nounMapping.getKind(), claimant,
                nounMapping.getProbabilityForKind(nounMapping.getKind()));

    }

    @Override
    public void mergePhraseMappingsAndNounMappings(PhraseMapping phraseMapping, PhraseMapping similarPhraseMapping,
            MutableList<Pair<NounMapping, NounMapping>> similarNounMappings, Claimant claimant) {
        mergePhraseMappings(phraseMapping, similarPhraseMapping);
        for (Pair<NounMapping, NounMapping> nounMappingPair : similarNounMappings) {
            this.mergeNounMappings(nounMappingPair.first(), nounMappingPair.second(), claimant);
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

    void addNounMappingAddPhraseMapping(NounMapping nounMapping) {
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

    String calculateNounMappingReference(ImmutableList<Word> referenceWords) {
        StringBuilder refBuilder = new StringBuilder();
        referenceWords.toSortedListBy(Word::getPosition);
        referenceWords.toSortedListBy(Word::getSentenceNo);

        for (int i = 0; i < referenceWords.size() - 1; i++) {
            refBuilder.append(referenceWords.get(i).getText()).append(" ");
        }
        refBuilder.append(referenceWords.get(referenceWords.size() - 1).getText());
        return refBuilder.toString();
    }

    private void addNounMappingToState(NounMapping nounMapping) {
        this.nounMappings.add(wrap(nounMapping));
    }

    void removeNounMappingFromState(NounMapping nounMapping) {
        this.nounMappings.remove(wrap(nounMapping));
    }

    private ElementWrapper<NounMapping> wrap(NounMapping nounMapping) {

        return strategy.wrap(nounMapping);
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
