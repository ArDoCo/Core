package edu.kit.ipd.consistency_analyzer.datastructures;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.ipd.consistency_analyzer.common.SimilarityUtils;

/**
 * The text extraction state holds instance, relation mappings and terms extracted from the textual information.
 *
 * @author Sophie
 *
 */
public class TextExtractionState implements ITextState {

    private List<INounMapping> nounMappings;
    private List<IRelationMapping> relationMappings;
    private List<ITermMapping> terms;

    @Override
    public ITextState createCopy() {
        TextExtractionState textExtractionState = new TextExtractionState();
        textExtractionState.nounMappings = nounMappings.stream().map(INounMapping::createCopy).collect(Collectors.toList());
        textExtractionState.relationMappings = relationMappings.stream().map(IRelationMapping::createCopy).collect(Collectors.toList());
        textExtractionState.terms = terms.stream().map(ITermMapping::createCopy).collect(Collectors.toList());
        return textExtractionState;
    }

    /**
     * Creates a new name type relation state
     */
    public TextExtractionState() {
        nounMappings = new ArrayList<>();
        relationMappings = new ArrayList<>();
        terms = new ArrayList<>();
    }

    // --- add section --->

    /**
     * Adds a node with its ref to the state. If the node and the reference is not already contained by the mappings the
     * mapping is created. If then similar occurrences are contained the matching entries are copied to the new mapping.
     * If the node is not contained, but the reference, the matching mapping is updated. If the node is already
     * contained but its mapping doesn't contains a separated input it is extended. This possibly changes the kind. If
     * the node is already contained and the mappings contain its occurrences are checked for similarities. If they are
     * similar to the new occurrences the found mapping is updated. Elsewhere dependent on the occurrence of the
     * reference a new is created or an existing one is extended.
     *
     * Only use for occurrences with separator in it!
     *
     * @param n           node to add to the mappings
     * @param ref         reference to add to the mappings
     * @param occurrences occurrences to add to the mappings
     * @param kind        kind of the mapping
     */
    private void hardAdd(IWord n, String ref, List<String> occurrences, double probability, MappingKind kind) {

        double hardAddProbability = TextExtractionStateConfig.HARD_ADD_PROBABILITY * probability;
        List<INounMapping> nounMappingsWithNode = getNounMappingsByNode(n);

        if (nounMappingsWithNode.isEmpty()) {
            List<INounMapping> nounMappingsWithRef = SimilarityUtils.getMostLikelyNMappingsByReference(ref, nounMappings);

            addOrUpdate(nounMappingsWithRef, n, ref, occurrences, hardAddProbability, kind);
        } else {

            for (INounMapping nounMapping : nounMappingsWithNode) {

                if (occurrences.stream().anyMatch(SimilarityUtils::containsSeparator) && !SimilarityUtils.areWordsSimilar(nounMapping.getReference(), ref)) {

                    List<INounMapping> nounMappingsWithRef = SimilarityUtils.getMostLikelyNMappingsByReference(ref, nounMappings);

                    addOrUpdate(nounMappingsWithRef, n, ref, occurrences, hardAddProbability, kind);

                } else {
                    updateKindProbOccOfMapping(nounMapping, occurrences, kind, probability);
                }
            }
        }
    }

    private void addOrUpdate(List<INounMapping> nounMappingsWithRef, IWord n, String ref, List<String> occurrences, double hardAddProbability,
            MappingKind kind) {
        if (nounMappingsWithRef.isEmpty()) {

            List<INounMapping> nounMappingsWithOcc = nounMappings.stream()
                    .filter(//
                            nsn -> SimilarityUtils.areWordsOfListsSimilar(nsn.getRepresentativeComparables(), occurrences))
                    .collect(Collectors.toList());

            INounMapping createdMapping = createMappingTypeDependentMapping(kind, n, hardAddProbability, ref, occurrences);

            extendNounMappingsWithOccurrences(nounMappingsWithOcc, ref, createdMapping);

        } else {

            for (INounMapping nm : nounMappingsWithRef) {

                extendMappingBy(nm, kind, n, hardAddProbability, occurrences);
            }

        }
    }

