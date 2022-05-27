/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.agent.IClaimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IPhrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.IPhraseMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

/**
 * The Class TextState defines the basic implementation of a {@link ITextState}.
 *
 * @author Sophie Schulz
 * @author Jan Keim
 */
public class TextState extends AbstractState implements ITextState {

    private MutableList<INounMapping> nounMappings;

    private MutableList<IPhraseMapping> phraseMappings;

    /**
     * Creates a new name type relation state
     *
     * @param configs any additional configuration
     */
    public TextState(Map<String, String> configs) {
        super(configs);
        nounMappings = Lists.mutable.empty();
        phraseMappings = Lists.mutable.empty();
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
        INounMapping nounMapping = new NounMapping(words, kind, claimant, probability, words.castToList(), occurrences);
        addNounMappingOrAppendToSimilarNounMapping(nounMapping, claimant);

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
    public void addPhraseMapping(ImmutableList<IPhrase> phrases, ImmutableList<INounMapping> nounMappings, IClaimant claimant, double probability) {
        IPhraseMapping phraseMapping = new PhraseMapping(phrases, nounMappings, claimant, probability);
        addPhraseMappingOrAppendToSimilarPhraseMapping(phraseMapping, claimant);
    }

    private void addPhraseMappingOrAppendToSimilarPhraseMapping(IPhraseMapping phraseMapping, IClaimant claimant) {
        // TODO: check for similarity!
        phraseMappings.add(phraseMapping);
    }

    @Override
    public final ImmutableList<INounMapping> getNounMappings() {
        return nounMappings.toImmutable();
    }

    @Override
    public ImmutableList<IPhraseMapping> getPhraseMappings() {
        return phraseMappings.toImmutable();
    }

    /**
     * Returns all type mappings.
     * 
     * @param kind searched mappingKind
     * @return all type mappings as list
     */
    @Override
    public final ImmutableList<INounMapping> getNounMappingsOfKind(MappingKind kind) {
        return nounMappings.select(nounMappingIsOfKind(kind)).toImmutable();
    }

    /**
     * Returns all mappings containing the given node.
     *
     * @param word the given word
     * @return all mappings containing the given node as list
     */
    @Override
    public final ImmutableList<INounMapping> getNounMappingsByWord(IWord word) {
        return nounMappings.select(nMapping -> nMapping.getWords().contains(word)).toImmutable();
    }

    @Override
    public ImmutableList<IPhraseMapping> getPhraseMappingsByNounMapping(INounMapping nounMapping) {

        return phraseMappings.select(p -> p.getNounMappings().contains(nounMapping)).toImmutable();
    }

    @Override
    public ImmutableList<IPhraseMapping> getPhraseMappingsByPhrase(IPhrase phrase) {
        return phraseMappings.select(p -> p.equals(phrase)).toImmutable();
    }

    /**
     * Returns all mappings with a similar reference as given.
     *
     * @param ref the reference to search for
     * @return a list of noun mappings with the given reference.
     */
    @Override
    public final ImmutableList<INounMapping> getNounMappingsWithSimilarReference(String ref) {
        return nounMappings.select(nm -> SimilarityUtils.areWordsSimilar(ref, nm.getReference())).toImmutable();
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
    public final ImmutableList<INounMapping> getNounMappingsByWordAndKind(IWord word, MappingKind kind) {
        return nounMappings.select(n -> n.getWords().contains(word)).select(nounMappingIsOfKind(kind)).toImmutable();
    }

    @Override
    public ImmutableList<IPhraseMapping> getPhraseMappingsByPhraseType(PhraseType phraseType) {
        return this.phraseMappings.select(p -> p.getPhraseType().equals(phraseType)).toImmutable();
    }

    /**
     * Returns if a node is contained by the mappings.
     *
     * @param word node to check
     * @return true if the node is contained by mappings.
     */
    @Override
    public final boolean isWordContainedByNounMappings(IWord word) {
        return nounMappings.anySatisfy(n -> n.getWords().contains(word));
    }

    @Override
    public boolean isNounMappingContainedByPhrase(INounMapping nounMapping) {
        if (this.getPhraseMappingsByNounMapping(nounMapping).size() > 0) {
            return true;
        }
        return false;
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
        return nounMappings.select(n -> n.getWords().contains(word)).anySatisfy(nounMappingIsOfKind(mappingKind));
    }

    private Predicate<? super INounMapping> nounMappingIsOfKind(MappingKind mappingKind) {
        return n -> n.getKind() == mappingKind;
    }

    @Override
    public ITextState createCopy() {
        var textExtractionState = new TextState(this.configs);
        textExtractionState.nounMappings = Lists.mutable.ofAll(nounMappings);
        return textExtractionState;
    }

    @Override
    public void addNounMapping(INounMapping nounMapping, IClaimant claimant) {
        addNounMappingOrAppendToSimilarNounMapping(nounMapping, claimant);
    }

    private INounMapping addNounMappingOrAppendToSimilarNounMapping(INounMapping nounMapping, IClaimant claimant) {
        for (var existingNounMapping : nounMappings) {
            if (SimilarityUtils.areNounMappingsSimilar(nounMapping, existingNounMapping)) {
                appendNounMappingToExistingNounMapping(nounMapping, existingNounMapping, claimant);
                return existingNounMapping;
            }
        }
        nounMappings.add(nounMapping);
        return nounMapping;
    }

    private void appendNounMappingToExistingNounMapping(INounMapping disposableNounMapping, INounMapping existingNounMapping, IClaimant claimant) {
        existingNounMapping.addKindWithProbability(disposableNounMapping.getKind(), claimant, disposableNounMapping.getProbability());
        existingNounMapping.addOccurrence(disposableNounMapping.getSurfaceForms());
        existingNounMapping.addWord(disposableNounMapping.getReferenceWords().get(0));
    }

    @Override
    public void removeNounMapping(INounMapping n) {
        nounMappings.remove(n);
    }

    @Override
    public void removePhraseMapping(IPhraseMapping phraseMapping) {
        this.phraseMappings.remove(phraseMapping);
    }

    @Override
    public String toString() {
        return "TextExtractionState [nounMappings=" + String.join("\n", nounMappings.toString()) + ", relationNodes=" + "]";
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // handle additional configuration
    }
}
