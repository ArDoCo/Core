package edu.kit.kastel.mcse.ardoco.core.datastructures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.common.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRelationMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITermMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.MappingKind;

public class TextStateWithoutClustering extends AbstractTextState implements ITextState {
    private List<INounMapping> nounMappings;

    @Override
    protected Collection<? extends INounMapping> getReadableNounMappings() {
        return nounMappings;
    }

    @Override
    public ITextState createCopy() {
        TextStateWithoutClustering textExtractionState = new TextStateWithoutClustering();
        textExtractionState.nounMappings = nounMappings.stream().map(INounMapping::createCopy).collect(Collectors.toList());
        textExtractionState.relationMappings = relationMappings.stream().map(IRelationMapping::createCopy).collect(Collectors.toList());
        textExtractionState.terms = terms.stream().map(ITermMapping::createCopy).collect(Collectors.toList());
        return textExtractionState;
    }

    /**
     * Creates a new name type relation state
     */
    public TextStateWithoutClustering() {
        nounMappings = new ArrayList<>();
        relationMappings = new ArrayList<>();
        terms = new ArrayList<>();
    }

    @Override
    public void addNounMapping(List<IWord> nodes, String reference, MappingKind kind, double confidence, List<String> occurrences) {
        INounMapping mapping = new NounMappingWithoutDistribution(nodes, confidence, kind, reference, occurrences);
        nounMappings.add(mapping);
    }

    // --- add section --->
    @Override
    protected void addNounMapping(IWord node, String reference, MappingKind kind, double probability, List<String> occurrences) {

        if (SimilarityUtils.containsSeparator(reference)) {
            addNounMappingWithSeparator(node, reference, probability, kind);
        }

        List<INounMapping> nounMappingsWithNode = getNounMappingsByNode(node);
        List<INounMapping> nounMappingsWithRef = nounMappings.stream().filter(nm -> nm.getReference().equalsIgnoreCase(reference)).collect(Collectors.toList());

        if (nounMappingsWithNode.isEmpty() && nounMappingsWithRef.isEmpty()) {
            INounMapping createdMapping = NounMappingFactory.createMappingTypeNode(node, reference.toLowerCase(), kind, probability, occurrences);
            nounMappings.add(createdMapping);

        } else if (nounMappingsWithNode.isEmpty() && nounMappingsWithRef.size() == 1) {
            // sollte hier nicht passieren!
            INounMapping nounMapping = nounMappingsWithRef.get(0);
            nounMapping.addNode(node);
            updateMapping(nounMapping, kind, probability, occurrences);

        } else {
            for (INounMapping nounMapping : nounMappingsWithNode) {
                updateMapping(nounMapping, kind, probability, occurrences);
            }
        }

    }

    private void hardAdd(IWord n, String ref, List<String> occurrences, double probability, MappingKind kind) {

        double hardAddProbability = TextExtractionStateConfig.HARD_ADD_PROBABILITY * probability;
        List<INounMapping> nounMappingsWithNode = getNounMappingsByNode(n);

        if (nounMappingsWithNode.isEmpty()) {
            createMappingTypeDependentMapping(kind, n, hardAddProbability, ref, occurrences);
        } else {

            for (INounMapping nounMapping : nounMappingsWithNode) {

                if (occurrences.stream().anyMatch(SimilarityUtils::containsSeparator) && !nounMapping.getReference().equalsIgnoreCase(ref)) {

                    List<INounMapping> nounMappingsWithRef = nounMappings.stream()
                            .filter(nm -> nm.getReference().equalsIgnoreCase(ref))
                            .collect(Collectors.toList());

                    addOrUpdate(nounMappingsWithRef, n, ref, occurrences, hardAddProbability, kind);

                } else {
                    extendMappingBy(nounMapping, kind, n, probability, occurrences);
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
            List<String> occEquals = nnm.getOccurrences().stream().filter(occ -> occ.equalsIgnoreCase(ref)).collect(Collectors.toList());
            occEquals.stream().forEach(occ -> nnm.copyOccurrencesAndNodesTo(occ, createdMapping));
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

        if (!nounMapping.getWords().contains(n)) {
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
            createdMapping = NounMappingFactory.createNameMapping(n, probability, ref.toLowerCase(), occurrences);
            nounMappings.add(createdMapping);
        } else if (kind.equals(MappingKind.TYPE)) {
            createdMapping = NounMappingFactory.createTypeMapping(n, probability, ref.toLowerCase(), occurrences);
            nounMappings.add(createdMapping);
        } else {
            createdMapping = NounMappingFactory.createNortMapping(n, probability, ref.toLowerCase(), occurrences);
            nounMappings.add(createdMapping);
        }
        return createdMapping;
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

    /**
     * Creates a new term if the term is not yet included by the state, and adds it it. If terms with the same mappings
     * and of the same kind can be found their probability is updated.
     *
     * @param reference   the reference of the term
     * @param mappings    mappings of the term
     * @param kind        the kind of the term
     * @param probability the probability that this term is from that kind
     */
    @Override
    protected void addTerm(String reference, List<INounMapping> mappings, MappingKind kind, double probability) {

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
     * Removes a noun mapping from the state.
     *
     * @param n noun mapping to remove
     */
    @Override
    public void removeNounNode(INounMapping n) {
        nounMappings.remove(n);
    }

    // --- get section --->

    /**
     * Prints the name type relation state: The noun mappings as well as the relation mappings.
     */
    @Override
    public String toString() {
        return "TextExtractionState [nounMappings=" + String.join("\n", nounMappings.toString()) + ", relationNodes="
                + String.join("\n", relationMappings.toString()) + "]";
    }

}
