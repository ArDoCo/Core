package edu.kit.kastel.mcse.ardoco.core.datastructures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.common.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRelationMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITermMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.MappingKind;

/**
 * The Class TextState defines the basic implementation of a {@link ITextState}.
 */
public class TextState implements ITextState {
    private double similarityPercentage;

    private Map<String, NounMapping> nounMappings;

    /** The relation mappings. */
    private List<IRelationMapping> relationMappings;

    /** The terms. */
    private List<ITermMapping> terms;

    /**
     * Creates a new name type relation state.
     *
     * @param similarityPercentage the similarity percentage
     */
    public TextState(double similarityPercentage) {
        nounMappings = new HashMap<>();
        relationMappings = new ArrayList<>();
        terms = new ArrayList<>();
        this.similarityPercentage = similarityPercentage;
    }

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
    public final List<INounMapping> getNounMappings() {
        return new ArrayList<>(nounMappings.values());
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
        return nounMappings.values().stream().filter(n -> MappingKind.TYPE.equals(n.getKind())).collect(Collectors.toList());
    }

    /**
     * Returns all type term mappings.
     *
     * @return all type term mappings as list
     */
    @Override
    public final List<ITermMapping> getTypeTerms() {
        return terms.stream().filter(n -> MappingKind.TYPE.equals(n.getKind())).collect(Collectors.toList());
    }

    /**
     * Returns all mappings containing the given node.
     *
     * @param n the given node
     * @return all mappings containing the given node as list
     */
    @Override
    public final List<INounMapping> getNounMappingsByNode(IWord n) {
        return nounMappings.values().stream().filter(nMapping -> nMapping.getWords().contains(n)).collect(Collectors.toList());
    }

    /**
     * Returns all mappings with the exact same reference as given.
     *
     * @param ref the reference to search for
     * @return a list of noun mappings with the given reference.
     */
    @Override
    public final List<INounMapping> getNounMappingsWithSameReference(String ref) {
        return nounMappings.values().stream().filter(nMapping -> nMapping.getReference().equalsIgnoreCase(ref)).collect(Collectors.toList());
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
        return nounMappings.values().stream().filter(n -> MappingKind.NAME.equals(n.getKind())).collect(Collectors.toList());
    }

    /**
     * Returns all name term mappings
     *
     * @return a list of all name term mappings
     */
    @Override
    public final List<ITermMapping> getNameTerms() {
        return terms.stream().filter(n -> MappingKind.NAME.equals(n.getKind())).collect(Collectors.toList());
    }

    /**
     * Returns all name or type mappings
     *
     * @return a list of all name or type mappings
     */
    @Override
    public final List<INounMapping> getNameOrTypeMappings() {
        return nounMappings.values().stream().filter(n -> MappingKind.NAME_OR_TYPE.equals(n.getKind())).collect(Collectors.toList());
    }

    /**
     * Returns alltype mappings containing the given node.
     *
     * @param node node to filter for
     * @return a list of alltype mappings containing the given node
     */
    @Override
    public final List<INounMapping> getTypeNodesByNode(IWord node) {
        return nounMappings.values()
                .stream()
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
        return nounMappings.values()
                .stream()
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
        return nounMappings.values()
                .stream()
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
        return !nounMappings.values()
                .stream()
                .filter(n -> MappingKind.NAME_OR_TYPE.equals(n.getKind()))
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
        return !nounMappings.values().stream().filter(n -> MappingKind.NAME.equals(n.getKind())).filter(n -> n.getWords().contains(node)).findAny().isEmpty();
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
        return !nounMappings.values().stream().filter(n -> n.getWords().contains(node)).findAny().isEmpty();
    }

    /**
     * Returns if a node is contained by the type mappings.
     *
     * @param node node to check
     * @return true if the node is contained by type mappings.
     */
    @Override
    public final boolean isNodeContainedByTypeNodes(IWord node) {
        return !nounMappings.values().stream().filter(n -> MappingKind.TYPE.equals(n.getKind())).filter(n -> n.getWords().contains(node)).findAny().isEmpty();
    }

    @Override
    public ITextState createCopy() {
        var textExtractionState = new TextState(similarityPercentage);
        textExtractionState.nounMappings = new HashMap<>(nounMappings);
        textExtractionState.relationMappings = relationMappings.stream().map(IRelationMapping::createCopy).collect(Collectors.toList());
        textExtractionState.terms = terms.stream().map(ITermMapping::createCopy).collect(Collectors.toList());
        return textExtractionState;
    }

