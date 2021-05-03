package edu.kit.kastel.mcse.ardoco.core.datastructures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.common.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRelationMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITermMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.MappingKind;

public abstract class AbstractTextState implements ITextState {

    protected List<IRelationMapping> relationMappings;
    protected List<ITermMapping> terms;

    protected abstract void addNounMapping(IWord n, String name, MappingKind kind, double probability, List<String> occurrences);

    protected abstract void addTerm(String reference, List<INounMapping> of, MappingKind kind, double probability);

    protected abstract Collection<? extends INounMapping> getReadableNounMappings();

    /***
     * Adds a name mapping to the state
     *
     * @param n           node of the mapping
     * @param name        reference of the mapping
     * @param probability probability to be a name mapping
     * @param occurrences list of the appearances of the mapping
     */
    @Override
    public final void addName(IWord n, String name, double probability, List<String> occurrences) {
        addNounMapping(n, name.toLowerCase(), MappingKind.NAME, probability, occurrences);
    }

    /***
     * Adds a name mapping to the state
     *
     * @param word        word of the mapping
     * @param name        reference of the mapping
     * @param probability probability to be a name mapping
     */
    @Override
    public final void addName(IWord word, String name, double probability) {
        addName(word, name.toLowerCase(), probability, List.of(word.getText()));
    }

    /***
     * Adds a name or type mapping to the state
     *
     * @param n           node of the mapping
     * @param ref         reference of the mapping
     * @param probability probability to be a name or type mapping
     */
    @Override
    public final void addNort(IWord n, String ref, double probability) {
        addNort(n, ref.toLowerCase(), probability, List.of(n.getText()));
    }

    /***
     * Adds a name or type mapping to the state
     *
     * @param n           node of the mapping
     * @param ref         reference of the mapping
     * @param probability probability to be a name or type mapping
     * @param occurrences list of the appearances of the mapping
     */
    @Override
    public void addNort(IWord n, String ref, double probability, List<String> occurrences) {
        addNounMapping(n, ref.toLowerCase(), MappingKind.NAME_OR_TYPE, probability, occurrences);
    }

    /***
     * Adds a type mapping to the state
     *
     * @param n           node of the mapping
     * @param type        reference of the mapping
     * @param probability probability to be a type mapping
     */
    @Override
    public final void addType(IWord n, String type, double probability) {
        addType(n, type.toLowerCase(), probability, List.of(n.getText()));
    }

    /***
     * Adds a type mapping to the state
     *
     * @param n           node of the mapping
     * @param type        reference of the mapping
     * @param probability probability to be a type mapping
     * @param occurrences list of the appearances of the mapping
     */
    @Override
    public final void addType(IWord n, String type, double probability, List<String> occurrences) {
        addNounMapping(n, type.toLowerCase(), MappingKind.TYPE, probability, occurrences);
    }

    /**
     * Adds a relation mapping to the state.
     *
     * @param n the relation mapping to add.
     */
    @Override
    public final void addRelation(IRelationMapping n) {
        if (!relationMappings.contains(n)) {
            relationMappings.add(n);
        }
    }

    /**
     * Adds a term to the state.
     *
     * @param reference   the reference of the term
     * @param mapping1    the first mapping of the term
     * @param mapping2    the second mapping of the term
     * @param kind        the kind of the term
     * @param probability the probability that this term is from that kind
     */
    @Override
    public final void addTerm(String reference, INounMapping mapping1, INounMapping mapping2, MappingKind kind, double probability) {
        addTerm(reference, List.of(mapping1, mapping2), kind, probability);
    }

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
    @Override
    public final void addTerm(String reference, INounMapping mapping1, INounMapping mapping2, List<INounMapping> otherMappings, MappingKind kind,
            double probability) {

        List<INounMapping> mappings = new ArrayList<>();
        mappings.add(mapping1);
        mappings.add(mapping2);
        mappings.addAll(otherMappings);
        addTerm(reference, mappings, kind, probability);
    }

    /**
     * Adds a term as a name to the state.
     *
     * @param reference   the reference of the term
     * @param mapping1    the first mapping of the term
     * @param mapping2    the second mapping of the term
     * @param probability the probability that this term is a name
     */
    @Override
    public final void addNameTerm(String reference, INounMapping mapping1, INounMapping mapping2, double probability) {
        addTerm(reference, List.of(mapping1, mapping2), MappingKind.NAME, probability);
    }

    /**
     * Adds a term as a type to the state.
     *
     * @param reference   the reference of the term
     * @param mapping1    the first mapping of the term
     * @param mapping2    the second mapping of the term
     * @param probability the probability that this term is a type
     */
    @Override
    public final void addTypeTerm(String reference, INounMapping mapping1, INounMapping mapping2, double probability) {
        addTerm(reference, List.of(mapping1, mapping2), MappingKind.TYPE, probability);
    }

    /**
     * Removes a relation mapping from the state.
     *
     * @param n relation mapping to remove
     */
    @Override
    public final void removeRelation(IRelationMapping n) {
        relationMappings.remove(n);
    }

