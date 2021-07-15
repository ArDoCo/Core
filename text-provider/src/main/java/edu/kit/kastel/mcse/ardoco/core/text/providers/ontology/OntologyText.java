package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.rdf.model.Property;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.ontology.OntologyConnector;

public class OntologyText implements IText {
    private OntologyConnector ontologyConnector;
    private Individual textIndividual;

    private DatatypeProperty uuidProperty;
    private ObjectProperty wordsProperty;

    private OntologyText(OntologyConnector ontologyConnector, Individual textIndividual) {
        this.ontologyConnector = ontologyConnector;
        this.textIndividual = textIndividual;
    }

    protected static OntologyText get(OntologyConnector ontologyConnector, Individual textIndividual) {
        var ontologyText = new OntologyText(ontologyConnector, textIndividual);
        ontologyText.init();
        return ontologyText;
    }

    protected static OntologyText get(OntologyConnector ontologyConnector) {
        var optText = getTextIndividual(ontologyConnector);
        if (optText.isEmpty()) {
            throw new IllegalStateException("Cannot find a text in the ontology.");
        }
        var ontologyText = new OntologyText(ontologyConnector, optText.get());
        ontologyText.init();
        return ontologyText;
    }

    protected static Optional<Individual> getTextIndividual(OntologyConnector ontologyConnector) {
        var textIndividuals = ontologyConnector.getIndividualsOfClass("TextDocument");
        if (textIndividuals.isEmpty()) {
            return Optional.empty();
        } else {
            // Assumption: We only have one text right now and always retrieve only the first one
            return Optional.of(textIndividuals.get(0));
        }
    }

    private void init() {
        uuidProperty = ontologyConnector.getDataProperty("uuid").orElseThrow();
        wordsProperty = ontologyConnector.getObjectProperty("has words").orElseThrow();
    }

    @Override
    public IWord getStartNode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<IWord> getWords() {
        var words = new ArrayList<IWord>();
        var textListIndividualNode = textIndividual.getPropertyValue(getWordsProperty());
        if (!textListIndividualNode.canAs(Individual.class)) {
            throw new IllegalStateException("Cannot find a text in the ontology.");
        }
        var textListIndividual = textListIndividualNode.as(Individual.class);
        var wordIndividualListOpt = ontologyConnector.getListByIri(textListIndividual.getURI());
        if (wordIndividualListOpt.isEmpty()) {
            return words;
        }
        var wordIndividualList = wordIndividualListOpt.get().toList();
        for (var wordIndividual : wordIndividualList) {
            // TODO

        }
    }

    private Property getWordsProperty() {
        return ontologyConnector.getProperty("has words").orElseThrow();
    }

}
