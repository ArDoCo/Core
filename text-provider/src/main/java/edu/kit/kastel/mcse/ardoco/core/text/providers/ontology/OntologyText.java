package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;

import java.util.Optional;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.ontology.OntologyConnector;
import edu.kit.kastel.mcse.ardoco.core.ontology.OrderedOntologyList;

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
    public IWord getFirstWord() {
        var wordIndividualList = getOrderedOntologyListOfText();
        return OntologyWord.get(ontologyConnector, wordIndividualList.get(0));
    }

    @Override
    public int getLength() {
        return getOrderedOntologyListOfText().size();
    }

    @Override
    public ImmutableList<IWord> getWords() {
        MutableList<IWord> words = Lists.mutable.empty();
        var textOlo = getOrderedOntologyListOfText();

        var wordIndividualList = textOlo.toList();
        for (var wordIndividual : wordIndividualList) {
            var word = OntologyWord.get(ontologyConnector, wordIndividual);
            words.add(word);
        }
        return words.toImmutable();
    }

    private OrderedOntologyList getOrderedOntologyListOfText() {
        var textListIndividualNode = ontologyConnector.getPropertyValue(textIndividual, wordsProperty);
        if (!textListIndividualNode.canAs(Individual.class)) {
            throw new IllegalStateException(ERR_NO_TEXT_FOUND);
        }
        var textListIndividual = ontologyConnector.transformIntoIndividual(textListIndividualNode);
        if (textListIndividual.isEmpty()) {
            throw new IllegalStateException(ERR_NO_TEXT_FOUND);
        }
        var textOloOpt = ontologyConnector.transformIntoOrderedOntologyList(textListIndividual.get());
        if (textOloOpt.isEmpty()) {
            throw new IllegalStateException(ERR_NO_LIST);
        }
        return textOloOpt.get();
    }

}
