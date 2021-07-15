package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;

import java.util.ArrayList;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.vocabulary.XSD;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.ontology.OntologyConnector;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;

public class OntologyTextProvider implements ITextConnector {
    private static final String TEXT = "TextDocument";
    private static final String TEXT_ONTOLOGY_IRI = "https://informalin.github.io/knowledgebases/informalin_base_text.owl";

    private OntologyConnector ontologyConnector;

    private OntClass textClass;
    private OntClass wordClass;
    private DatatypeProperty uuidProperty;
    private DatatypeProperty textProperty;
    private DatatypeProperty posProperty;
    private DatatypeProperty lemmaProperty;
    private DatatypeProperty positionProperty;
    private DatatypeProperty sentenceProperty;
    private ObjectProperty wordsProperty;

    private OntologyTextProvider(OntologyConnector ontologyConnector) {
        this.ontologyConnector = ontologyConnector;
    }

    private OntologyTextProvider(String ontologyPath) {
        ontologyConnector = new OntologyConnector(ontologyPath);
    }

    public static OntologyTextProvider get(String ontologyPath) {
        var otp = new OntologyTextProvider(ontologyPath);
        otp.init();
        return otp;
    }

    public static OntologyTextProvider get(OntologyConnector ontologyConnector) {
        var otp = new OntologyTextProvider(ontologyConnector);
        otp.init();
        return otp;
    }

    private void init() {
        ontologyConnector.addOntologyImport(TEXT_ONTOLOGY_IRI);

        textClass = ontologyConnector.getClass(TEXT).orElseThrow();
        wordClass = ontologyConnector.getClass("Word").orElseThrow();
        uuidProperty = ontologyConnector.getDataProperty("uuid").orElseThrow();
        textProperty = ontologyConnector.getDataProperty("has text").orElseThrow();
        posProperty = ontologyConnector.getDataProperty("has POS").orElseThrow();
        lemmaProperty = ontologyConnector.getDataProperty("has lemma").orElseThrow();
        positionProperty = ontologyConnector.getDataProperty("has position").orElseThrow();
        sentenceProperty = ontologyConnector.getDataProperty("contained in sentence").orElseThrow();
        wordsProperty = ontologyConnector.getObjectProperty("has words").orElseThrow();
    }

    public void addText(IText text) {
        // create text in ontology
        var uuid = generateUUID();
        var name = "Text_" + uuid; // TODO should texts have a name? E.g. the filename etc.?
        var textIndividual = ontologyConnector.addIndividualToClass(name, textClass);
        ontologyConnector.addPropertyToIndividual(textIndividual, uuidProperty, uuid);

        // add word individuals
        var wordIndividuals = new ArrayList<Individual>();
        for (var word : text.getWords()) {
            var wordIndividual = addWord(word);
            wordIndividuals.add(wordIndividual);
        }

        // create the list that is used for the words property
        var olo = ontologyConnector.addList("WordsOf" + name, wordIndividuals);
        var listIndividual = olo.getListIndividual();
        textIndividual.addProperty(wordsProperty, listIndividual);
    }

    private Individual addWord(IWord word) {
        var label = word.getText();
        var uuid = generateUUID();
        var wordIndividual = ontologyConnector.addIndividualToClass(label, wordClass);
        ontologyConnector.addPropertyToIndividual(wordIndividual, uuidProperty, uuid);

        ontologyConnector.addPropertyToIndividual(wordIndividual, textProperty, word.getText());
        ontologyConnector.addPropertyToIndividual(wordIndividual, posProperty, word.getPosTag().getTag());
        ontologyConnector.addPropertyToIndividual(wordIndividual, lemmaProperty, word.getLemma());
        ontologyConnector.addPropertyToIndividual(wordIndividual, sentenceProperty, word.getSentenceNo(), XSD.nonNegativeInteger.toString());
        ontologyConnector.addPropertyToIndividual(wordIndividual, positionProperty, word.getPosition(), XSD.nonNegativeInteger.toString());

        return wordIndividual;
    }

    private static String generateUUID() {
        return OntologyConnector.generateRandomID();
    }

    @Override
    public IText getAnnotatedText() {
        return OntologyText.get(ontologyConnector);
    }

}
