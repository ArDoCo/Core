package edu.kit.kastel.mcse.ardoco.core.datastructures.definitions;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.datastructures.modules.IState;

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

    /**
     * Adds a term to the state.
     *
     * @param reference   the reference of the term
     * @param mapping1    the first mapping of the term
     * @param mapping2    the second mapping of the term
     * @param kind        the kind of the term
     * @param probability the probability that this term is from that kind
     */
    void addTerm(String reference, INounMapping mapping1, INounMapping mapping2, MappingKind kind, double probability);

    /**
     * Adds a term to the state.
     *
     * @param reference     the reference of the term
     * @param mapping1      the first mapping of the term
     * @param mapping2      the second mapping of the term
     * @param otherMappings other mappings of the term
     * @param kind          the kind of the term
     * @param probability   the probability that this term is from that kind
     */
    void addTerm(String reference, INounMapping mapping1, INounMapping mapping2, ImmutableList<INounMapping> otherMappings, MappingKind kind,
            double probability);

    /**
     * Adds a term as a name to the state.
     *
     * @param reference   the reference of the term
     * @param mapping1    the first mapping of the term
     * @param mapping2    the second mapping of the term
     * @param probability the probability that this term is a name
     */
    void addNameTerm(String reference, INounMapping mapping1, INounMapping mapping2, double probability);

    /**
     * Adds a term as a type to the state.
     *
     * @param reference   the reference of the term
     * @param mapping1    the first mapping of the term
     * @param mapping2    the second mapping of the term
     * @param probability the probability that this term is a type
     */
    void addTypeTerm(String reference, INounMapping mapping1, INounMapping mapping2, double probability);

    // --- remove section --->
    /**
     * Removes a noun mapping from the state.
     *
     * @param n noun mapping to remove
     */
    void removeNounNode(INounMapping n);

    /**
     * Removes a relation mapping from the state.
     *
     * @param n relation mapping to remove
     */
    void removeRelation(IRelationMapping n);

    /**
     * Removes the given term from the state.
     *
     * @param term the term to remove.
     */
    void removeTerm(ITermMapping term);

    /**
     * Getter for the terms of this state.
     *
     * @return the list of found terms
     */
    ImmutableList<ITermMapping> getTerms();

    /**
     * Getter for the terms of this state, that have exactly the same nounMappings.
     *
     * @param nounMappings the nounMappings to search for
     * @return a list of terms with that nounMappings
     */
    ImmutableList<ITermMapping> getTermsByMappings(ImmutableList<INounMapping> nounMappings);

    /**
     * Getter for the terms of this state, that have a similar reference.
     *
     * @param reference the given reference
     * @return a list of terms with a reference that is similar to the given
     */
    ImmutableList<ITermMapping> getTermsBySimilarReference(String reference);

    /**
     * Getter for the terms of this state, that have exactly the same nounMappings and the same kind.
     *
     * @param nounMappings the nounMappings to search for
     * @param kind         the kind of the term mappings to search for
     * @return a list of terms with that nounMappings and the same kind
     */
    ImmutableList<ITermMapping> getTermsByMappingsAndKind(ImmutableList<INounMapping> nounMappings, MappingKind kind);

    /**
     * Returns all type mappings.
     *
     * @return all type mappings as list
     */
    ImmutableList<INounMapping> getTypes();

    /**
     * Returns all type term mappings.
     *
     * @return all type term mappings as list
     */
    ImmutableList<ITermMapping> getTypeTerms();

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
    ImmutableList<INounMapping> getNounMappingsWithSameReference(String ref);

    /**
     * Returns a list of all references of name mappings.
     *
     * @return all references of name mappings as list.
     */
    ImmutableList<String> getNameList();

    /**
     * Returns a list of all references of name term mappings.
     *
     * @return all references of name term mappings as list.
     */
    ImmutableList<String> getNameTermList();

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
     * Returns a list of all references of type term mappings.
     *
     * @return all references of type term mappings as list.
     */
    ImmutableList<String> getTypeTermList();

    /**
     * Returns all name mappings.
     *
     * @return a list of all name mappings
     */
    ImmutableList<INounMapping> getNames();

    /**
     * Returns all name term mappings.
     *
     * @return a list of all name term mappings
     */
    ImmutableList<ITermMapping> getNameTerms();

    /**
     * Returns all name or type mappings.
     *
     * @return a list of all name or type mappings
     */
    ImmutableList<INounMapping> getNameOrTypeMappings();

    /**
     * Returns alltype mappings containing the given node.
     *
     * @param node node to filter for
     * @return a list of alltype mappings containing the given node
     */
    ImmutableList<INounMapping> getTypeNodesByNode(IWord node);

    /**
     * Returns all name mappings containing the given node.
     *
     * @param node node to filter for
     * @return a list of all name mappings containing the given node
     */
    ImmutableList<INounMapping> getNameNodesByNode(IWord node);

    /**
     * Returns all name or type mappings containing the given node.
     *
     * @param node node to filter for
     * @return a list of all name or type mappings containing the given node
     */
    ImmutableList<INounMapping> getNortNodesByNode(IWord node);

    /**
     * Returns all relation mappings.
     *
     * @return relation mappings as list
     */
    ImmutableList<IRelationMapping> getRelations();

    /**
     * Returns all term mappings that contain the given noun mapping.
     *
     * @param nounMapping the noun mapping that should be contained.
     * @return all term mappings that contain the noun mapping.
     */
    ImmutableList<ITermMapping> getTermsByContainedMapping(INounMapping nounMapping);

    // --- isContained section --->
    /**
     * Returns if a node is contained by the name or type mappings.
     *
     * @param node node to check
     * @return true if the node is contained by name or type mappings.
     */
    boolean isNodeContainedByNameOrTypeNodes(IWord node);

    /**
     * Returns if a node is contained by the name mappings.
     *
     * @param node node to check
     * @return true if the node is contained by name mappings.
     */
    boolean isNodeContainedByNameNodes(IWord node);

    /**
     * Returns if a node is contained by the term mappings.
     *
     * @param node node to check
     * @return true if the node is contained by term mappings.
     */
    boolean isNodeContainedByTermMappings(IWord node);

    /**
     * Returns all term mappings that contain noun mappings containing the given node.
     *
     * @param node the node to search for
     * @return a list of term mappings that contain that node.
     */
    ImmutableList<ITermMapping> getTermMappingsByNode(IWord node);

    /**
     * Returns if a node is contained by the mappings.
     *
     * @param node node to check
     * @return true if the node is contained by mappings.
     */
    boolean isNodeContainedByNounMappings(IWord node);

    /**
     * Returns if a node is contained by the type mappings.
     *
     * @param node node to check
     * @return true if the node is contained by type mappings.
     */
    boolean isNodeContainedByTypeNodes(IWord node);

    /**
     * Gets the all noun mappings.
     *
     * @return the all mappings
     */
    ImmutableList<INounMapping> getNounMappings();

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

}