    @Override
    public void addNounMapping(List<IWord> nodes, String reference, MappingKind kind, double confidence, List<String> occurrences) {
        var mapping = new NounMapping(nodes, Map.of(kind, confidence), reference, occurrences);
        nounMappings.put(mapping.getReference(), mapping);
    }

    private void addNounMapping(IWord word, String reference, MappingKind kind, double probability, List<String> occurrences) {
        if (SimilarityUtils.containsSeparator(reference)) {
            List<String> parts = SimilarityUtils.splitAtSeparators(reference).stream().filter(part -> part.length() > 1).collect(Collectors.toList());
            for (String referencePart : parts) {
                addNounMapping(word, referencePart, kind, probability, occurrences);
            }
            return;

        }

        if (nounMappings.containsKey(reference)) {
            // extend existing nounMapping
            NounMapping existingMapping = nounMappings.get(reference);
            existingMapping.addKindWithProbability(kind, probability);
            existingMapping.addOccurrence(occurrences);
            existingMapping.addNode(word);

        } else {

            List<String> similarRefs = nounMappings.keySet()
                    .stream()
                    .filter(ref -> SimilarityUtils.areWordsSimilar(ref, reference, similarityPercentage))
                    .collect(Collectors.toList());

            for (String ref : similarRefs) {
                NounMapping similarMapping = nounMappings.get(ref);
                similarMapping.addOccurrence(occurrences);
                similarMapping.addNode(word);
                similarMapping.addKindWithProbability(kind, probability);
            }
            if (similarRefs.isEmpty()) {
                // create new nounMapping
                var mapping = new NounMapping(List.of(word), kind, probability, reference, occurrences);
                nounMappings.put(reference, mapping);
            }
        }

    }

    /**
     * Creates a new term if the term is not yet included by the state, and adds it it. If terms with the same mappings
     * and of the same kind can be found their probability is updated.
     *
     * @param reference   the reference of the term
     * @param mappings    mappings of the term
     * @param kind        the kind of the term
     * @param probability the probability that this term is from that kind
     */
    private void addTerm(String reference, List<INounMapping> mappings, MappingKind kind, double probability) {

        List<ITermMapping> includedTerms = getTermsByMappingsAndKind(mappings, kind);

        if (!includedTerms.isEmpty()) {
            for (ITermMapping includedTerm : includedTerms) {
                includedTerm.updateProbability(probability);
            }
        } else {
            ITermMapping term;
            if (mappings.size() <= 2) {
                term = new TermMapping(reference, mappings.get(0), mappings.get(1), List.of(), kind, probability);

            } else {
                term = new TermMapping(reference, mappings.get(0), mappings.get(1), mappings.subList(2, mappings.size() - 1), kind, probability);
            }
            terms.add(term);
        }
    }

    @Override
    public void addNort(IWord n, String ref, double probability, List<String> occurrences) {
        addNounMapping(n, ref.toLowerCase(), MappingKind.NAME_OR_TYPE, probability, occurrences);

        List<NounMapping> wordsWithSimilarNode = nounMappings.values().stream().filter(mapping -> mapping.getWords().contains(n)).collect(Collectors.toList());
        for (NounMapping mapping : wordsWithSimilarNode) {
            if (mapping.getProbabilityForName() == 0) {
                mapping.addKindWithProbability(MappingKind.NAME, TextExtractionStateConfig.NORT_PROBABILITY_FOR_NAME_AND_TYPE);
            }
            if (mapping.getProbabilityForType() == 0) {
                mapping.addKindWithProbability(MappingKind.TYPE, TextExtractionStateConfig.NORT_PROBABILITY_FOR_NAME_AND_TYPE);
            }
        }
    }

    @Override
    public IRelationMapping addRelation(INounMapping node1, INounMapping node2, double probability) {
        IRelationMapping relationMapping = new RelationMapping(node1, node2, probability);
        if (!relationMappings.contains(relationMapping)) {
            relationMappings.add(relationMapping);
        }
        return relationMapping;
    }

    @Override
    public void removeNounNode(INounMapping n) {
        nounMappings.remove(n.getReference());
    }

    @Override
    public String toString() {
        return "TextExtractionState [nounMappings=" + String.join("\n", nounMappings.toString()) + ", relationNodes="
                + String.join("\n", relationMappings.toString()) + "]";
    }

}
