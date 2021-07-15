package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.ontology;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.vocabulary.XSD;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.ontology.OntologyConnector;

public class TextOntologyAdapter {
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
    private ObjectProperty nextProperty;
    private ObjectProperty prevProperty;

    private TextOntologyAdapter(String ontologyPath) {
        ontologyConnector = new OntologyConnector(ontologyPath);
    }

    private TextOntologyAdapter(OntologyConnector ontologyConnector) {
        this.ontologyConnector = ontologyConnector;
    }

    public static TextOntologyAdapter get(String ontologyPath) {
        var toa = new TextOntologyAdapter(ontologyPath);
        toa.init();
        return toa;
    }

    public static TextOntologyAdapter get(OntologyConnector ontologyConnector) {
        var toa = new TextOntologyAdapter(ontologyConnector);
        toa.init();
        return toa;
    }

    private void init() {
        ontologyConnector.addOntologyImport(TEXT_ONTOLOGY_IRI);

        textClass = ontologyConnector.getClass("TextDocument").orElseThrow();
        wordClass = ontologyConnector.getClass("Word").orElseThrow();
        uuidProperty = ontologyConnector.getDataProperty("uuid").orElseThrow();
        textProperty = ontologyConnector.getDataProperty("has text").orElseThrow();
        posProperty = ontologyConnector.getDataProperty("has POS").orElseThrow();
        lemmaProperty = ontologyConnector.getDataProperty("has lemma").orElseThrow();
        positionProperty = ontologyConnector.getDataProperty("has position").orElseThrow();
        sentenceProperty = ontologyConnector.getDataProperty("contained in sentence").orElseThrow();
        wordsProperty = ontologyConnector.getObjectProperty("has words").orElseThrow();
        nextProperty = ontologyConnector.getObjectProperty("has next word").orElseThrow();
        prevProperty = ontologyConnector.getObjectProperty("has previous word").orElseThrow();
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

        createLinkingRelations(wordIndividuals);

        // create the list that is used for the words property
        var olo = ontologyConnector.addList("WordsOf" + name, wordIndividuals);
        var listIndividual = olo.getListIndividual();
        textIndividual.addProperty(wordsProperty, listIndividual);
    }

    private void createLinkingRelations(List<Individual> wordIndividuals) {
        // TODO FIXME: Something is broken here when serialising/writing out the ontology
        // I think it has to do with long strings/names/uris, but I also have no real clue
        // Currently, setting the prev relation fails
        // Idea: scrap the next/prev relation, do that over the OLO next/prev relations instead (at later stage, when
        // needed)
        for (var i = 0; i < wordIndividuals.size(); i++) {
            var curr = wordIndividuals.get(i);

            if (i > 0) {
                var prev = wordIndividuals.get(i - 1);
                // ontologyConnector.addPropertyToIndividual(curr, prevProperty, prev);
            }
            if (i < wordIndividuals.size() - 1) {
                var next = wordIndividuals.get(i + 1);
                ontologyConnector.addPropertyToIndividual(curr, nextProperty, next);
            }
        }
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
        return OntologyConnector.generateRandomUUID();
    }

    public void save(String path) {
        ontologyConnector.save(path);
    }

}
