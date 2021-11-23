package edu.kit.kastel.mcse.ardoco.core.textextraction;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.common.IState;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;

/**
 * The Interface ITextState.
 */
public interface ITextState extends IState<ITextState> {

    /**
     * * Adds a name mapping to the state.
     *
     * @param n           node of the mapping
     * @param name        reference of the mapping
     * @param probability probability to be a name mapping
     * @param occurrences list of the appearances of the mapping
     */
    void addName(IWord n, String name, double probability, ImmutableList<String> occurrences);

    /**
     * * Adds a name mapping to the state.
     *
     * @param word        word of the mapping
     * @param name        reference of the mapping
     * @param probability probability to be a name mapping
     */
    void addName(IWord word, String name, double probability);

    /**
     * * Adds a name or type mapping to the state.
     *
     * @param n           node of the mapping
     * @param ref         reference of the mapping
     * @param probability probability to be a name or type mapping
     */
    void addNort(IWord n, String ref, double probability);

    /**
     * * Adds a name or type mapping to the state.
     *
     * @param n           node of the mapping
     * @param ref         reference of the mapping
     * @param probability probability to be a name or type mapping
     * @param occurrences list of the appearances of the mapping
     */
    void addNort(IWord n, String ref, double probability, ImmutableList<String> occurrences);

    /**
     * * Adds a type mapping to the state.
     *
     * @param n           node of the mapping
     * @param type        reference of the mapping
     * @param probability probability to be a type mapping
     */
    void addType(IWord n, String type, double probability);

    /**
     * * Adds a type mapping to the state.
     *
     * @param n           node of the mapping
     * @param type        reference of the mapping
     * @param probability probability to be a type mapping
     * @param occurrences list of the appearances of the mapping
     */
    void addType(IWord n, String type, double probability, ImmutableList<String> occurrences);

    /**
     * Creates a new relation mapping and adds it to the state. More end points, as well as a preposition can be added
     * afterwards.
     *
     * @param node1       first relation end point
     * @param node2       second relation end point
     * @param probability probability of being a relation
     * @return the added relation mapping
     */
    IRelationMapping addRelation(INounMapping node1, INounMapping node2, double probability);

    /**
     * Adds a relation mapping to the state.
     *
     * @param n the relation mapping to add.
     */
    void addRelation(IRelationMapping n);

    // --- remove section --->
    /**
     * Removes a noun mapping from the state.
     *
     * @param n noun mapping to remove
     */
    void removeNounMapping(INounMapping n);

    /**
     * Removes a relation mapping from the state.
     *
     * @param n relation mapping to remove
     */
    void removeRelation(IRelationMapping n);

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
     * Returns all mappings with the exact same reference as given.
     *
     * @param ref the reference to search for
     * @return a list of noun mappings with the given reference.
     */
    ImmutableList<INounMapping> getNounMappingsWithEqualReference(String ref);

    /**
     * Returns a list of all references of name mappings.
     *
     * @return all references of name mappings as list.
     */
    ImmutableList<String> getNameList();

    /**
     * Returns a list of all references of name or type mappings.
     *
     * @return all references of name or type mappings as list.
     */
    ImmutableList<String> getNortList();

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
     * Returns all name or type mappings.
     *
     * @return a list of all name or type mappings
     */
    ImmutableList<INounMapping> getNameOrTypeMappings();

    /**
     * Returns alltype mappings containing the given node.
     *
     * @param word node to filter for
     * @return a list of alltype mappings containing the given node
     */
    ImmutableList<INounMapping> getTypeMappingsByWord(IWord word);

    /**
     * Returns all name mappings containing the given node.
     *
     * @param node node to filter for
     * @return a list of all name mappings containing the given node
     */
    ImmutableList<INounMapping> getNameMappingsByWord(IWord node);

    /**
     * Returns all name or type mappings containing the given node.
     *
     * @param node node to filter for
     * @return a list of all name or type mappings containing the given node
     */
    ImmutableList<INounMapping> getNortMappingsByWord(IWord node);

    /**
     * Returns all relation mappings.
     *
     * @return relation mappings as list
     */
    ImmutableList<IRelationMapping> getRelations();

    // --- isContained section --->
    /**
     * Returns if a node is contained by the name or type mappings.
     *
     * @param node node to check
     * @return true if the node is contained by name or type mappings.
     */
    boolean isWordContainedByNameOrTypeMapping(IWord node);

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
    void addNounMapping(INounMapping nounMapping);

    /**
     * Adds the noun mapping.
     *
     * @param nodes       the nodes
     * @param reference   the reference
     * @param kind        the kind
     * @param confidence  the confidence
     * @param occurrences the occurrences
     */
    void addNounMapping(ImmutableList<IWord> nodes, String reference, MappingKind kind, double confidence, ImmutableList<String> occurrences);

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
     * Gets the mappings that could be A nort.
     *
     * @param word the word
     * @return the mappings that could be A nort
     */
    default ImmutableList<INounMapping> getMappingsThatCouldBeANort(IWord word) {
        return getNounMappingsByWord(word).select(mapping -> mapping.getProbabilityForNort() > 0);
    }

    /**
     * Returns all mappings with a similar reference as given.
     *
     * @param ref the reference to search for
     * @return a list of noun mappings with the given reference.
     */
    ImmutableList<INounMapping> getNounMappingsWithSimilarReference(String ref);
}
