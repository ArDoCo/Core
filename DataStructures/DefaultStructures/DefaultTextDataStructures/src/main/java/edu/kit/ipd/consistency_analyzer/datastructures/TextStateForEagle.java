package edu.kit.ipd.consistency_analyzer.datastructures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.ipd.consistency_analyzer.common.SimilarityUtils;

public class TextStateForEagle implements ITextStateWithDistributions {
    private Map<String, NounMappingForEagle> nounMappings;
    private List<IRelationMapping> relationMappings;
    private List<ITermMapping> terms;
    private double similarityPercentage;

    @Override
    public ITextState createCopy() {
        TextStateForEagle textExtractionState = new TextStateForEagle(similarityPercentage);
        textExtractionState.nounMappings = new HashMap<>(nounMappings);
        textExtractionState.relationMappings = relationMappings.stream().map(IRelationMapping::createCopy).collect(Collectors.toList());
        textExtractionState.terms = terms.stream().map(ITermMapping::createCopy).collect(Collectors.toList());
        return textExtractionState;
    }

    /**
     * Creates a new name type relation state
     */
    public TextStateForEagle(double similarityPercentage) {
        nounMappings = new HashMap<>();
        relationMappings = new ArrayList<>();
        terms = new ArrayList<>();
        this.similarityPercentage = similarityPercentage;
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
            NounMappingForEagle existingMapping = nounMappings.get(reference);
            existingMapping.addKindWithProbability(kind, probability);
            existingMapping.addOccurrence(occurrences);
            existingMapping.addNode(word);

        } else {

            List<String> similarRefs = nounMappings.keySet()
                    .stream()
                    .filter(ref -> SimilarityUtils.areWordsSimilar(ref, reference, similarityPercentage))
                    .collect(Collectors.toList());

            if (!similarRefs.isEmpty()) {
                if (similarRefs.size() == 1) {
                    NounMappingForEagle similarMapping = nounMappings.get(similarRefs.get(0));
                    similarMapping.addOccurrence(occurrences);
                    similarMapping.addNode(word);
                    similarMapping.addKindWithProbability(kind, probability);

                } else {
                    for (String ref : similarRefs) {
                        NounMappingForEagle similarMapping = nounMappings.get(ref);
                        similarMapping.addOccurrence(occurrences);
                        similarMapping.addNode(word);
                        similarMapping.addKindWithProbability(kind, probability);
                    }
                }
            } else {
                // create new nounMapping
                NounMappingForEagle mapping = new NounMappingForEagle(List.of(word), kind, probability, reference, occurrences);
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
    public void addName(IWord n, String name, double probability, List<String> occurrences) {
        addNounMapping(n, name.toLowerCase(), MappingKind.NAME, probability, occurrences);
    }

    @Override
    public void addName(IWord word, String name, double probability) {
        addName(word, name.toLowerCase(), probability, List.of(word.getText()));
    }

    @Override
    public void addNort(IWord n, String ref, double probability) {
        addNort(n, ref.toLowerCase(), probability, List.of(n.getText()));
    }

    @Override
    public void addNort(IWord n, String ref, double probability, List<String> occurrences) {
        addNounMapping(n, ref.toLowerCase(), MappingKind.NAME_OR_TYPE, probability, occurrences);
        List<NounMappingForEagle> wordsWithSimilarNode = nounMappings.values()
                .stream()
                .filter(mapping -> mapping.getNodes().contains(n))
                .collect(Collectors.toList());
        for (NounMappingForEagle mapping : wordsWithSimilarNode) {
            if (mapping.getProbabilityForName() == 0) {
                mapping.addKindWithProbability(MappingKind.NAME, TextExtractionStateConfig.NORT_PROBABILITY_FOR_NAME_AND_TYPE);
            }
            if (mapping.getProbabilityForType() == 0) {
                mapping.addKindWithProbability(MappingKind.TYPE, TextExtractionStateConfig.NORT_PROBABILITY_FOR_NAME_AND_TYPE);
            }
        }
    }

    @Override
    public void addType(IWord n, String type, double probability) {
        addType(n, type.toLowerCase(), probability, List.of(n.getText()));
    }

    @Override
    public void addType(IWord n, String type, double probability, List<String> occurrences) {
        addNounMapping(n, type.toLowerCase(), MappingKind.TYPE, probability, occurrences);
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
    public void addRelation(IRelationMapping n) {
        if (!relationMappings.contains(n)) {
            relationMappings.add(n);
        }
    }

    @Override
    public void addTerm(String reference, INounMapping mapping1, INounMapping mapping2, MappingKind kind, double probability) {
        addTerm(reference, List.of(mapping1, mapping2), kind, probability);
    }

    @Override
    public void addTerm(String reference, INounMapping mapping1, INounMapping mapping2, List<INounMapping> otherMappings, MappingKind kind,
            double probability) {

        List<INounMapping> mappings = new ArrayList<>();
        mappings.add(mapping1);
        mappings.add(mapping2);
        mappings.addAll(otherMappings);
        addTerm(reference, mappings, kind, probability);
    }

    @Override
    public void addNameTerm(String reference, INounMapping mapping1, INounMapping mapping2, double probability) {
        addTerm(reference, List.of(mapping1, mapping2), MappingKind.NAME, probability);
    }

    @Override
    public void addTypeTerm(String reference, INounMapping mapping1, INounMapping mapping2, double probability) {
        addTerm(reference, List.of(mapping1, mapping2), MappingKind.TYPE, probability);
    }

    @Override
    public void removeNounNode(INounMapping n) {
        nounMappings.remove(n.getReference());
    }

    @Override
    public void removeRelation(IRelationMapping n) {
        relationMappings.remove(n);
    }

    @Override
    public void removeTerm(ITermMapping term) {
        terms.remove(term);
    }

    @Override
    public List<ITermMapping> getTerms() {
        return new ArrayList<>(terms);
    }

    @Override
    public List<ITermMapping> getTermsByMappings(List<INounMapping> nounMappings) {
        return terms.stream()
                .filter(//
                        t -> t.getMappings().containsAll(nounMappings) && nounMappings.containsAll(t.getMappings()))
                .//
                collect(Collectors.toList());
    }

    @Override
    public List<ITermMapping> getTermsBySimilarReference(String reference) {
        return terms.stream().filter(t -> SimilarityUtils.areWordsSimilar(reference, t.getReference())).collect(Collectors.toList());
    }

    @Override
    public List<ITermMapping> getTermsByMappingsAndKind(List<INounMapping> nounMappings, MappingKind kind) {
        List<ITermMapping> termsByMapping = getTermsByMappings(nounMappings);
        return termsByMapping.stream().filter(t -> t.getKind().equals(kind)).collect(Collectors.toList());
    }

    @Override
    public List<INounMapping> getTypes() {
        return nounMappings.values().stream().filter(n -> n.getKind().equals(MappingKind.TYPE)).collect(Collectors.toList());
    }

    @Override
    public List<ITermMapping> getTypeTerms() {
        return terms.stream().filter(n -> n.getKind().equals(MappingKind.TYPE)).collect(Collectors.toList());
    }

    @Override
    public List<INounMapping> getNounMappingsByNode(IWord n) {
        return nounMappings.values().stream().filter(nMapping -> nMapping.getNodes().contains(n)).collect(Collectors.toList());
    }

    @Override
    public List<INounMapping> getNounMappingsWithSameReference(String ref) {
        return nounMappings.values().stream().filter(nMapping -> nMapping.getReference().equalsIgnoreCase(ref)).collect(Collectors.toList());
    }

    @Override
    public List<String> getNameList() {

        Set<String> names = new HashSet<>();
        List<INounMapping> nameMappings = getNames();
        for (INounMapping nnm : nameMappings) {
            names.add(nnm.getReference());
        }
        return new ArrayList<>(names);
    }

    @Override
    public List<String> getNameTermList() {
        Set<String> names = new HashSet<>();
        List<ITermMapping> nameMappings = getNameTerms();
        for (ITermMapping nnm : nameMappings) {
            names.add(nnm.getReference());
        }
        return new ArrayList<>(names);
    }

    @Override
    public List<String> getNortList() {
        Set<String> norts = new HashSet<>();
        List<INounMapping> nortMappings = getNameOrTypeMappings();
        for (INounMapping nnm : nortMappings) {
            norts.add(nnm.getReference());
        }
        return new ArrayList<>(norts);
    }

    @Override
    public List<String> getTypeList() {

        Set<String> types = new HashSet<>();
        List<INounMapping> typeMappings = getTypes();
        for (INounMapping nnm : typeMappings) {
            types.add(nnm.getReference());
        }
        return new ArrayList<>(types);
    }

    @Override
    public List<String> getTypeTermList() {

        Set<String> types = new HashSet<>();
        List<ITermMapping> typeMappings = getTypeTerms();
        for (ITermMapping nnm : typeMappings) {
            types.add(nnm.getReference());
        }
        return new ArrayList<>(types);
    }

    @Override
    public List<INounMapping> getNames() {
        return nounMappings.values().stream().filter(n -> n.getKind().equals(MappingKind.NAME)).collect(Collectors.toList());
    }

    @Override
    public List<ITermMapping> getNameTerms() {
        return terms.stream().filter(n -> n.getKind().equals(MappingKind.NAME)).collect(Collectors.toList());
    }

    @Override
    public List<INounMapping> getNameOrTypeMappings() {
        return nounMappings.values().stream().filter(n -> n.getKind().equals(MappingKind.NAME_OR_TYPE)).collect(Collectors.toList());
    }

    @Override
    public List<INounMapping> getTypeNodesByNode(IWord node) {
        return nounMappings.values()
                .stream()
                .filter(n -> n.getNodes().contains(node))
                .filter(n -> n.getKind() == MappingKind.TYPE)
                .collect(Collectors.toList());
    }

    @Override
    public List<INounMapping> getNameNodesByNode(IWord node) {
        return nounMappings.values()
                .stream()
                .filter(n -> n.getNodes().contains(node))
                .filter(n -> n.getKind() == MappingKind.NAME)
                .collect(Collectors.toList());
    }

    @Override
    public List<INounMapping> getNortNodesByNode(IWord node) {
        return nounMappings.values()
                .stream()
                .filter(n -> n.getNodes().contains(node))
                .filter(n -> n.getKind() == MappingKind.NAME_OR_TYPE)
                .collect(Collectors.toList());
    }

    @Override
    public List<IRelationMapping> getRelations() {
        return new ArrayList<>(relationMappings);
    }

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

    @Override
    public boolean isNodeContainedByNameOrTypeNodes(IWord node) {
        return !nounMappings.values()
                .stream()
                .filter(n -> n.getKind().equals(MappingKind.NAME_OR_TYPE))
                .filter(n -> n.getNodes().contains(node))
                .findAny()
                .isEmpty();
    }

    @Override
    public boolean isNodeContainedByNameNodes(IWord node) {
        return !nounMappings.values().stream().filter(n -> n.getKind().equals(MappingKind.NAME)).filter(n -> n.getNodes().contains(node)).findAny().isEmpty();
    }

    @Override
    public boolean isNodeContainedByTermMappings(IWord node) {

        for (ITermMapping term : terms) {
            if (term.getMappings().stream().anyMatch(n -> n.getNodes().contains(node))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ITermMapping> getTermMappingsByNode(IWord node) {

        return terms.stream()
                .filter(//
                        term -> term.getMappings().stream().anyMatch(n -> n.getNodes().contains(node)))
                .collect(Collectors.toList());

    }

    @Override
    public boolean isNodeContainedByNounMappings(IWord node) {
        return !nounMappings.values().stream().filter(n -> n.getNodes().contains(node)).findAny().isEmpty();
    }

    @Override
    public boolean isNodeContainedByTypeNodes(IWord node) {
        return !nounMappings.values().stream().filter(n -> n.getKind().equals(MappingKind.TYPE)).filter(n -> n.getNodes().contains(node)).findAny().isEmpty();
    }

    @Override

    public String toString() {
        return "TextExtractionState [nounMappings=" + String.join("\n", nounMappings.toString()) + ", relationNodes="
                + String.join("\n", relationMappings.toString()) + "]";
    }

    @Override
    public List<INounMapping> getAllMappings() {
        return new ArrayList<>(nounMappings.values());
    }

    @Override
    public List<INounMappingWithDistribution> getMappingsWithDistributions() {
        return new ArrayList<>(nounMappings.values());
    }

    @Override
    public void addNounMapping(List<IWord> nodes, String reference, MappingKind kind, double confidence, List<String> occurrences) {

        NounMappingForEagle mapping = new NounMappingForEagle(nodes, Map.of(kind, confidence), reference, occurrences);
        nounMappings.put(mapping.getReference(), mapping);

    }

}
