package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.vocabulary.XSD;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.ontology.OntologyConnector;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;

public final class OntologyTextProvider implements ITextConnector {
    private static final String TEXT_ONTOLOGY_IRI = "https://informalin.github.io/knowledgebases/informalin_base_text.owl";

    private static boolean useCache = true;

    private OntologyConnector ontologyConnector;

    private OntClass textClass;
    private OntClass wordClass;
    private OntClass dependencyClass;

    private OntProperty uuidProperty;
    private OntProperty textProperty;
    private OntProperty posProperty;
    private OntProperty lemmaProperty;
    private OntProperty positionProperty;
    private OntProperty sentenceProperty;
    private OntProperty wordsProperty;
    private OntProperty dependencySourceProperty;
    private OntProperty dependencyTargetProperty;
    private OntProperty dependencyTypeProperty;

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

    /**
     * Sets whether there should be caching used for the annotated text. Does not change the caching for previously
     * returned annotated texts, only for future ones!
     *
     * @param useCache if caching should be used for the annotated text
     */
    public static void enableCache(boolean useCache) {
        OntologyTextProvider.useCache = useCache;
    }

    private void init() {
        ontologyConnector.addOntologyImport(TEXT_ONTOLOGY_IRI);

        textClass = ontologyConnector.getClassByIri(CommonOntologyUris.TEXT_DOCUMENT_CLASS.getUri()).orElseThrow();
        wordClass = ontologyConnector.getClassByIri(CommonOntologyUris.WORD_CLASS.getUri()).orElseThrow();
        dependencyClass = ontologyConnector.getClassByIri(CommonOntologyUris.WORD_DEPENDENCY_CLASS.getUri()).orElseThrow();
        uuidProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.UUID_PROPERTY.getUri()).orElseThrow();
        textProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.TEXT_PROPERTY.getUri()).orElseThrow();
        posProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.POS_PROPERTY.getUri()).orElseThrow();
        lemmaProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.LEMMA_PROPERTY.getUri()).orElseThrow();
        positionProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.POSITION_PROPERTY.getUri()).orElseThrow();
        sentenceProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.SENTENCE_PROPERTY.getUri()).orElseThrow();
        wordsProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.HAS_WORDS_PROPERTY.getUri()).orElseThrow();
        dependencySourceProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.DEP_SOURCE_PROPERTY.getUri()).orElseThrow();
        dependencyTargetProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.DEP_TARGET_PROPERTY.getUri()).orElseThrow();
        dependencyTypeProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.DEP_TYPE_PROPERTY.getUri()).orElseThrow();
    }

    public void addText(IText text) {
        // create text in ontology
        var name = "Text"; // TODO should texts have a name? E.g. the filename etc.?
        var textIndividual = ontologyConnector.addIndividualToClass(name, textClass);
        var uuid = ontologyConnector.getLocalName(textIndividual);
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
        ontologyConnector.addPropertyToIndividual(textIndividual, wordsProperty, listIndividual);
    }

    private Individual addWord(IWord word) {
        var label = word.getText();
        var wordIndividual = ontologyConnector.addIndividualToClass(label, wordClass);
        var uuid = ontologyConnector.getLocalName(wordIndividual);
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

        var sourceName = ontologyConnector.getLabel(source);
        var targetName = ontologyConnector.getLabel(target);
        var depName = depType.name();
        var dependencyLabel = sourceName + "-" + depName + "->" + targetName;
        var dependencyIndividual = ontologyConnector.addIndividualToClass(dependencyLabel, dependencyClass);
        var uid = ontologyConnector.getLocalName(dependencyIndividual);

        ontologyConnector.addPropertyToIndividual(dependencyIndividual, dependencySourceProperty, source);
        ontologyConnector.addPropertyToIndividual(dependencyIndividual, dependencyTargetProperty, target);

        ontologyConnector.addPropertyToIndividual(dependencyIndividual, dependencyTypeProperty, depName);
        ontologyConnector.addPropertyToIndividual(dependencyIndividual, uuidProperty, uid);

    }

    @Override
    public IText getAnnotatedText() {
        if (useCache) {
            return CachedOntologyText.get(ontologyConnector);
        } else {
            return OntologyText.get(ontologyConnector);
        }
    }

}
