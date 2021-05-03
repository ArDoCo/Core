package edu.kit.kastel.mcse.ardoco.core.datastructures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;

public abstract class AbstractNounMapping implements INounMapping {
    protected List<IWord> words;
    protected String reference;
    protected List<String> occurrences;

    /**
     * Returns the occurrences of this mapping.
     *
     * @return all appearances of the mapping
     */
    @Override
    public final List<String> getOccurrences() {
        return new ArrayList<>(occurrences);
    }

    /**
     * Returns all nodes contained by the mapping
     *
     * @return all mapping nodes
     */
    @Override
    public final List<IWord> getWords() {
        return new ArrayList<>(words);
    }

    /**
     * Adds nodes to the mapping, if they are not already contained.
     *
     * @param nodes graph nodes to add to the mapping
     */
    @Override
    public final void addNodes(List<IWord> nodes) {
        for (IWord n : nodes) {
            addNode(n);
        }
    }

    /**
     * Adds a node to the mapping, it its not already contained.
     *
     * @param n graph node to add.
     */
    @Override
    public final void addNode(IWord n) {
        if (!words.contains(n)) {
            words.add(n);
        }
    }

    /**
     * Returns the reference, the comparable and naming attribute of this mapping.
     *
     * @return the reference
     */
    @Override
    public final String getReference() {
        return reference;
    }

    /**
     * Returns the sentence numbers of occurrences, sorted.
     *
     * @return sentence numbers of the occurrences of this mapping.
     */
    @Override
    public final List<Integer> getMappingSentenceNo() {
        List<Integer> positions = new ArrayList<>();
        for (IWord n : words) {
            positions.add(n.getSentenceNo() + 1);
        }
        Collections.sort(positions);
        return positions;
    }

    /**
     * Adds occurrences to the mapping
     *
     * @param newOccurances occurrences to add
     */
    @Override
    public final void addOccurrence(List<String> newOccurances) {
        for (String o : newOccurances) {
            if (!occurrences.contains(o)) {
                occurrences.add(o);
            }
        }
    }

    /**
     * Copies all nodes and occurrences matching the occurrence to another mapping
     *
     * @param occurrence     the occurrence to copy
     * @param createdMapping the other mapping
     */
    @Override
    public final void copyOccurrencesAndNodesTo(String occurrence, INounMapping createdMapping) {
        List<IWord> occNodes = words.stream().filter(n -> n.getText().equals(occurrence)).collect(Collectors.toList());
        createdMapping.addNodes(occNodes);
        createdMapping.addOccurrence(List.of(occurrence));
    }

    /**
     * Returns a list of all node lemmas encapsulated by a mapping.
     *
     * @return list of containing node lemmas
     */
    public final List<String> getMappingLemmas() {
        return words.stream().map(IWord::getLemma).collect(Collectors.toList());
    }
}
