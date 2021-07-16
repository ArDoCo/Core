package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.vocabulary.XSD;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.ontology.OntologyConnector;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;

public class OntologyTextProvider implements ITextConnector {
    private static final String TEXT_ONTOLOGY_IRI = "https://informalin.github.io/knowledgebases/informalin_base_text.owl";

    private OntologyConnector ontologyConnector;

    private OntClass textClass;
    private OntClass wordClass;
    private OntClass dependencyClass;

    private DatatypeProperty uuidProperty;
    private DatatypeProperty textProperty;
    private DatatypeProperty posProperty;
    private DatatypeProperty lemmaProperty;
    private DatatypeProperty positionProperty;
    private DatatypeProperty sentenceProperty;
    private ObjectProperty wordsProperty;
    private ObjectProperty dependencySourceProperty;
    private ObjectProperty dependencyTargetProperty;
    private AnnotationProperty dependencyTypeProperty;

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

        textClass = ontologyConnector.getClass("TextDocument").orElseThrow();
        wordClass = ontologyConnector.getClass("Word").orElseThrow();
        dependencyClass = ontologyConnector.getClass("WordDependency").orElseThrow();
        uuidProperty = ontologyConnector.getDataProperty("uuid").orElseThrow();
        textProperty = ontologyConnector.getDataProperty("has text").orElseThrow();
        posProperty = ontologyConnector.getDataProperty("has POS").orElseThrow();
        lemmaProperty = ontologyConnector.getDataProperty("has lemma").orElseThrow();
        positionProperty = ontologyConnector.getDataProperty("has position").orElseThrow();
        sentenceProperty = ontologyConnector.getDataProperty("contained in sentence").orElseThrow();
        wordsProperty = ontologyConnector.getObjectProperty("has words").orElseThrow();
        dependencySourceProperty = ontologyConnector.getObjectProperty("has source").orElseThrow();
        dependencyTargetProperty = ontologyConnector.getObjectProperty("has target").orElseThrow();
        dependencyTypeProperty = ontologyConnector.getAnnotationProperty("dependencyType").orElseThrow();

    }

    public void addText(IText text) {
        // create text in ontology
        var name = "Text"; // TODO should texts have a name? E.g. the filename etc.?
        var textIndividual = ontologyConnector.addIndividualToClass(name, textClass);
        var uuid = textIndividual.getLocalName();
        ontologyConnector.addPropertyToIndividual(textIndividual, uuidProperty, uuid);

        // add word individuals
        var wordIndividuals = new ArrayList<Individual>();
        var wordsToIndividuals = new HashMap<IWord, Individual>();
        for (var word : text.getWords()) {
            var wordIndividual = addWord(word);
            wordIndividuals.add(wordIndividual);
            wordsToIndividuals.put(word, wordIndividual);
        }

        // add dependencies to words.
        // We only add outgoing dependencies as ingoing are the same (but viewed from another perspective)
        for (var word : text.getWords()) {
            var wordIndividual = wordsToIndividuals.get(word);
            for (var dependencyType : DependencyTag.values()) {
                var outDependencies = word.getWordsThatAreDependencyOfThis(dependencyType);
                for (var outDep : outDependencies) {
                    var outWordIndividual = wordsToIndividuals.get(outDep);
                    addDependencyBetweenWords(wordIndividual, dependencyType, outWordIndividual);
                }
            }
        }

        // create the list that is used for the words property
        var olo = ontologyConnector.addList("WordsOf" + name, wordIndividuals);
        var listIndividual = olo.getListIndividual();
        textIndividual.addProperty(wordsProperty, listIndividual);
    }

    private Individual addWord(IWord word) {
        var label = word.getText();
        var wordIndividual = ontologyConnector.addIndividualToClass(label, wordClass);
        var uuid = wordIndividual.getLocalName();
        ontologyConnector.addPropertyToIndividual(wordIndividual, uuidProperty, uuid);

        ontologyConnector.addPropertyToIndividual(wordIndividual, textProperty, word.getText());
        ontologyConnector.addPropertyToIndividual(wordIndividual, posProperty, word.getPosTag().getTag());
        ontologyConnector.addPropertyToIndividual(wordIndividual, lemmaProperty, word.getLemma());
        ontologyConnector.addPropertyToIndividual(wordIndividual, sentenceProperty, word.getSentenceNo(), XSD.nonNegativeInteger.toString());
        ontologyConnector.addPropertyToIndividual(wordIndividual, positionProperty, word.getPosition(), XSD.nonNegativeInteger.toString());

        return wordIndividual;
    }

    private void addDependencyBetweenWords(Individual source, DependencyTag depType, Individual target) {
        if (source == null || target == null) {
            return;
        }

        var sourceName = source.getLabel(null);
        var targetName = target.getLabel(null);
        var depName = depType.name();
        var dependencyLabel = sourceName + "-" + depName + "->" + targetName;
        var dependencyIndividual = ontologyConnector.addIndividualToClass(dependencyLabel, dependencyClass);
        var uid = dependencyIndividual.getLocalName();

        ontologyConnector.addPropertyToIndividual(dependencyIndividual, dependencySourceProperty, source);
        ontologyConnector.addPropertyToIndividual(dependencyIndividual, dependencyTargetProperty, target);

        ontologyConnector.addPropertyToIndividual(dependencyIndividual, dependencyTypeProperty, depName);
        ontologyConnector.addPropertyToIndividual(dependencyIndividual, uuidProperty, uid);

    }

    @Override
    public IText getAnnotatedText() {
        return OntologyText.get(ontologyConnector);
    }

}