    /**
     * Searches for noun mappings with similar occurrences as the given ref. If some are found their occurrences and
     * nodes are copied to the given mapping.
     *
     * @param nounMappingsWithOcc
     * @param ref
     * @param createdMapping
     */
    private void extendNounMappingsWithOccurrences(List<INounMapping> nounMappingsWithOcc, String ref, INounMapping createdMapping) {

        for (INounMapping nnm : nounMappingsWithOcc) {
            List<String> occSimilar = nnm.getOccurrences().stream().filter(occ -> SimilarityUtils.areWordsSimilar(ref, occ)).collect(Collectors.toList());
            occSimilar.stream().forEach(occ -> nnm.copyOccurrencesAndNodesTo(occ, createdMapping));
        }
    }

    /**
     * If the given kind is not a nort, the type of the noun mapping is changed to the given kind if the probability is
     * high enough. The occurrences are extended and the probability is set in any case.
     *
     * @param nounMapping the mapping to update
     * @param occurrences the occurrences to update with
     * @param kind        the kind of the updating mapping
     * @param probability the probability of the updating mapping
     */
    private void updateKindProbOccOfMapping(INounMapping nounMapping, List<String> occurrences, MappingKind kind, Double probability) {
        if (kind != null && probability != null && !kind.equals(MappingKind.NAME_OR_TYPE)) {
            updateKindOfNounMapping(nounMapping, kind, probability);
        }
        nounMapping.addOccurrence(occurrences);
        if (probability != null) {
            nounMapping.hardSetProbability(probability);
        }
    }

    private void updateKindOfNounMapping(INounMapping nounMapping, MappingKind kind, double probability) {
        MappingKind preKind = nounMapping.getKind();
        if (preKind == kind) {
            nounMapping.changeMappingType(kind, probability);
            return;
        }

        List<ITermMapping> termsWithMapping = terms.stream()
                .filter(//
                        t -> t.getMappings().contains(nounMapping))
                .collect(Collectors.toList());

        nounMapping.changeMappingType(kind, probability);

        for (ITermMapping termWithMapping : termsWithMapping) {
            if (!termWithMapping.getKind().equals(kind) && hasAnyTermMatchingKind(kind, termWithMapping)) {
                removeTerm(termWithMapping);
            }
        }

    }

    private boolean hasAnyTermMatchingKind(MappingKind kind, ITermMapping termWithMapping) {
        return termWithMapping.getMappings()
                .stream()
                .anyMatch(mapping -> !mapping.getKind().equals(kind) && !mapping.getKind().equals(MappingKind.NAME_OR_TYPE));
    }

    /**
     * Extends the given mapping by the node if it is not already contained. After that, the kind, probability, and
     * occurrences are updated.
     *
     * @param nounMapping the noun mapping to update
     * @param mt          the kind to update with
     * @param n           the node to update with
     * @param probability the probability to update with
     * @param occurrences the occurrences to update with
     */
    private void extendMappingBy(INounMapping nounMapping, MappingKind mt, IWord n, Double probability, List<String> occurrences) {

        if (!nounMapping.getNodes().contains(n)) {
            nounMapping.addNode(n);
        }

        updateKindProbOccOfMapping(nounMapping, occurrences, mt, probability);

    }

    /**
     * Dependent on the kind a new node is created and added to the mappings of the state.
     *
     * @param kind        the given kind
     * @param n           the given node
     * @param probability the given probability
     * @param ref         the reference of the mapping
     * @param occurrences the occurrences for the mapping
     * @return the created noun mapping
     */
    private INounMapping createMappingTypeDependentMapping(MappingKind kind, IWord n, double probability, String ref, List<String> occurrences) {
        INounMapping createdMapping;
        if (kind.equals(MappingKind.NAME)) {
            createdMapping = NounMappingFactory.createNameMapping(n, probability, ref, occurrences);
            nounMappings.add(createdMapping);
        } else if (kind.equals(MappingKind.TYPE)) {
            createdMapping = NounMappingFactory.createTypeMapping(n, probability, ref, occurrences);
            nounMappings.add(createdMapping);
        } else {
            createdMapping = NounMappingFactory.createNortMapping(n, probability, ref, occurrences);
            nounMappings.add(createdMapping);
        }
        return createdMapping;
    }

