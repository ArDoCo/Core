package edu.kit.kastel.mcse.ardoco.core.datastructures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.common.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMappingWithDistribution;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRelationMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITermMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextStateWithDistributions;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.MappingKind;

public class TextStateWithClustering extends AbstractTextState implements ITextStateWithDistributions {
    private Map<String, NounMappingWithDistribution> nounMappings;
    private double similarityPercentage;

    @Override
    protected Collection<? extends INounMapping> getReadableNounMappings() {
        return nounMappings.values();
    }

    @Override
    public ITextState createCopy() {
        TextStateWithClustering textExtractionState = new TextStateWithClustering(similarityPercentage);
        textExtractionState.nounMappings = new HashMap<>(nounMappings);
        textExtractionState.relationMappings = relationMappings.stream().map(IRelationMapping::createCopy).collect(Collectors.toList());
        textExtractionState.terms = terms.stream().map(ITermMapping::createCopy).collect(Collectors.toList());
        return textExtractionState;
    }

    /**
     * Creates a new name type relation state
     */
    public TextStateWithClustering(double similarityPercentage) {
        nounMappings = new HashMap<>();
        relationMappings = new ArrayList<>();
        terms = new ArrayList<>();
        this.similarityPercentage = similarityPercentage;
    }

    @Override
    public void addNounMapping(List<IWord> nodes, String reference, MappingKind kind, double confidence, List<String> occurrences) {
        NounMappingWithDistribution mapping = new NounMappingWithDistribution(nodes, Map.of(kind, confidence), reference, occurrences);
        nounMappings.put(mapping.getReference(), mapping);
    }

    @Override
    protected void addNounMapping(IWord word, String reference, MappingKind kind, double probability, List<String> occurrences) {
        if (SimilarityUtils.containsSeparator(reference)) {
            List<String> parts = SimilarityUtils.splitAtSeparators(reference).stream().filter(part -> part.length() > 1).collect(Collectors.toList());
            for (String referencePart : parts) {
                addNounMapping(word, referencePart, kind, probability, occurrences);
            }
            return;

        }

        if (nounMappings.containsKey(reference)) {
            // extend existing nounMapping
            NounMappingWithDistribution existingMapping = nounMappings.get(reference);
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
                    NounMappingWithDistribution similarMapping = nounMappings.get(similarRefs.get(0));
                    similarMapping.addOccurrence(occurrences);
                    similarMapping.addNode(word);
                    similarMapping.addKindWithProbability(kind, probability);

                } else {
                    for (String ref : similarRefs) {
                        NounMappingWithDistribution similarMapping = nounMappings.get(ref);
                        similarMapping.addOccurrence(occurrences);
                        similarMapping.addNode(word);
                        similarMapping.addKindWithProbability(kind, probability);
                    }
                }
            } else {
                // create new nounMapping
                NounMappingWithDistribution mapping = new NounMappingWithDistribution(List.of(word), kind, probability, reference, occurrences);
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

    @Override
    public void addNort(IWord n, String ref, double probability, List<String> occurrences) {
        super.addNort(n, ref, probability, occurrences);

        List<NounMappingWithDistribution> wordsWithSimilarNode = nounMappings.values()
                .stream()
                .filter(mapping -> mapping.getWords().contains(n))
                .collect(Collectors.toList());
        for (NounMappingWithDistribution mapping : wordsWithSimilarNode) {
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

    @Override
    public List<INounMappingWithDistribution> getMappingsWithDistributions() {
        return new ArrayList<>(nounMappings.values());
    }

}
