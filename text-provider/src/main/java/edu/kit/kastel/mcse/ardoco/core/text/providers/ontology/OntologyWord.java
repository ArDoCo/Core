package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.POSTag;
import edu.kit.kastel.mcse.ardoco.core.ontology.OntologyConnector;

public class OntologyWord implements IWord {
    private OntologyConnector ontologyConnector;
    private Individual wordIndividual;

    private DatatypeProperty textProperty;
    private DatatypeProperty posProperty;
    private DatatypeProperty lemmaProperty;
    private DatatypeProperty positionProperty;
    private DatatypeProperty sentenceProperty;
    private ObjectProperty hasItem;
    private ObjectProperty hasNext;
    private ObjectProperty hasPrevious;

    private OntologyWord(OntologyConnector ontologyConnector, Individual wordIndividual) {
        this.ontologyConnector = ontologyConnector;
        this.wordIndividual = wordIndividual;
    }

    protected static OntologyWord get(OntologyConnector ontologyConnector, Individual wordIndividual) {
        if (ontologyConnector == null || wordIndividual == null) {
            return null;
        }
        var ow = new OntologyWord(ontologyConnector, wordIndividual);
        ow.init();
        return ow;
    }

    private void init() {
        textProperty = ontologyConnector.getDataProperty("has text").orElseThrow();
        posProperty = ontologyConnector.getDataProperty("has POS").orElseThrow();
        lemmaProperty = ontologyConnector.getDataProperty("has lemma").orElseThrow();
        positionProperty = ontologyConnector.getDataProperty("has position").orElseThrow();
        sentenceProperty = ontologyConnector.getDataProperty("contained in sentence").orElseThrow();
        hasItem = ontologyConnector.getObjectProperty("has item").orElseThrow();
        hasNext = ontologyConnector.getObjectProperty("has next").orElseThrow();
        hasPrevious = ontologyConnector.getObjectProperty("has previous").orElseThrow();
    }

    @Override
    public int getSentenceNo() {
        var optInt = ontologyConnector.getPropertyIntValue(wordIndividual, sentenceProperty);
        if (optInt.isPresent()) {
            return optInt.get();
        }
        return -1;
    }

    @Override
    public String getText() {
        var optString = ontologyConnector.getPropertyStringValue(wordIndividual, textProperty);
        if (optString.isPresent()) {
            return optString.get();
        }
        return null;
    }

    @Override
    public POSTag getPosTag() {
        var optString = ontologyConnector.getPropertyStringValue(wordIndividual, posProperty);
        if (optString.isEmpty()) {
            return POSTag.NONE;
        }
        var posString = optString.get();
        return POSTag.get(posString);
    }

    @Override
    public IWord getPreWord() {
        Individual slot = getSlot();
        var prevSlotNode = slot.getPropertyValue(hasPrevious);
        if (prevSlotNode.canAs(Individual.class)) {
            Individual prevSlot = prevSlotNode.as(Individual.class);
            var prevIndividual = extractItemOutOfSlot(prevSlot);
            if (prevIndividual.isPresent()) {
                return get(ontologyConnector, prevIndividual.get());
            }
        }
        return null;
    }

    @Override
    public IWord getNextWord() {
        Individual slot = getSlot();
        var nextSlotNode = slot.getPropertyValue(hasNext);
        if (nextSlotNode.canAs(Individual.class)) {
            Individual nextSlot = nextSlotNode.as(Individual.class);
            var nextIndividual = extractItemOutOfSlot(nextSlot);
            if (nextIndividual.isPresent()) {
                return get(ontologyConnector, nextIndividual.get());
            }
        }
        return null;
    }

    private Optional<Individual> extractItemOutOfSlot(Individual slot) {
        if (slot == null) {
            return Optional.empty();
        }
        var itemNode = slot.getPropertyValue(hasItem);
        if (itemNode == null) {
            return Optional.empty();
        }
        return Optional.of(itemNode.as(Individual.class));
    }

    private Individual getSlot() {
        var optSlot = ontologyConnector.getFirstSubjectOf(hasItem, wordIndividual);
        if (optSlot.isPresent()) {
            var slot = optSlot.get();
            if (slot.canAs(Individual.class)) {
                return slot.as(Individual.class);
            }
        }
        throw new IllegalStateException("Word should be contained in a slot of a word list, but could not find the corresponding slot.");
    }

    @Override
    public int getPosition() {
        var optInt = ontologyConnector.getPropertyIntValue(wordIndividual, positionProperty);
        if (optInt.isPresent()) {
            return optInt.get();
        }
        return -1;
    }

    @Override
    public String getLemma() {
        var optString = ontologyConnector.getPropertyStringValue(wordIndividual, lemmaProperty);
        if (optString.isPresent()) {
            return optString.get();
        }
        return null;
    }

    @Override
    public List<IWord> getWordsThatAreDependencyOfThis(DependencyTag dependencyTag) {
        // TODO Auto-generated method stub
        return new ArrayList<>();
    }

    @Override
    public List<IWord> getWordsThatAreDependentOnThis(DependencyTag dependencyTag) {
        // TODO Auto-generated method stub
        return new ArrayList<>();
    }

}
