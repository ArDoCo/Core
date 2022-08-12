/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

/**
 * The Class TextState defines the basic implementation of a {@link TextState}.
 *
 */
public class TextStateImpl extends AbstractState implements TextState {

    private transient MutableList<NounMapping> nounMappings;

    /**
     * Creates a new name type relation state
     */
    public TextStateImpl() {
        nounMappings = Lists.mutable.empty();
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
    public final void addNounMapping(Word word, MappingKind kind, Claimant claimant, double probability, ImmutableList<String> occurrences) {
        var words = Lists.immutable.with(word);
        NounMapping nounMapping = new NounMappingImpl(words, kind, claimant, probability, words.castToList(), occurrences);
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
    public final void addNounMapping(Word word, MappingKind kind, Claimant claimant, double probability) {
        addNounMapping(word, kind, claimant, probability, Lists.immutable.with(word.getText()));
    }

    /***
     * Adds a type mapping to the state
     *
     * @param probability probability to be a type mapping
     * @param occurrences list of the appearances of the mapping
     * @param word        node of the mapping
     */
    @Override
    public final void addNounMapping(Word word, Claimant claimant, double probability, ImmutableList<String> occurrences) {
        addNounMapping(word, MappingKind.TYPE, claimant, probability, occurrences);
    }

    @Override
    public final ImmutableList<NounMapping> getNounMappings() {
        return nounMappings.toImmutable();
    }

    /**
     * Returns all type mappings.
     * 
     * @param kind searched mappingKind
     * @return all type mappings as list
     */
    @Override
    public final ImmutableList<NounMapping> getNounMappingsOfKind(MappingKind kind) {
        return nounMappings.select(nounMappingIsOfKind(kind)).toImmutable();
    }

    /**
     * Returns all mappings containing the given node.
     *
     * @param word the given word
     * @return all mappings containing the given node as list
     */
    @Override
    public final ImmutableList<NounMapping> getNounMappingsByWord(Word word) {
        return nounMappings.select(nMapping -> nMapping.getWords().contains(word)).toImmutable();
    }

    /**
     * Returns all mappings with a similar reference as given.
     *
     * @param ref the reference to search for
     * @return a list of noun mappings with the given reference.
     */
    @Override
    public final ImmutableList<NounMapping> getNounMappingsWithSimilarReference(String ref) {
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
        for (NounMapping nnm : kindMappings) {
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
    public final ImmutableList<NounMapping> getNounMappingsByWordAndKind(Word word, MappingKind kind) {
        return nounMappings.select(n -> n.getWords().contains(word)).select(nounMappingIsOfKind(kind)).toImmutable();
    }

    /**
     * Returns if a node is contained by the name mappings.
     *
     * @param word        node to check
     * @param mappingKind mappingKind to check for
     * @return true if the node is contained by name mappings.
     */
    @Override
    public final boolean isWordContainedByMappingKind(Word word, MappingKind mappingKind) {
        return nounMappings.select(n -> n.getWords().contains(word)).anySatisfy(nounMappingIsOfKind(mappingKind));
    }

    private Predicate<? super NounMapping> nounMappingIsOfKind(MappingKind mappingKind) {
        return n -> n.getKind() == mappingKind;
    }

    @Override
    public TextState createCopy() {
        var textExtractionState = new TextStateImpl();
        textExtractionState.nounMappings = Lists.mutable.ofAll(nounMappings);
        return textExtractionState;
    }

    @Override
    public void addNounMapping(NounMapping nounMapping, Claimant claimant) {
        addNounMappingOrAppendToSimilarNounMapping(nounMapping, claimant);
    }

    private NounMapping addNounMappingOrAppendToSimilarNounMapping(NounMapping nounMapping, Claimant claimant) {
        for (var existingNounMapping : nounMappings) {
            if (SimilarityUtils.areNounMappingsSimilar(nounMapping, existingNounMapping)) {
                appendNounMappingToExistingNounMapping(nounMapping, existingNounMapping, claimant);
                return existingNounMapping;
            }
        }
        nounMappings.add(nounMapping);
        return nounMapping;
    }

    private void appendNounMappingToExistingNounMapping(NounMapping disposableNounMapping, NounMapping existingNounMapping, Claimant claimant) {
        existingNounMapping.addKindWithProbability(disposableNounMapping.getKind(), claimant, disposableNounMapping.getProbability());
        existingNounMapping.addOccurrence(disposableNounMapping.getSurfaceForms());
        existingNounMapping.addWord(disposableNounMapping.getReferenceWords().get(0));
    }

    @Override
    public void removeNounMapping(NounMapping n) {
        nounMappings.remove(n);
    }

    @Override
    public String toString() {
        return "TextExtractionState [nounMappings=" + String.join("\n", nounMappings.toString()) + ", relationNodes=" + "]";
    }

}