    /**
     * Removes the given term from the state.
     *
     * @param term the term to remove.
     */
    @Override
    public final void removeTerm(ITermMapping term) {
        terms.remove(term);
    }

    @Override
    public final List<INounMapping> getAllMappings() {
        return new ArrayList<>(getReadableNounMappings());
    }

    /**
     * Getter for the terms of this state.
     *
     * @return the list of found terms
     */
    @Override
    public final List<ITermMapping> getTerms() {
        return new ArrayList<>(terms);
    }

    /**
     * Returns all type mappings.
     *
     * @return all type mappings as list
     */
    @Override
    public final List<INounMapping> getTypes() {
        return getReadableNounMappings().stream().filter(n -> n.getKind().equals(MappingKind.TYPE)).collect(Collectors.toList());
    }

    /**
     * Returns all type term mappings.
     *
     * @return all type term mappings as list
     */
    @Override
    public final List<ITermMapping> getTypeTerms() {
        return terms.stream().filter(n -> n.getKind().equals(MappingKind.TYPE)).collect(Collectors.toList());
    }

    /**
     * Returns all mappings containing the given node.
     *
     * @param n the given node
     * @return all mappings containing the given node as list
     */
    @Override
    public final List<INounMapping> getNounMappingsByNode(IWord n) {
        return getReadableNounMappings().stream().filter(nMapping -> nMapping.getWords().contains(n)).collect(Collectors.toList());
    }

    /**
     * Returns all mappings with the exact same reference as given.
     *
     * @param ref the reference to search for
     * @return a list of noun mappings with the given reference.
     */
    @Override
    public final List<INounMapping> getNounMappingsWithSameReference(String ref) {
        return getReadableNounMappings().stream().filter(nMapping -> nMapping.getReference().equalsIgnoreCase(ref)).collect(Collectors.toList());
    }

    /**
     * Returns a list of all references of name mappings.
     *
     * @return all references of name mappings as list.
     */
    @Override
    public final List<String> getNameList() {

        Set<String> names = new HashSet<>();
        List<INounMapping> nameMappings = getNames();
        for (INounMapping nnm : nameMappings) {
            names.add(nnm.getReference());
        }
        return new ArrayList<>(names);
    }

    /**
     * Returns a list of all references of name term mappings.
     *
     * @return all references of name term mappings as list.
     */
    @Override
    public final List<String> getNameTermList() {
        Set<String> names = new HashSet<>();
        List<ITermMapping> nameMappings = getNameTerms();
        for (ITermMapping nnm : nameMappings) {
            names.add(nnm.getReference());
        }
        return new ArrayList<>(names);
    }

    /**
     * Returns a list of all references of name or type mappings.
     *
     * @return all references of name or type mappings as list.
     */
    @Override
    public final List<String> getNortList() {
        Set<String> norts = new HashSet<>();
        List<INounMapping> nortMappings = getNameOrTypeMappings();
        for (INounMapping nnm : nortMappings) {
            norts.add(nnm.getReference());
        }
        return new ArrayList<>(norts);
    }

    /**
     * Returns a list of all references of type mappings.
     *
     * @return all references of type mappings as list.
     */
    @Override
    public final List<String> getTypeList() {

        Set<String> types = new HashSet<>();
        List<INounMapping> typeMappings = getTypes();
        for (INounMapping nnm : typeMappings) {
            types.add(nnm.getReference());
        }
        return new ArrayList<>(types);
    }

    /**
     * Getter for the terms of this state, that have exactly the same nounMappings.
     *
     * @param nounMappings the nounMappings to search for
     * @return a list of terms with that nounMappings
     */
    @Override
    public final List<ITermMapping> getTermsByMappings(List<INounMapping> nounMappings) {
        return terms.stream()
                .filter(//
                        t -> t.getMappings().containsAll(nounMappings) && nounMappings.containsAll(t.getMappings()))
                .//
                collect(Collectors.toList());
    }

    @Override
    public final List<ITermMapping> getTermsBySimilarReference(String reference) {
        return terms.stream().filter(t -> SimilarityUtils.areWordsSimilar(reference, t.getReference())).collect(Collectors.toList());
    }

    @Override
    public final List<ITermMapping> getTermsByMappingsAndKind(List<INounMapping> nounMappings, MappingKind kind) {
        List<ITermMapping> termsByMapping = getTermsByMappings(nounMappings);
        return termsByMapping.stream().filter(t -> t.getKind().equals(kind)).collect(Collectors.toList());
    }

    /**
     * Returns a list of all references of type term mappings.
     *
     * @return all references of type term mappings as list.
     */
    @Override
    public final List<String> getTypeTermList() {

        Set<String> types = new HashSet<>();
        List<ITermMapping> typeMappings = getTypeTerms();
        for (ITermMapping nnm : typeMappings) {
            types.add(nnm.getReference());
        }
        return new ArrayList<>(types);
    }

    /**
     * Returns all name mappings
     *
     * @return a list of all name mappings
     */
    @Override
    public final List<INounMapping> getNames() {
        return getReadableNounMappings().stream().filter(n -> n.getKind().equals(MappingKind.NAME)).collect(Collectors.toList());
    }

