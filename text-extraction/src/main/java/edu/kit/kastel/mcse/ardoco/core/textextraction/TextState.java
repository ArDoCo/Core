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
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.IRelationMapping;
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

    /**
     * The relation mappings.
     */
    private MutableList<IRelationMapping> relationMappings;

    /**
     * Creates a new name type relation state
     *
     * @param configs any additional configuration
     */
    public TextState(Map<String, String> configs) {
        super(configs);
        nounMappings = Lists.mutable.empty();
        relationMappings = Lists.mutable.empty();
    }

    /***
     * Adds a name mapping to the state
     *
     * @param probability probability to be a name mapping
     * @param occurrences list of the appearances of the mapping
     * @param word        node of the mapping
     */
    @Override
    public final void addName(IWord word, IClaimant claimant, double probability, ImmutableList<String> occurrences) {
        addNounMapping(word, MappingKind.NAME, claimant, probability, occurrences);
    }

    /***
     * Adds a name mapping to the state
     *
     * @param word        word of the mapping
     * @param probability probability to be a name mapping
     */
    @Override
    public final void addName(IWord word, IClaimant claimant, double probability) {
        addName(word, claimant, probability, Lists.immutable.with(word.getText()));
    }

    /***
     * Adds a type mapping to the state
     *
     * @param probability probability to be a type mapping
     * @param word        node of the mapping
     */
    @Override
    public final void addType(IWord word, IClaimant claimant, double probability) {
        addType(word, claimant, probability, Lists.immutable.with(word.getText()));
    }

    /***
     * Adds a type mapping to the state
     *
     * @param probability probability to be a type mapping
     * @param occurrences list of the appearances of the mapping
     * @param word        node of the mapping
     */
    @Override
    public final void addType(IWord word, IClaimant claimant, double probability, ImmutableList<String> occurrences) {
        addNounMapping(word, MappingKind.TYPE, claimant, probability, occurrences);
    }

    @Override
    public final ImmutableList<INounMapping> getNounMappings() {
        return nounMappings.toImmutable();
    }

    /**
     * Returns all type mappings.
     *
     * @return all type mappings as list
     */
    @Override
    public final ImmutableList<INounMapping> getTypes() {
        return nounMappings.select(nounMappingIsType()).toImmutable();
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
     * Returns a list of all references of name mappings.
     *
     * @return all references of name mappings as list.
     */
    @Override
    public final ImmutableList<String> getNameList() {

        Set<String> names = new HashSet<>();
        var nameMappings = getNames();
        for (INounMapping nnm : nameMappings) {
            names.add(nnm.getReference());
        }
        return Lists.immutable.withAll(names);
    }

    /**
     * Returns a list of all references of type mappings.
     *
     * @return all references of type mappings as list.
     */
    @Override
    public final ImmutableList<String> getTypeList() {
        Set<String> types = new HashSet<>();
        var typeMappings = getTypes();
        for (INounMapping nnm : typeMappings) {
            types.add(nnm.getReference());
        }
        return Lists.immutable.withAll(types);
    }

    /**
     * Returns all name mappings
     *
     * @return a list of all name mappings
     */
    @Override
    public final ImmutableList<INounMapping> getNames() {
        return nounMappings.select(n -> MappingKind.NAME == n.getKind()).toImmutable();
    }

    /**
     * Returns all type mappings containing the given node.
     *
     * @param word word to filter for
     * @return a list of alltype mappings containing the given node
     */
    @Override
    public final ImmutableList<INounMapping> getTypeMappingsByWord(IWord word) {
        return nounMappings.select(n -> n.getWords().contains(word)).select(nounMappingIsType()).toImmutable();
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

    /**
     * Returns if a node is contained by the name mappings.
     *
     * @param word node to check
     * @return true if the node is contained by name mappings.
     */
    @Override
    public final boolean isWordContainedByNameMapping(IWord word) {
        return nounMappings.select(n -> n.getWords().contains(word)).anySatisfy(nounMappingIsName());
    }

    private Predicate<? super INounMapping> nounMappingIsName() {
        return n -> n.getKind() == MappingKind.NAME;
    }

    /**
     * Returns if a node is contained by the type mappings.
     *
     * @param word node to check
     * @return true if the node is contained by type mappings.
     */
    @Override
    public final boolean isWordContainedByTypeMapping(IWord word) {
        return nounMappings.select(n -> n.getWords().contains(word)).anySatisfy(nounMappingIsType());
    }

    private Predicate<? super INounMapping> nounMappingIsType() {
        return n -> n.getKind() == MappingKind.TYPE;
    }

    @Override
    public final boolean isWordContainedByNameOrTypeMapping(IWord word) {
        return nounMappings.select(n -> n.getWords().contains(word)).anySatisfy(n -> {
            var nameProb = n.getProbabilityForName();
            var typeProb = n.getProbabilityForType();
            return nameProb > 0 && typeProb > 0 && Math.abs(nameProb - typeProb) < NAME_OR_TYPE_MAX_DIFF;
        });
    }

    @Override
    public ITextState createCopy() {
        var textExtractionState = new TextState(this.configs);
        textExtractionState.nounMappings = Lists.mutable.ofAll(nounMappings);
        textExtractionState.relationMappings = relationMappings.collect(IRelationMapping::createCopy);
        return textExtractionState;
    }

    @Override
    public void addNounMapping(INounMapping nounMapping, IClaimant claimant) {
        addNounMappingOrAppendToSimilarNounMapping(nounMapping, claimant);
    }

    private INounMapping addNounMapping(IWord word, MappingKind kind, IClaimant claimant, double probability, ImmutableList<String> occurrences) {
        var words = Lists.immutable.with(word);
        INounMapping nounMapping = new NounMapping(words, kind, claimant, probability, words.castToList(), occurrences);

        return addNounMappingOrAppendToSimilarNounMapping(nounMapping, claimant);
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
    public IRelationMapping addRelation(INounMapping node1, INounMapping node2, IClaimant claimant, double probability) {
        IRelationMapping relationMapping = new RelationMapping(node1, node2, probability);
        if (!relationMappings.contains(relationMapping)) {
            relationMappings.add(relationMapping);
        }
        return relationMapping;
    }

    @Override
    public void removeNounMapping(INounMapping n) {
        nounMappings.remove(n);
    }

    @Override
    public String toString() {
        return "TextExtractionState [nounMappings=" + String.join("\n", nounMappings.toString()) + ", relationNodes="
                + String.join("\n", relationMappings.toString()) + "]";
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // handle additional configuration
    }
}