    /**
     * Adds a mapping of a certain kind to the mappings of this state. If the reference contains a separator it is
     * removed and the method {@link #hardAdd(IWord, String, List, MappingKind)} is called and returned. If neither the
     * node nor the reference is contained in the mappings a new mapping is created. Elsewhere if a mapping with the
     * node can be found it is updated. If the reference can be found the mapping with it is updated. The method
     * {@link #updateMapping(NounMapping, MappingKind, double, List)} is used for updating the mappings.
     *
     *
     * @param n           node to add
     * @param reference   reference to add
     * @param kind        kind to add
     * @param probability probability for kind
     * @param occurrences appearances of the mapping to add
     */
    private void addNounMapping(IWord node, String reference, MappingKind kind, double probability, List<String> occurrences) {

        if (SimilarityUtils.containsSeparator(reference)) {
            addNounMappingWithSeparator(node, reference, probability, kind);
        }

        List<INounMapping> nounMappingsWithNode = getNounMappingsByNode(node);
        List<INounMapping> nounMappingsWithRef = SimilarityUtils.getMostLikelyNMappingsByReference(reference, nounMappings);

        if (nounMappingsWithNode.isEmpty() && nounMappingsWithRef.isEmpty()) {
            INounMapping createdMapping = NounMappingFactory.createMappingTypeNode(node, reference, kind, probability, occurrences);
            nounMappings.add(createdMapping);

        } else if (nounMappingsWithNode.isEmpty()) {
            if (nounMappingsWithRef.size() == 1) {
                INounMapping nounMapping = nounMappingsWithRef.get(0);
                nounMapping.addNode(node);
                updateMapping(nounMapping, kind, probability, occurrences);
            }
        } else {
            for (INounMapping nounMapping : nounMappingsWithNode) {
                updateMapping(nounMapping, kind, probability, occurrences);
            }
        }

    }

    private void addNounMappingWithSeparator(IWord n, String reference, double probability, MappingKind kind) {

        List<String> parts = SimilarityUtils.splitAtSeparators(reference);
        parts = parts.stream().filter(part -> part.length() > 1).collect(Collectors.toList());
        for (String part : parts) {
            hardAdd(n, part, List.of(reference), probability, kind);
        }
    }