    /**
     * Returns all name term mappings
     *
     * @return a list of all name term mappings
     */
    @Override
    public final List<ITermMapping> getNameTerms() {
        return terms.stream().filter(n -> n.getKind().equals(MappingKind.NAME)).collect(Collectors.toList());
    }

    /**
     * Returns all name or type mappings
     *
     * @return a list of all name or type mappings
     */
    @Override
    public final List<INounMapping> getNameOrTypeMappings() {
        return getReadableNounMappings().stream().filter(n -> n.getKind().equals(MappingKind.NAME_OR_TYPE)).collect(Collectors.toList());
    }

    /**
     * Returns alltype mappings containing the given node.
     *
     * @param node node to filter for
     * @return a list of alltype mappings containing the given node
     */
    @Override
    public final List<INounMapping> getTypeNodesByNode(IWord node) {
        return getReadableNounMappings().stream()
                .filter(n -> n.getWords().contains(node))
                .filter(n -> n.getKind() == MappingKind.TYPE)
                .collect(Collectors.toList());
    }

    /**
     * Returns all name mappings containing the given node.
     *
     * @param node node to filter for
     * @return a list of all name mappings containing the given node
     */
    @Override
    public final List<INounMapping> getNameNodesByNode(IWord node) {
        return getReadableNounMappings().stream()
                .filter(n -> n.getWords().contains(node))
                .filter(n -> n.getKind() == MappingKind.NAME)
                .collect(Collectors.toList());
    }

    /**
     * Returns all name or type mappings containing the given node.
     *
     * @param node node to filter for
     * @return a list of all name or type mappings containing the given node
     */
    @Override
    public final List<INounMapping> getNortNodesByNode(IWord node) {
        return getReadableNounMappings().stream()
                .filter(n -> n.getWords().contains(node))
                .filter(n -> n.getKind() == MappingKind.NAME_OR_TYPE)
                .collect(Collectors.toList());
    }

    /**
     * Returns all relation mappings.
     *
     * @return relation mappings as list
     */
    @Override
    public final List<IRelationMapping> getRelations() {
        return new ArrayList<>(relationMappings);
    }

    /**
     * Returns all term mappings that contain the given noun mapping.
     *
     * @param nounMapping the noun mapping that should be contained.
     * @return all term mappings that contain the noun mapping.
     */
    @Override
    public final List<ITermMapping> getTermsByContainedMapping(INounMapping nounMapping) {

        List<ITermMapping> filteredTerms = new ArrayList<>();

        for (ITermMapping term : terms) {
            if (term.getMappings().contains(nounMapping)) {
                filteredTerms.add(term);
            }
        }
        return filteredTerms;
    }

    /**
     * Returns if a node is contained by the name or type mappings.
     *
     * @param node node to check
     * @return true if the node is contained by name or type mappings.
     */
    @Override
    public final boolean isNodeContainedByNameOrTypeNodes(IWord node) {
        return !getReadableNounMappings().stream()
                .filter(n -> n.getKind().equals(MappingKind.NAME_OR_TYPE))
                .filter(n -> n.getWords().contains(node))
                .findAny()
                .isEmpty();
    }

    /**
     * Returns if a node is contained by the name mappings.
     *
     * @param node node to check
     * @return true if the node is contained by name mappings.
     */
    @Override
    public final boolean isNodeContainedByNameNodes(IWord node) {
        return !getReadableNounMappings().stream()
                .filter(n -> n.getKind().equals(MappingKind.NAME))
                .filter(n -> n.getWords().contains(node))
                .findAny()
                .isEmpty();
    }

    /**
     * Returns if a node is contained by the term mappings.
     *
     * @param node node to check
     * @return true if the node is contained by term mappings.
     */
    @Override
    public final boolean isNodeContainedByTermMappings(IWord node) {

        for (ITermMapping term : terms) {
            if (term.getMappings().stream().anyMatch(n -> n.getWords().contains(node))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns all term mappings that contain noun mappings containing the given node.
     *
     * @param node the node to search for
     * @return a list of term mappings that contain that node.
     */
    @Override
    public final List<ITermMapping> getTermMappingsByNode(IWord node) {

        return terms.stream()
                .filter(//
                        term -> term.getMappings().stream().anyMatch(n -> n.getWords().contains(node)))
                .collect(Collectors.toList());

    }

    /**
     * Returns if a node is contained by the mappings.
     *
     * @param node node to check
     * @return true if the node is contained by mappings.
     */
    @Override
    public final boolean isNodeContainedByNounMappings(IWord node) {
        return !getReadableNounMappings().stream().filter(n -> n.getWords().contains(node)).findAny().isEmpty();
    }

    /**
     * Returns if a node is contained by the type mappings.
     *
     * @param node node to check
     * @return true if the node is contained by type mappings.
     */
    @Override
    public final boolean isNodeContainedByTypeNodes(IWord node) {
        return !getReadableNounMappings().stream()
                .filter(n -> n.getKind().equals(MappingKind.TYPE))
                .filter(n -> n.getWords().contains(node))
                .findAny()
                .isEmpty();
    }

}
