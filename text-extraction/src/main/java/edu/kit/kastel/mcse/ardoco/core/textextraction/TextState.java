package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;

/**
 * The Class TextState defines the basic implementation of a {@link ITextState}.
 */
public class TextState implements ITextState {
    private double similarityPercentage;

    private Map<String, INounMapping> nounMappings;

    /** The relation mappings. */
    private MutableList<IRelationMapping> relationMappings;

    /** The terms. */
    private MutableList<ITermMapping> terms;

    /**
     * Creates a new name type relation state.
     *
     * @param similarityPercentage the similarity percentage
     */
    public TextState(double similarityPercentage) {
        nounMappings = new HashMap<>();
        relationMappings = Lists.mutable.empty();
        terms = Lists.mutable.empty();
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
    public final void addName(IWord n, String name, double probability, ImmutableList<String> occurrences) {
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
        addName(word, name.toLowerCase(), probability, Lists.immutable.with(word.getText()));
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
        addNort(n, ref.toLowerCase(), probability, Lists.immutable.with(n.getText()));
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
        addType(n, type.toLowerCase(), probability, Lists.immutable.with(n.getText()));
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
    public final void addType(IWord n, String type, double probability, ImmutableList<String> occurrences) {
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
        addTerm(reference, Lists.immutable.with(mapping1, mapping2), kind, probability);
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
    public final void addTerm(String reference, INounMapping mapping1, INounMapping mapping2, ImmutableList<INounMapping> otherMappings, MappingKind kind,
            double probability) {

        MutableList<INounMapping> mappings = Lists.mutable.empty();
        mappings.add(mapping1);
        mappings.add(mapping2);
        mappings.addAll(otherMappings.castToCollection());
        addTerm(reference, mappings.toImmutable(), kind, probability);
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
        addTerm(reference, Lists.immutable.with(mapping1, mapping2), MappingKind.NAME, probability);
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
        addTerm(reference, Lists.immutable.with(mapping1, mapping2), MappingKind.TYPE, probability);
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
    public final ImmutableList<INounMapping> getNounMappings() {
        return Lists.immutable.withAll(nounMappings.values());
    }

    /**
     * Getter for the terms of this state.
     *
     * @return the list of found terms
     */
    @Override
    public final ImmutableList<ITermMapping> getTerms() {
        return Lists.immutable.withAll(terms);
    }

    /**
     * Returns all type mappings.
     *
     * @return all type mappings as list
     */
    @Override
    public final ImmutableList<INounMapping> getTypes() {
        return Lists.immutable.fromStream(nounMappings.values().stream().filter(n -> MappingKind.TYPE == n.getKind()));
    }

    /**
     * Returns all type term mappings.
     *
     * @return all type term mappings as list
     */
    @Override
    public final ImmutableList<ITermMapping> getTypeTerms() {
        return terms.select(n -> MappingKind.TYPE == n.getKind()).toImmutable();
    }

    /**
     * Returns all mappings containing the given node.
     *
     * @param word the given word
     * @return all mappings containing the given node as list
     */
    @Override
    public final ImmutableList<INounMapping> getNounMappingsByWord(IWord word) {
        return Lists.immutable.fromStream(nounMappings.values().stream().filter(nMapping -> nMapping.getWords().contains(word)));
    }

    /**
     * Returns all mappings with the exact same reference as given.
     *
     * @param ref the reference to search for
     * @return a list of noun mappings with the given reference.
     */
    @Override
    public final ImmutableList<INounMapping> getNounMappingsWithSameReference(String ref) {
        return Lists.immutable.fromStream(nounMappings.values().stream().filter(nMapping -> nMapping.getReference().equalsIgnoreCase(ref)));
    }

    /**
     * Returns a list of all references of name mappings.
     *
     * @return all references of name mappings as list.
     */
    @Override
    public final ImmutableList<String> getNameList() {

        Set<String> names = new HashSet<>();
        ImmutableList<INounMapping> nameMappings = getNames();
        for (INounMapping nnm : nameMappings) {
            names.add(nnm.getReference());
        }
        return Lists.immutable.withAll(names);
    }

    /**
     * Returns a list of all references of name term mappings.
     *
     * @return all references of name term mappings as list.
     */
    @Override
    public final ImmutableList<String> getNameTermList() {
        Set<String> names = new HashSet<>();
        ImmutableList<ITermMapping> nameMappings = getNameTerms();
        for (ITermMapping nnm : nameMappings) {
            names.add(nnm.getReference());
        }
        return Lists.immutable.withAll(names);
    }

    /**
     * Returns a list of all references of name or type mappings.
     *
     * @return all references of name or type mappings as list.
     */
    @Override
    public final ImmutableList<String> getNortList() {
        Set<String> norts = new HashSet<>();
        ImmutableList<INounMapping> nortMappings = getNameOrTypeMappings();
        for (INounMapping nnm : nortMappings) {
            norts.add(nnm.getReference());
        }
        return Lists.immutable.withAll(norts);
    }

    /**
     * Returns a list of all references of type mappings.
     *
     * @return all references of type mappings as list.
     */
    @Override
    public final ImmutableList<String> getTypeList() {

        Set<String> types = new HashSet<>();
        ImmutableList<INounMapping> typeMappings = getTypes();
        for (INounMapping nnm : typeMappings) {
            types.add(nnm.getReference());
        }
        return Lists.immutable.withAll(types);
    }

    /**
     * Getter for the terms of this state, that have exactly the same nounMappings.
     *
     * @param nounMappings the nounMappings to search for
     * @return a list of terms with that nounMappings
     */
    @Override
    public final ImmutableList<ITermMapping> getTermsByMappings(ImmutableList<INounMapping> nounMappings) {
        return terms.select(t -> t.getMappings().containsAll(nounMappings.castToCollection()) && nounMappings.containsAll(t.getMappings().castToCollection()))
                .toImmutable();
    }

    @Override
    public final ImmutableList<ITermMapping> getTermsBySimilarReference(String reference) {
        return terms.select(t -> SimilarityUtils.areWordsSimilar(reference, t.getReference())).toImmutable();
    }

    @Override
    public final ImmutableList<ITermMapping> getTermsByMappingsAndKind(ImmutableList<INounMapping> nounMappings, MappingKind kind) {
        ImmutableList<ITermMapping> termsByMapping = getTermsByMappings(nounMappings);
        return termsByMapping.select(t -> t.getKind() == kind);
    }

    /**
     * Returns a list of all references of type term mappings.
     *
     * @return all references of type term mappings as list.
     */
    @Override
    public final ImmutableList<String> getTypeTermList() {

        Set<String> types = new HashSet<>();
        ImmutableList<ITermMapping> typeMappings = getTypeTerms();
        for (ITermMapping nnm : typeMappings) {
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
        return Lists.immutable.fromStream(nounMappings.values().stream().filter(n -> MappingKind.NAME == n.getKind()));
    }

    /**
     * Returns all name term mappings
     *
     * @return a list of all name term mappings
     */
    @Override
    public final ImmutableList<ITermMapping> getNameTerms() {
        return terms.select(n -> MappingKind.NAME == n.getKind()).toImmutable();
    }

    /**
     * Returns all name or type mappings
     *
     * @return a list of all name or type mappings
     */
    @Override
    public final ImmutableList<INounMapping> getNameOrTypeMappings() {
        return Lists.immutable.fromStream(nounMappings.values().stream().filter(n -> MappingKind.NAME_OR_TYPE == n.getKind()));
    }

    /**
     * Returns alltype mappings containing the given node.
     *
     * @param node node to filter for
     * @return a list of alltype mappings containing the given node
     */
    @Override
    public final ImmutableList<INounMapping> getTypeNodesByNode(IWord node) {
        return Lists.immutable.fromStream(nounMappings.values().stream().filter(n -> n.getWords().contains(node)).filter(n -> n.getKind() == MappingKind.TYPE));
    }

    /**
     * Returns all name mappings containing the given node.
     *
     * @param node node to filter for
     * @return a list of all name mappings containing the given node
     */
    @Override
    public final ImmutableList<INounMapping> getNameNodesByNode(IWord node) {
        return Lists.immutable.fromStream(nounMappings.values().stream().filter(n -> n.getWords().contains(node)).filter(n -> n.getKind() == MappingKind.NAME));
    }

    /**
     * Returns all name or type mappings containing the given node.
     *
     * @param node node to filter for
     * @return a list of all name or type mappings containing the given node
     */
    @Override
    public final ImmutableList<INounMapping> getNortNodesByNode(IWord node) {
        return Lists.immutable
                .fromStream(nounMappings.values().stream().filter(n -> n.getWords().contains(node)).filter(n -> n.getKind() == MappingKind.NAME_OR_TYPE));
    }

    /**
     * Returns all relation mappings.
     *
     * @return relation mappings as list
     */
    @Override
    public final ImmutableList<IRelationMapping> getRelations() {
        return Lists.immutable.withAll(relationMappings);
    }

    /**
     * Returns all term mappings that contain the given noun mapping.
     *
     * @param nounMapping the noun mapping that should be contained.
     * @return all term mappings that contain the noun mapping.
     */
    @Override
    public final ImmutableList<ITermMapping> getTermsByContainedMapping(INounMapping nounMapping) {

        MutableList<ITermMapping> filteredTerms = Lists.mutable.empty();

        for (ITermMapping term : terms) {
            if (term.getMappings().contains(nounMapping)) {
                filteredTerms.add(term);
            }
        }
        return filteredTerms.toImmutable();
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
                .filter(n -> MappingKind.NAME_OR_TYPE == n.getKind())
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
        return !nounMappings.values().stream().filter(n -> MappingKind.NAME == n.getKind()).filter(n -> n.getWords().contains(node)).findAny().isEmpty();
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
    public final ImmutableList<ITermMapping> getTermMappingsByNode(IWord node) {
        return Lists.immutable.fromStream(terms.stream().filter(term -> term.getMappings().stream().anyMatch(n -> n.getWords().contains(node))));
    }

    /**
     * Returns if a node is contained by the mappings.
     *
     * @param node node to check
     * @return true if the node is contained by mappings.
     */
    @Override
    public final boolean isNodeContainedByNounMappings(IWord node) {
        return nounMappings.values().stream().anyMatch(n -> n.getWords().contains(node));
    }

    /**
     * Returns if a node is contained by the type mappings.
     *
     * @param node node to check
     * @return true if the node is contained by type mappings.
     */
    @Override
    public final boolean isNodeContainedByTypeNodes(IWord node) {
        return nounMappings.values().stream().anyMatch(n -> MappingKind.TYPE == n.getKind() && n.getWords().contains(node));
    }

    @Override
    public ITextState createCopy() {
        var textExtractionState = new TextState(similarityPercentage);
        textExtractionState.nounMappings = new HashMap<>(nounMappings);
        textExtractionState.relationMappings = relationMappings.collect(IRelationMapping::createCopy);
        textExtractionState.terms = terms.collect(ITermMapping::createCopy);
        return textExtractionState;
    }

    @Override
    public void addNounMapping(INounMapping nounMapping) {
        nounMappings.put(nounMapping.getReference(), nounMapping);
    }

    @Override
    public void addNounMapping(ImmutableList<IWord> nodes, String reference, MappingKind kind, double confidence, ImmutableList<String> occurrences) {
        INounMapping mapping = new NounMapping(nodes, Map.of(kind, confidence), reference, occurrences);
        nounMappings.put(mapping.getReference(), mapping);
    }

    private void addNounMapping(IWord word, String reference, MappingKind kind, double probability, ImmutableList<String> occurrences) {
        if (CommonUtilities.containsSeparator(reference)) {
            ImmutableList<String> parts = CommonUtilities.splitAtSeparators(reference).select(part -> part.length() > 1);
            for (String referencePart : parts) {
                addNounMapping(word, referencePart, kind, probability, occurrences);
            }
            return;
        }

        if (nounMappings.containsKey(reference)) {
            // extend existing nounMapping
            var existingMapping = nounMappings.get(reference);
            existingMapping.addKindWithProbability(kind, probability);
            existingMapping.addOccurrence(occurrences);
            existingMapping.addWord(word);

        } else {
            ImmutableList<String> similarRefs = Lists.immutable
                    .fromStream(nounMappings.keySet().stream().filter(ref -> SimilarityUtils.areWordsSimilar(ref, reference, similarityPercentage)));
            for (String ref : similarRefs) {
                INounMapping similarMapping = nounMappings.get(ref);
                similarMapping.addOccurrence(occurrences);
                similarMapping.addWord(word);
                similarMapping.addKindWithProbability(kind, probability);
            }
            if (similarRefs.isEmpty()) {
                // create new nounMapping
                INounMapping mapping = new NounMapping(Lists.immutable.with(word), kind, probability, reference, occurrences);
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
    private void addTerm(String reference, ImmutableList<INounMapping> mappings, MappingKind kind, double probability) {
        ImmutableList<ITermMapping> includedTerms = getTermsByMappingsAndKind(mappings, kind);

        if (!includedTerms.isEmpty()) {
            for (ITermMapping includedTerm : includedTerms) {
                includedTerm.updateProbability(probability);
            }
        } else {
            ITermMapping term;
            if (mappings.size() <= 2) {
                term = new TermMapping(reference, mappings.get(0), mappings.get(1), Lists.immutable.with(), kind, probability);
            } else {
                term = new TermMapping(reference, mappings.get(0), mappings.get(1), mappings.subList(2, mappings.size() - 1), kind, probability);
            }
            terms.add(term);
        }
    }

    @Override
    public void addNort(IWord n, String ref, double probability, ImmutableList<String> occurrences) {
        addNounMapping(n, ref.toLowerCase(), MappingKind.NAME_OR_TYPE, probability, occurrences);

        ImmutableList<INounMapping> wordsWithSimilarNode = Lists.immutable
                .fromStream(nounMappings.values().stream().filter(mapping -> mapping.getWords().contains(n)));
        for (INounMapping mapping : wordsWithSimilarNode) {
            if (CommonUtilities.valueEqual(mapping.getProbabilityForName(), 0)) {
                mapping.addKindWithProbability(MappingKind.NAME, TextExtractionStateConfig.NORT_PROBABILITY_FOR_NAME_AND_TYPE);
            }
            if (CommonUtilities.valueEqual(mapping.getProbabilityForType(), 0)) {
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
