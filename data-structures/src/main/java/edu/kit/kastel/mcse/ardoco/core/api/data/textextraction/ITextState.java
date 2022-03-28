/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.textextraction;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.agent.IClaimant;
import edu.kit.kastel.mcse.ardoco.core.api.common.IConfigurable;
import edu.kit.kastel.mcse.ardoco.core.api.common.ICopyable;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;

/**
 * The Interface ITextState.
 */
public interface ITextState extends ICopyable<ITextState>, IConfigurable {

    /**
     * * Adds a name mapping to the state.
     *
     * @param n           node of the mapping
     * @param probability probability to be a name mapping
     * @param occurrences list of the appearances of the mapping
     */
    void addName(IWord n, IClaimant claimant, double probability, ImmutableList<String> occurrences);

    /**
     * * Adds a name mapping to the state.
     *
     * @param word        word of the mapping
     * @param probability probability to be a name mapping
     */
    void addName(IWord word, IClaimant claimant, double probability);

    /**
     * * Adds a type mapping to the state.
     *
     * @param word        node of the mapping
     * @param probability probability to be a type mapping
     */
    void addType(IWord word, IClaimant claimant, double probability);

    /**
     * * Adds a type mapping to the state.
     *
     * @param word        node of the mapping
     * @param probability probability to be a type mapping
     * @param occurrences list of the appearances of the mapping
     */
    void addType(IWord word, IClaimant claimant, double probability, ImmutableList<String> occurrences);

    /**
     * Creates a new relation mapping and adds it to the state. More end points, as well as a preposition can be added
     * afterwards.
     *
     * @param node1       first relation end point
     * @param node2       second relation end point
     * @param probability probability of being a relation
     * @return the added relation mapping
     */
    IRelationMapping addRelation(INounMapping node1, INounMapping node2, IClaimant claimant, double probability);

    // --- remove section --->

    /**
     * Removes a noun mapping from the state.
     *
     * @param n noun mapping to remove
     */
    void removeNounMapping(INounMapping n);

    /**
     * Returns all type mappings.
     *
     * @return all type mappings as list
     */
    ImmutableList<INounMapping> getTypes();

    /**
     * Returns all mappings containing the given node.
     *
     * @param n the given node
     * @return all mappings containing the given node as list
     */
    ImmutableList<INounMapping> getNounMappingsByWord(IWord n);

    /**
     * Returns a list of all references of name mappings.
     *
     * @return all references of name mappings as list.
     */
    ImmutableList<String> getNameList();

    /**
     * Returns a list of all references of type mappings.
     *
     * @return all references of type mappings as list.
     */
    ImmutableList<String> getTypeList();

    /**
     * Returns all name mappings.
     *
     * @return a list of all name mappings
     */
    ImmutableList<INounMapping> getNames();

    /**
     * Returns alltype mappings containing the given node.
     *
     * @param word node to filter for
     * @return a list of alltype mappings containing the given node
     */
    ImmutableList<INounMapping> getTypeMappingsByWord(IWord word);

    /**
     * Returns if a node is contained by the name mappings.
     *
     * @param node node to check
     * @return true if the node is contained by name mappings.
     */
    boolean isWordContainedByNameMapping(IWord node);

    /**
     * Returns if a node is contained by the mappings.
     *
     * @param node node to check
     * @return true if the node is contained by mappings.
     */
    boolean isWordContainedByNounMappings(IWord node);

    /**
     * Returns if a node is contained by the type mappings.
     *
     * @param node node to check
     * @return true if the node is contained by type mappings.
     */
    boolean isWordContainedByTypeMapping(IWord node);

    /**
     * Returns if a node is contained by a mapping that can be both, a name or a type mapping.
     *
     * @param word word to check
     * @return true if the node is contained by name or type mappings.
     */
    boolean isWordContainedByNameOrTypeMapping(IWord word);

    /**
     * Gets the all noun mappings.
     *
     * @return the all mappings
     */
    ImmutableList<INounMapping> getNounMappings();

    /**
     * Adds the noun mapping.
     *
     * @param nounMapping the noun mapping.
     */
    void addNounMapping(INounMapping nounMapping, IClaimant claimant);

    /**
     * Gets the mappings that could be A type.
     *
     * @param word the word
     * @return the mappings that could be A type
     */
    default ImmutableList<INounMapping> getMappingsThatCouldBeAType(IWord word) {
        return getNounMappingsByWord(word).select(mapping -> mapping.getProbabilityForType() > 0);
    }

    /**
     * Gets the mappings that could be A name.
     *
     * @param word the word
     * @return the mappings that could be A name
     */
    default ImmutableList<INounMapping> getMappingsThatCouldBeAName(IWord word) {
        return getNounMappingsByWord(word).select(mapping -> mapping.getProbabilityForName() > 0);
    }

    /**
     * Returns all mappings with a similar reference as given.
     *
     * @param ref the reference to search for
     * @return a list of noun mappings with the given reference.
     */
    ImmutableList<INounMapping> getNounMappingsWithSimilarReference(String ref);

    /**
     * Gets the mappings that could be a Name or Type.
     *
     * @param word the word
     * @return the mappings that could be a Name or Type
     */
    default ImmutableList<INounMapping> getMappingsThatCouldBeNameOrType(IWord word) {
        return getNounMappingsByWord(word).select(mapping -> mapping.getProbabilityForName() > 0 && mapping.getProbabilityForType() > 0);
    }

}
