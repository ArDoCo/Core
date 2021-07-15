package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;

import java.util.ArrayList;
import java.util.List;

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
    private ObjectProperty nextProperty;
    private ObjectProperty prevProperty;

    private OntologyWord(OntologyConnector ontologyConnector, Individual wordIndividual) {
        this.ontologyConnector = ontologyConnector;
        this.wordIndividual = wordIndividual;
    }

    protected static OntologyWord get(OntologyConnector ontologyConnector, Individual wordIndividual) {
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
        nextProperty = ontologyConnector.getObjectProperty("has next word").orElseThrow();
        prevProperty = ontologyConnector.getObjectProperty("has previous word").orElseThrow();
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
        var optString = ontologyConnector.getPropertyStringValue(wordIndividual, textProperty);
        if (optString.isEmpty()) {
            return POSTag.NONE;
        }
        var posString = optString.get();
        return POSTag.get(posString);
    }

    @Override
    public IWord getPreWord() {
        // TODO Auto-generated method stub
        // Idea: get slot and then get the previous word

        return null;
    }

    @Override
    public IWord getNextWord() {
        // TODO Auto-generated method stub
        return null;
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
