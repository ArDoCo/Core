/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.PhraseMapping;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.TextStateStrategy;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.common.util.ElementWrapper;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * The Class TextState defines the basic implementation of a {@link TextState}.
 */
public class TextStateImpl extends AbstractState implements TextState {
    private static final Comparator<ElementWrapper<NounMapping>> ORDER_NOUNMAPPING = (Comparator<ElementWrapper<NounMapping>> & Serializable) (n1, n2) -> {
        if (n1.equals(n2))
            return 0;
        var nm1 = (NounMappingImpl) n1.getElement();
        var nm2 = (NounMappingImpl) n2.getElement();
        int compare = Long.compare(nm1.earliestCreationTime(), nm2.earliestCreationTime());
        if (compare != 0)
            return compare;

        // Not equal but at same time -> order by something .. e.g., hash ..
        return Integer.compare(n1.hashCode(), n2.hashCode());
    };

    /**
     * Minimum difference that need to shall not be reached to identify a NounMapping as NameOrType.
     *
     * @see #getMappingsThatCouldBeOfKind(Word, MappingKind)
     */
    private static final double MAPPING_KIND_MAX_DIFF = 0.1;
    private MutableList<ElementWrapper<NounMapping>> nounMappings;
    private MutableSet<PhraseMapping> phraseMappings;
    private TextStateStrategy strategy;

    /**
     * Creates a new name type relation state
     */
    public TextStateImpl() {
        this(OriginalTextStateStrategy::new);
    }

    public TextStateImpl(Function<TextStateImpl, TextStateStrategy> constructor) {
        nounMappings = Lists.mutable.empty();
        phraseMappings = Sets.mutable.empty();
        strategy = constructor.apply(this);
    }

    @Override
    public void setTextStateStrategy(TextStateStrategy textStateStrategy) {
        this.strategy = textStateStrategy;
    }

    @Override
    public TextStateStrategy getTextStateStrategy() {
        return this.strategy;
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

    @Override
    public NounMapping setReferenceOfNounMapping(NounMapping nounMapping, ImmutableList<Word> referenceWords, String reference) {

        return this.addNounMapping(nounMapping.getWords().toImmutableSet(), nounMapping.getDistribution().toMap(), referenceWords,
                nounMapping.getSurfaceForms(), reference);

    }

    @Override
    public ImmutableList<NounMapping> getMappingsThatCouldBeOfKind(Word word, MappingKind kind) {
        return getNounMappingsByWord(word).select(mapping -> mapping.getProbabilityForKind(kind) > 0);
    }

    @Override
    public ImmutableList<NounMapping> getMappingsThatCouldBeMultipleKinds(Word word, MappingKind... kinds) {
        if (kinds.length == 0) {
            throw new IllegalArgumentException("You need to provide some mapping kinds!");
        }

        if (kinds.length < 2) {
            return getNounMappingsOfKind(kinds[0]);
        }

        MutableList<NounMapping> result = Lists.mutable.empty();
        ImmutableList<NounMapping> mappings = getNounMappingsByWord(word);

        for (NounMapping mapping : mappings) {
            ImmutableList<Double> probabilities = Lists.immutable.with(kinds).collect(mapping::getProbabilityForKind);
            if (probabilities.anySatisfy(p -> p <= 0)) {
                continue;
            }

            boolean similar = probabilities.allSatisfy(p1 -> probabilities.allSatisfy(p2 -> Math.abs(p1 - p2) < MAPPING_KIND_MAX_DIFF));
            if (similar) {
                result.add(mapping);
            }

        }

        return result.toImmutable();
    }

    @Override
    public ImmutableList<NounMapping> getNounMappingsByWord(Word word) {
        return getNounMappings().select(nm -> nm.getWords().contains(word));
    }

    @Override
    public ImmutableList<NounMapping> getNounMappingsByWordAndKind(Word word, MappingKind kind) {
        return getNounMappings().select(n -> n.getWords().contains(word)).select(nounMappingIsOfKind(kind)).toImmutable();
    }

    @Override
    public boolean isWordContainedByMappingKind(Word word, MappingKind kind) {
        return getNounMappings().select(n -> n.getWords().contains(word)).anySatisfy(nounMappingIsOfKind(kind));
    }

    @Override
    public ImmutableList<NounMapping> getNounMappingsWithSimilarReference(String reference) {
        return getNounMappings().select(nm -> SimilarityUtils.areWordsSimilar(reference, nm.getReference())).toImmutable();
    }

    @Override
    public NounMapping mergeNounMappings(NounMapping nounMapping, NounMapping textuallyEqualNounMapping, Claimant claimant) {
        return strategy.mergeNounMappings(nounMapping, textuallyEqualNounMapping, null, null, nounMapping.getKind(), claimant,
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
    public PhraseMapping mergePhraseMappings(PhraseMapping phraseMapping, PhraseMapping similarPhraseMapping) {

        MutableSet<Phrase> mergedPhrases = phraseMapping.getPhrases().toSet();
        mergedPhrases.addAll(similarPhraseMapping.getPhrases().toList());

        PhraseMapping mergedPhraseMapping = new PhraseMappingImpl(mergedPhrases);

        this.phraseMappings.add(mergedPhraseMapping);

        this.removePhraseMappingFromState(phraseMapping, mergedPhraseMapping);
        this.removePhraseMappingFromState(similarPhraseMapping, mergedPhraseMapping);
        return mergedPhraseMapping;
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

    void addNounMappingAddPhraseMapping(NounMapping nounMapping) {
        //FIXME I think this should also be delegated to a strategy, rather than creating the phrasemappingimpl directly
        addNounMappingToState(nounMapping);
        phraseMappings.add(new PhraseMappingImpl(nounMapping.getPhrases().castToSet()));
    }

    @Override
    public void removeNounMapping(NounMapping nounMapping, NounMapping replacement) {
        PhraseMapping phraseMapping = getPhraseMappingByNounMapping(nounMapping);

        var otherNounMappings = getNounMappingsThatBelongToTheSamePhraseMapping(nounMapping);
        if (!otherNounMappings.isEmpty()) {

            var phrases = nounMapping.getPhrases().select(p -> !otherNounMappings.flatCollect(NounMapping::getPhrases).contains(p));
            phrases.forEach(phraseMapping::removePhrase);
        }
        removeNounMappingFromState(nounMapping, replacement);
    }

    private void addNounMappingToState(NounMapping nounMapping) {
        if (this.nounMappings.contains(wrap(nounMapping))) {
            throw new IllegalArgumentException("Nounmapping was already in state");
        }
        this.nounMappings.add(wrap(nounMapping));
        this.nounMappings.sortThis(ORDER_NOUNMAPPING);
    }

    void removePhraseMappingFromState(PhraseMapping phraseMapping, PhraseMapping replacement) {
        this.phraseMappings.remove(phraseMapping);
        phraseMapping.onDelete(replacement);
    }

    void removeNounMappingFromState(NounMapping nounMapping, NounMapping replacement) {
        this.nounMappings.remove(wrap(nounMapping));
        nounMapping.onDelete(replacement);
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
