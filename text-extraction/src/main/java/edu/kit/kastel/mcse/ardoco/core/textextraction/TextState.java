/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.informalin.framework.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.api.agent.IClaimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IPhrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.IPhraseMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.common.util.ElementWrapper;

/**
 * The Class TextState defines the basic implementation of a {@link ITextState}.
 *
 * @author Sophie Schulz
 * @author Jan Keim
 */
public class TextState extends AbstractState implements ITextState {

    private MutableSet<ElementWrapper<INounMapping>> nounMappings;
    private MutableSet<IPhraseMapping> phraseMappings;

    /**
     * Creates a new name type relation state
     *
     * @param configs any additional configuration
     */
    public TextState(Map<String, String> configs) {
        super(configs);
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
    public final void addNounMapping(IWord word, MappingKind kind, IClaimant claimant, double probability, ImmutableList<String> occurrences) {
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
    public final void addNounMapping(IWord word, MappingKind kind, IClaimant claimant, double probability) {

        addNounMapping(word, kind, claimant, probability, Lists.immutable.with(word.getText()));
    }

    @Override
    public final ImmutableList<INounMapping> getNounMappings() {
        return this.nounMappings.toImmutableList().collect(ElementWrapper::getElement);

    }

    @Override
    public ImmutableList<IPhraseMapping> getPhraseMappings() {
        return phraseMappings.toImmutableList();
    }

    public ImmutableList<IPhraseMapping> getPhraseMappingsByNounMapping(INounMapping nm) {

        MutableList<IPhraseMapping> result = Lists.mutable.empty();

        for (IPhrase phrase : nm.getPhrases()) {
            result.addAll(phraseMappings.select(pm -> pm.getPhrases().contains(phrase)));
        }

        return result.toImmutable();
    }

    public IPhraseMapping getPhraseMappingByNounMapping(INounMapping nounMapping) {
        ImmutableList<IPhraseMapping> phraseMappingsByNounMapping = getPhraseMappingsByNounMapping(nounMapping);
        assert (phraseMappingsByNounMapping.size() >= 1) : "Every noun mapping should be connected to a phrase mapping";
        return phraseMappingsByNounMapping.get(0);

    }

    public ImmutableList<INounMapping> getNounMappingsByPhraseMapping(IPhraseMapping phraseMapping) {
        return getNounMappings().select(nm -> phraseMapping.getPhrases().toImmutableSet().equals(nm.getPhrases()));
    }

    /**
     * Returns all type mappings.
     *
     * @param kind searched mappingKind
     * @return all type mappings as list
     */
    @Override
    public final ImmutableList<INounMapping> getNounMappingsOfKind(MappingKind kind) {
        return getNounMappings().select(nounMappingIsOfKind(kind)).toImmutable();
    }

    @Override
    public ImmutableList<INounMapping> getNounMappingsThatBelongToTheSamePhraseMapping(INounMapping nounMapping) {

        return this.getNounMappingsByPhraseMapping(this.getPhraseMappingByNounMapping(nounMapping)).select(nm -> !nm.equals(nounMapping));
    }

    @Override
    public void mergeNounMappings(INounMapping nounMapping, INounMapping textuallyEqualNounMapping) {

        nounMapping.merge(textuallyEqualNounMapping);
        removeNounMappingFromState(textuallyEqualNounMapping);

    }

    @Override
    public void mergePhraseMappingsAndNounMappings(IPhraseMapping phraseMapping, IPhraseMapping similarPhraseMapping,
            MutableList<Pair<INounMapping, INounMapping>> similarNounMappings) {

        mergePhraseMappings(phraseMapping, similarPhraseMapping);
        for (Pair<INounMapping, INounMapping> nounMappingPair : similarNounMappings) {
            nounMappingPair.first().merge(nounMappingPair.second());
            removeNounMappingFromState(nounMappingPair.second());
        }
    }

    @Override
    public void mergePhraseMappings(IPhraseMapping phraseMapping, IPhraseMapping similarPhraseMapping) {
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
    public final ImmutableList<INounMapping> getNounMappingsByWord(IWord word) {
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
    public final ImmutableList<String> getListOfReferences(MappingKind kind) {
        Set<String> referencesOfKind = new HashSet<>();
        var kindMappings = getNounMappingsOfKind(kind);
        for (INounMapping nnm : kindMappings) {
            referencesOfKind.add(nnm.getReference());
        }
        return Lists.immutable.withAll(referencesOfKind);
    }

    /**
     * Returns all type mappings containing the given node.
     *
     * @param word word to filter for
     * @return a list of alltype mappings containing the given node
     */
    @Override
    public final ImmutableList<INounMapping> getNounMappingsByWordTextAndKind(IWord word, MappingKind kind) {
        return getNounMappings().select(n -> n.getWords().anySatisfy(w -> w.getText().equals(word.getText()))).select(nounMappingIsOfKind(kind)).toImmutable();
    }

    /**
     * Returns if a node is contained by the name mappings.
     *
     * @param word        node to check
     * @param mappingKind mappingKind to check for
     * @return true if the node is contained by name mappings.
     */
    @Override
    public final boolean isWordContainedByMappingKind(IWord word, MappingKind mappingKind) {
        return getNounMappings().select(n -> n.getWords().contains(word)).anySatisfy(nounMappingIsOfKind(mappingKind));
    }

    private Predicate<? super INounMapping> nounMappingIsOfKind(MappingKind mappingKind) {
        return n -> n.getKind() == mappingKind;
    }

    @Override
    public ITextState createCopy() {
        var textExtractionState = new TextState(this.configs);
        textExtractionState.nounMappings = nounMappings.collect(ElementWrapper::getElement).collect(INounMapping::createCopy).collect(this::wrap);
        textExtractionState.phraseMappings = phraseMappings.collect(IPhraseMapping::createCopy);
        return textExtractionState;
    }

    @Override
    public void addNounMapping(INounMapping nounMapping, IClaimant claimant) {
        addNounMappingOrExtendExistingNounMapping(nounMapping.getWords(), nounMapping.getKind(), claimant, nounMapping.getProbability(),
                nounMapping.getSurfaceForms());
    }

    private void addNounMappingOrExtendExistingNounMapping(ImmutableList<IWord> words, MappingKind kind, IClaimant claimant, double probability,
            ImmutableList<String> occurrences) {

        INounMapping nounMapping = new NounMapping(words, kind, claimant, probability, Lists.immutable.withAll(words), occurrences);

        addNounMappingAddPhraseMapping(nounMapping);

    }

    private void addNounMappingAddPhraseMapping(INounMapping nounMapping) {

        addNounMappingToState(nounMapping);
        phraseMappings.add(new PhraseMapping(nounMapping.getPhrases()));
    }

    public void removeNounMapping(INounMapping nounMapping) {
        IPhraseMapping phraseMapping = getPhraseMappingByNounMapping(nounMapping);

        var otherNounMappings = getNounMappingsThatBelongToTheSamePhraseMapping(nounMapping);
        if (!otherNounMappings.isEmpty()) {

            var phrases = nounMapping.getPhrases().select(p -> !otherNounMappings.flatCollect(INounMapping::getPhrases).contains(p));
            phrases.forEach(phraseMapping::removePhrase);
        }
        removeNounMappingFromState(nounMapping);
    }

    private void addNounMappingToState(INounMapping nounMapping) {
        this.nounMappings.add(wrap(nounMapping));
    }

    private void removeNounMappingFromState(INounMapping nounMapping) {
        this.nounMappings.remove(wrap(nounMapping));
    }

    private ElementWrapper<INounMapping> wrap(INounMapping nounMapping) {

        return new ElementWrapper<>(INounMapping.class, nounMapping, nm -> Objects.hash(nm.getReference(), nm.getPhrases()),
                (nm1, nm2) -> (Objects.equals(nm1.getPhrases(), nm2.getPhrases()) && Objects.equals(nm1.getReference(), nm2.getReference())));
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
