/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import edu.kit.kastel.informalin.framework.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.PhraseMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.ElementWrapper;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;

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

    /***
     * Adds a name mapping to the state
     *
     * @param kind        mapping kind of the future noun mapping
     * @param probability probability to be a name mapping
     * @param occurrences list of the appearances of the mapping
     * @param word        node of the mapping
     */
    @Override
    public void addNounMapping(Word word, MappingKind kind, Claimant claimant, double probability, ImmutableList<String> occurrences) {
        var words = Lists.immutable.with(word);
        addNounMappingOrExtendExistingNounMapping(words, kind, claimant, probability, occurrences);
    }

    /***
     * Adds a name mapping to the state
     *
     * @param word        word of the mapping
     * @param kind        mapping kind of the future noun mapping
     * @param probability probability to be a name mapping
     */
    @Override
    public void addNounMapping(Word word, MappingKind kind, Claimant claimant, double probability) {
        addNounMapping(word, kind, claimant, probability, Lists.immutable.with(word.getText()));
    }

    @Override
    public ImmutableList<NounMapping> getNounMappings() {
        return this.nounMappings.toImmutableList().collect(ElementWrapper::getElement);

    }

    @Override
    public ImmutableList<PhraseMapping> getPhraseMappings() {
        return phraseMappings.toImmutableList();
    }

    public ImmutableList<PhraseMapping> getPhraseMappingsByNounMapping(NounMapping nm) {

        MutableList<PhraseMapping> result = Lists.mutable.empty();

        for (Phrase phrase : nm.getPhrases()) {
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
    public ImmutableList<NounMapping> getNounMappingsByWordAndKind(Word word, MappingKind type) {
        return this.getNounMappingsByWord(word).select(nm -> nm.getKind() == type);
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
    public void mergeNounMappings(NounMapping nounMapping, NounMapping textuallyEqualNounMapping) {
        nounMapping.merge(textuallyEqualNounMapping);
        removeNounMappingFromState(textuallyEqualNounMapping);
    }

    @Override
    public void mergePhraseMappingsAndNounMappings(PhraseMapping phraseMapping, PhraseMapping similarPhraseMapping,
            MutableList<Pair<NounMapping, NounMapping>> similarNounMappings) {
        mergePhraseMappings(phraseMapping, similarPhraseMapping);
        for (Pair<NounMapping, NounMapping> nounMappingPair : similarNounMappings) {
            nounMappingPair.first().merge(nounMappingPair.second());
            removeNounMappingFromState(nounMappingPair.second());
        }
    }

    @Override
    public void mergePhraseMappings(PhraseMapping phraseMapping, PhraseMapping similarPhraseMapping) {
        phraseMapping.merge(similarPhraseMapping);
        this.phraseMappings.remove(similarPhraseMapping);
    }

    /**
     * Returns all mappings containing the given word.
     *
     * @param word the given word
     * @return all mappings containing the given node as list
     */
    @Override
    public ImmutableList<NounMapping> getNounMappingsByWord(Word word) {
        var result = getNounMappings().select(nMapping -> nMapping.getWords().contains(word)).toImmutable();
        assert (result.size() <= 1) : "A word should only contained by one noun mapping";
        // TODO: Refactor
        return result;
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

    /**
     * Returns if a node is contained by the name mappings.
     *
     * @param word        node to check
     * @param mappingKind mappingKind to check for
     * @return true if the node is contained by name mappings.
     */
    @Override
    public boolean isWordContainedByMappingKind(Word word, MappingKind mappingKind) {
        return getNounMappings().select(n -> n.getWords().contains(word)).anySatisfy(nounMappingIsOfKind(mappingKind));
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

    @Override
    public void addNounMapping(NounMapping nounMapping, Claimant claimant) {
        addNounMappingOrExtendExistingNounMapping(nounMapping.getWords(), nounMapping.getKind(), claimant, nounMapping.getProbability(),
                nounMapping.getSurfaceForms());
    }

    private void addNounMappingOrExtendExistingNounMapping(ImmutableList<Word> words, MappingKind kind, Claimant claimant, double probability,
            ImmutableList<String> occurrences) {

        NounMapping nounMapping = new NounMappingImpl(words, kind, claimant, probability, Lists.immutable.withAll(words), occurrences);
        addNounMappingAddPhraseMapping(nounMapping);
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
