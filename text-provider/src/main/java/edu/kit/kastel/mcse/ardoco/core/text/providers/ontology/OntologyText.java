package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.ontology.OntologyConnector;

public class OntologyText implements IText {
    private static final String ERR_NO_LIST = "Could not find list of words in the ontology.";
    private static final String ERR_NO_TEXT_FOUND = "Cannot find a text in the ontology.";

    private OntologyConnector ontologyConnector;
    private Individual textIndividual;

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
            throw new IllegalStateException(ERR_NO_TEXT_FOUND);
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
        wordsProperty = ontologyConnector.getObjectProperty("has words").orElseThrow();
    }

    @Override
    public IWord getStartNode() {
        var textListIndividualNode = textIndividual.getPropertyValue(wordsProperty);
        if (textListIndividualNode == null || !textListIndividualNode.canAs(Individual.class)) {
            throw new IllegalStateException(ERR_NO_TEXT_FOUND);
        }
        var textListIndividual = textListIndividualNode.as(Individual.class);
        var textOloOpt = ontologyConnector.transformIntoOrderedOntologyList(textListIndividual);
        if (textOloOpt.isEmpty()) {
            return null;
        }

        var wordIndividualList = textOloOpt.get();
        return OntologyWord.get(ontologyConnector, wordIndividualList.get(0));
    }

    @Override
    public int getLength() {
        var textListIndividualNode = textIndividual.getPropertyValue(wordsProperty);
        if (!textListIndividualNode.canAs(Individual.class)) {
            throw new IllegalStateException(ERR_NO_TEXT_FOUND);
        }
        var textListIndividual = textListIndividualNode.as(Individual.class);
        var textOloOpt = ontologyConnector.transformIntoOrderedOntologyList(textListIndividual);
        if (textOloOpt.isEmpty()) {
            throw new IllegalStateException(ERR_NO_LIST);
        }
        return textOloOpt.get().size();
    }

    @Override
    public List<IWord> getWords() {
        var words = new ArrayList<IWord>();
        var textListIndividualNode = textIndividual.getPropertyValue(wordsProperty);
        if (!textListIndividualNode.canAs(Individual.class)) {
            throw new IllegalStateException(ERR_NO_TEXT_FOUND);
        }
        var textListIndividual = textListIndividualNode.as(Individual.class);
        var textOloOpt = ontologyConnector.transformIntoOrderedOntologyList(textListIndividual);
        if (textOloOpt.isEmpty()) {
            throw new IllegalStateException(ERR_NO_LIST);
        }

        var wordIndividualList = textOloOpt.get().toList();
        for (var wordIndividual : wordIndividualList) {
            var word = OntologyWord.get(ontologyConnector, wordIndividual);
            words.add(word);
        }
        return words;
    }

}