    /**
     * The update of a mapping depends on its kind. Name or type mappings can be changed always if something more
     * specific is proposed. For updating name or types the probability has to be greater than the current probability.
     * In every case the mapping is extended by the occurrences and the probability is updated.
     *
     * @param nnm         the existing mapping
     * @param kind        the proposed kind
     * @param probability the probability for the kind
     * @param occurrences the occurrences to add
     */
    private void updateMapping(INounMapping nnm, MappingKind kind, double probability, List<String> occurrences) {
        if (kind.equals(MappingKind.NAME_OR_TYPE)) {
            nnm.addOccurrence(occurrences);
            nnm.updateProbability(probability);

        } else {
            boolean existingMappingIsNortAndNewProbabilityIsHigher = nnm.getKind().equals(MappingKind.NAME_OR_TYPE) || probability >= nnm.getProbability();
            if (kind.equals(MappingKind.TYPE) && existingMappingIsNortAndNewProbabilityIsHigher) {
                nnm.changeMappingType(MappingKind.TYPE, probability);
            } else if (kind.equals(MappingKind.NAME) && existingMappingIsNortAndNewProbabilityIsHigher) {
                nnm.changeMappingType(MappingKind.NAME, probability);
            }
        }

        nnm.addOccurrence(occurrences);
        nnm.updateProbability(probability);
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
    public void addName(IWord n, String name, double probability, List<String> occurrences) {
        addNounMapping(n, name, MappingKind.NAME, probability, occurrences);
    }

    /***
     * Adds a name mapping to the state
     *
     * @param word        word of the mapping
     * @param name        reference of the mapping
     * @param probability probability to be a name mapping
     */
    @Override
    public void addName(IWord word, String name, double probability) {
        addName(word, name, probability, List.of(word.getText()));
    }

    /***
     * Adds a name or type mapping to the state
     *
     * @param n           node of the mapping
     * @param ref         reference of the mapping
     * @param probability probability to be a name or type mapping
     */
    @Override
    public void addNort(IWord n, String ref, double probability) {
        addNort(n, ref, probability, List.of(n.getText()));
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
        addNounMapping(n, ref, MappingKind.NAME_OR_TYPE, probability, occurrences);
    }

    /***
     * Adds a type mapping to the state
     *
     * @param n           node of the mapping
     * @param type        reference of the mapping
     * @param probability probability to be a type mapping
     */
    @Override
    public void addType(IWord n, String type, double probability) {
        addType(n, type, probability, List.of(n.getText()));
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
    public void addType(IWord n, String type, double probability, List<String> occurrences) {
        addNounMapping(n, type, MappingKind.TYPE, probability, occurrences);
    }

    /**
     * Creates a new relation mapping and adds it to the state. More end points, as well as a preposition can be added
     * afterwards.
     *
     * @param node1       first relation end point
     * @param node2       second relation end point
     * @param probability probability of being a relation
     * @return the added relation mapping
     */
    @Override
    public IRelationMapping addRelation(INounMapping node1, INounMapping node2, double probability) {
        IRelationMapping relationMapping = new RelationMapping(node1, node2, probability);
        if (!relationMappings.contains(relationMapping)) {
            relationMappings.add(relationMapping);
        }
        return relationMapping;
    }

    /**
     * Adds a relation mapping to the state.
     *
     * @param n the relation mapping to add.
     */
    @Override
    public void addRelation(IRelationMapping n) {
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
    public void addTerm(String reference, INounMapping mapping1, INounMapping mapping2, MappingKind kind, double probability) {
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
    public void addTerm(String reference, INounMapping mapping1, INounMapping mapping2, List<INounMapping> otherMappings, MappingKind kind,
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
    public void addNameTerm(String reference, INounMapping mapping1, INounMapping mapping2, double probability) {
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
    public void addTypeTerm(String reference, INounMapping mapping1, INounMapping mapping2, double probability) {
        addTerm(reference, List.of(mapping1, mapping2), MappingKind.TYPE, probability);
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

    // --- remove section --->
    /**
     * Removes a noun mapping from the state.
     *
     * @param n noun mapping to remove
     */
    @Override
    public void removeNounNode(INounMapping n) {
        nounMappings.remove(n);
    }

    /**
     * Removes a relation mapping from the state.
     *
     * @param n relation mapping to remove
     */
    @Override
    public void removeRelation(IRelationMapping n) {
        relationMappings.remove(n);
    }

    /**
     * Removes the given term from the state.
     *
     * @param term the term to remove.
     */
    @Override
    public void removeTerm(ITermMapping term) {
        terms.remove(term);
    }

    // --- get section --->

    /**
     * Getter for the terms of this state.
     *
     * @return the list of found terms
     */
    @Override
    public List<ITermMapping> getTerms() {
        return terms;
    }

    /**
     * Getter for the terms of this state, that have exactly the same nounMappings.
     *
     * @param nounMappings the nounMappings to search for
     * @return a list of terms with that nounMappings
     */
    @Override
    public List<ITermMapping> getTermsByMappings(List<INounMapping> nounMappings) {
        return terms.stream()
                .filter(//
                        t -> t.getMappings().containsAll(nounMappings) && nounMappings.containsAll(t.getMappings()))
                .//
                collect(Collectors.toList());
    }

    /**
     * Getter for the terms of this state, that have a similar reference.
     *
     * @param reference the given reference
     * @return a list of terms with a reference that is similar to the given
     */
    @Override
    public List<ITermMapping> getTermsBySimilarReference(String reference) {
        return terms.stream().filter(t -> SimilarityUtils.areWordsSimilar(reference, t.getReference())).collect(Collectors.toList());
    }

    /**
     * Getter for the terms of this state, that have exactly the same nounMappings and the same kind.
     *
     * @param nounMappings the nounMappings to search for
     * @param kind         the kind of the term mappings to search for
     * @return a list of terms with that nounMappings and the same kind
     */
    @Override
    public List<ITermMapping> getTermsByMappingsAndKind(List<INounMapping> nounMappings, MappingKind kind) {
        List<ITermMapping> termsByMapping = getTermsByMappings(nounMappings);
        return termsByMapping.stream().filter(t -> t.getKind().equals(kind)).collect(Collectors.toList());
    }

    /**
     * Returns all type mappings.
     *
     * @return all type mappings as list
     */
    @Override
    public List<INounMapping> getTypes() {
        return nounMappings.stream().filter(n -> n.getKind().equals(MappingKind.TYPE)).collect(Collectors.toList());
    }

    /**
     * Returns all type term mappings.
     *
     * @return all type term mappings as list
     */
    @Override
    public List<ITermMapping> getTypeTerms() {
        return terms.stream().filter(n -> n.getKind().equals(MappingKind.TYPE)).collect(Collectors.toList());
    }

    /**
     * Returns all mappings containing the given node.
     *
     * @param n the given node
     * @return all mappings containing the given node as list
     */
    @Override
    public List<INounMapping> getNounMappingsByNode(IWord n) {
        return nounMappings.stream().filter(nMapping -> nMapping.getNodes().contains(n)).collect(Collectors.toList());
    }

    /**
     * Returns all mappings with the exact same reference as given.
     *
     * @param ref the reference to search for
     * @return a list of noun mappings with the given reference.
     */
    @Override
    public List<INounMapping> getNounMappingsWithSameReference(String ref) {
        return nounMappings.stream().filter(nMapping -> nMapping.getReference().contentEquals(ref)).collect(Collectors.toList());
    }

    /**
     * Returns a list of all references of name mappings.
     *
     * @return all references of name mappings as list.
     */
    @Override
    public List<String> getNameList() {

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
    public List<String> getNameTermList() {
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
    public List<String> getNortList() {
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
    public List<String> getTypeList() {

        Set<String> types = new HashSet<>();
        List<INounMapping> typeMappings = getTypes();
        for (INounMapping nnm : typeMappings) {
            types.add(nnm.getReference());
        }
        return new ArrayList<>(types);
    }

    /**
     * Returns a list of all references of type term mappings.
     *
     * @return all references of type term mappings as list.
     */
    @Override
    public List<String> getTypeTermList() {

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
    public List<INounMapping> getNames() {
        return nounMappings.stream().filter(n -> n.getKind().equals(MappingKind.NAME)).collect(Collectors.toList());
    }

    /**
     * Returns all name term mappings
     *
     * @return a list of all name term mappings
     */
    @Override
    public List<ITermMapping> getNameTerms() {
        return terms.stream().filter(n -> n.getKind().equals(MappingKind.NAME)).collect(Collectors.toList());
    }

    /**
     * Returns all name or type mappings
     *
     * @return a list of all name or type mappings
     */
    @Override
    public List<INounMapping> getNameOrTypeMappings() {
        return nounMappings.stream().filter(n -> n.getKind().equals(MappingKind.NAME_OR_TYPE)).collect(Collectors.toList());
    }

    /**
     * Returns alltype mappings containing the given node.
     *
     * @param node node to filter for
     * @return a list of alltype mappings containing the given node
     */
    @Override
    public List<INounMapping> getTypeNodesByNode(IWord node) {
        return nounMappings.stream().filter(n -> n.getNodes().contains(node)).filter(n -> n.getKind() == MappingKind.TYPE).collect(Collectors.toList());
    }

    /**
     * Returns all name mappings containing the given node.
     *
     * @param node node to filter for
     * @return a list of all name mappings containing the given node
     */
    @Override
    public List<INounMapping> getNameNodesByNode(IWord node) {
        return nounMappings.stream().filter(n -> n.getNodes().contains(node)).filter(n -> n.getKind() == MappingKind.NAME).collect(Collectors.toList());
    }

    /**
     * Returns all name or type mappings containing the given node.
     *
     * @param node node to filter for
     * @return a list of all name or type mappings containing the given node
     */
    @Override
    public List<INounMapping> getNortNodesByNode(IWord node) {
        return nounMappings.stream().filter(n -> n.getNodes().contains(node)).filter(n -> n.getKind() == MappingKind.NAME_OR_TYPE).collect(Collectors.toList());
    }

    /**
     * Returns all relation mappings.
     *
     * @return relation mappings as list
     */
    @Override
    public List<IRelationMapping> getRelations() {
        return relationMappings;
    }

    /**
     * Returns all term mappings that contain the given noun mapping.
     *
     * @param nounMapping the noun mapping that should be contained.
     * @return all term mappings that contain the noun mapping.
     */
    @Override
    public List<ITermMapping> getTermsByContainedMapping(INounMapping nounMapping) {

        List<ITermMapping> filteredTerms = new ArrayList<>();

        for (ITermMapping term : terms) {
            if (term.getMappings().contains(nounMapping)) {
                filteredTerms.add(term);
            }
        }
        return filteredTerms;
    }

    // --- isContained section --->
    /**
     * Returns if a node is contained by the name or type mappings.
     *
     * @param node node to check
     * @return true if the node is contained by name or type mappings.
     */
    @Override
    public boolean isNodeContainedByNameOrTypeNodes(IWord node) {
        return !nounMappings.stream().filter(n -> n.getKind().equals(MappingKind.NAME_OR_TYPE)).filter(n -> n.getNodes().contains(node)).findAny().isEmpty();
    }

    /**
     * Returns if a node is contained by the name mappings.
     *
     * @param node node to check
     * @return true if the node is contained by name mappings.
     */
    @Override
    public boolean isNodeContainedByNameNodes(IWord node) {
        return !nounMappings.stream().filter(n -> n.getKind().equals(MappingKind.NAME)).filter(n -> n.getNodes().contains(node)).findAny().isEmpty();
    }

    /**
     * Returns if a node is contained by the term mappings.
     *
     * @param node node to check
     * @return true if the node is contained by term mappings.
     */
    @Override
    public boolean isNodeContainedByTermMappings(IWord node) {

        for (ITermMapping term : terms) {
            if (term.getMappings().stream().anyMatch(n -> n.getNodes().contains(node))) {
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
    public List<ITermMapping> getTermMappingsByNode(IWord node) {

        return terms.stream()
                .filter(//
                        term -> term.getMappings().stream().anyMatch(n -> n.getNodes().contains(node)))
                .collect(Collectors.toList());

    }

    /**
     * Returns if a node is contained by the mappings.
     *
     * @param node node to check
     * @return true if the node is contained by mappings.
     */
    @Override
    public boolean isNodeContainedByNounMappings(IWord node) {
        return !nounMappings.stream().filter(n -> n.getNodes().contains(node)).findAny().isEmpty();
    }

    /**
     * Returns if a node is contained by the type mappings.
     *
     * @param node node to check
     * @return true if the node is contained by type mappings.
     */
    @Override
    public boolean isNodeContainedByTypeNodes(IWord node) {
        return !nounMappings.stream().filter(n -> n.getKind().equals(MappingKind.TYPE)).filter(n -> n.getNodes().contains(node)).findAny().isEmpty();
    }

    @Override
    /**
     * Prints the name type relation state: The noun mappings as well as the relation mappings.
     */
    public String toString() {
        return "TextExtractionState [nounMappings=" + String.join("\n", nounMappings.toString()) + ", relationNodes="
                + String.join("\n", relationMappings.toString()) + "]";
    }

    @Override
    public List<INounMapping> getAllMappings() {
        return nounMappings;
    }

}
