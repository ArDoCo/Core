/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;


import edu.kit.kastel.informalin.ontology.OntologyConnector;
import edu.kit.kastel.mcse.ardoco.core.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.text.ICorefCluster;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.vocabulary.XSD;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

public final class OntologyTextProvider implements ITextConnector {
    private static final String TEXT_ONTOLOGY_IRI = "https://informalin.github.io/knowledgebases/informalin_base_text.owl";

    private static boolean useCache = true;

    private OntologyConnector ontologyConnector;

    private String lastAddedTextName = null;
    private OntResources resources;

    private OntologyTextProvider(OntologyConnector ontologyConnector) {
        this.ontologyConnector = ontologyConnector;
        resources = new OntResources();
    }

    private OntologyTextProvider(String ontologyPath) {
        ontologyConnector = new OntologyConnector(ontologyPath);
        resources = new OntResources();
    }

    public static OntologyTextProvider get(String ontologyPath) {
        return new OntologyTextProvider(ontologyPath);
    }

    public static OntologyTextProvider get(OntologyConnector ontologyConnector) {
        return new OntologyTextProvider(ontologyConnector);
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

    public void addText(IText text) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm");
        LocalDateTime now = LocalDateTime.now();
        lastAddedTextName = dtf.format(now);
        addText(text, lastAddedTextName);
    }

    public void addText(IText text, String textName) {
        // create text in ontology
        lastAddedTextName = "Text_" + textName;
        var textIndividual = ontologyConnector.addIndividualToClass(lastAddedTextName, resources.textClass);
        var uuid = ontologyConnector.getLocalName(textIndividual);
        ontologyConnector.addPropertyToIndividual(textIndividual, resources.uuidProperty, uuid);

        ImmutableList<IWord> words = text.getWords();

        // first add all word individuals
        var wordIndividuals = new ArrayList<Individual>();
        var wordsToIndividuals = new HashMap<IWord, Individual>();
        for (var word : words) {
            var wordIndividual = addWord(word);
            wordIndividuals.add(wordIndividual);
            wordsToIndividuals.put(word, wordIndividual);
        }

        // add dependencies to words.
        // We only add outgoing dependencies as ingoing are the same (but viewed from another perspective)
        for (var word : words) {
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
        var olo = ontologyConnector.addList("WordsOf" + lastAddedTextName, wordIndividuals);
        var listIndividual = olo.getListIndividual();
        ontologyConnector.addPropertyToIndividual(textIndividual, resources.wordsProperty, listIndividual);

        addCoref(text, textIndividual, wordsToIndividuals);
    }

    private void addCoref(IText text, Individual textIndividual, HashMap<IWord, Individual> wordsToIndividuals) {
        // add coref stuff
        var corefClusters = text.getCorefClusters();
        for (var corefCluster : corefClusters) {
            var representativeMention = corefCluster.getRepresentativeMention();
            var corefClusterIndividual = ontologyConnector.addIndividualToClass(representativeMention, resources.corefClusterClass);
            ontologyConnector.addPropertyToIndividual(corefClusterIndividual, resources.uuidProperty, "" + corefCluster.getId());
            ontologyConnector.addPropertyToIndividual(corefClusterIndividual, resources.representativeMentionProperty, representativeMention);
            ontologyConnector.addPropertyToIndividual(textIndividual, resources.hasCorefClusterProperty, corefClusterIndividual);

            var counter = 0;
            for (var mention : corefCluster.getMentions()) {
                var id = corefCluster.getId() + "_" + counter;
                counter += 1;
                var label = ICorefCluster.getTextForMention(mention);

                var mentionIndividual = ontologyConnector.addIndividualToClass(label, resources.corefMentionClass);
                ontologyConnector.addPropertyToIndividual(mentionIndividual, resources.uuidProperty, id);
                ontologyConnector.addPropertyToIndividual(corefClusterIndividual, resources.mentionProperty, mentionIndividual);

                var mentionWordsIndividuals = getMentionWordIndividuals(mention, wordsToIndividuals);
                var mentionOlo = ontologyConnector.addList("WordsOf Mention " + id, mentionWordsIndividuals);
                ontologyConnector.addPropertyToIndividual(mentionIndividual, resources.wordsProperty, mentionOlo.getListIndividual());
            }
        }
    }

    private static MutableList<Individual> getMentionWordIndividuals(ImmutableList<IWord> mention, HashMap<IWord, Individual> wordsToIndividuals) {
        return mention.collect(wordsToIndividuals::get).toList();
    }

    private Individual addWord(IWord word) {
        var label = word.getText();
        var wordIndividual = ontologyConnector.addIndividualToClass(label, resources.wordClass);
        var uuid = ontologyConnector.getLocalName(wordIndividual);
        ontologyConnector.addPropertyToIndividual(wordIndividual, resources.uuidProperty, uuid);

        ontologyConnector.addPropertyToIndividual(wordIndividual, resources.textProperty, word.getText());
        ontologyConnector.addPropertyToIndividual(wordIndividual, resources.posProperty, word.getPosTag().getTag());
        ontologyConnector.addPropertyToIndividual(wordIndividual, resources.lemmaProperty, word.getLemma());
        ontologyConnector.addPropertyToIndividual(wordIndividual, resources.sentenceProperty, word.getSentenceNo(), XSD.nonNegativeInteger.toString());
        ontologyConnector.addPropertyToIndividual(wordIndividual, resources.positionProperty, word.getPosition(), XSD.nonNegativeInteger.toString());

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
        var dependencyIndividual = ontologyConnector.addIndividualToClass(dependencyLabel, resources.dependencyClass);
        var uid = ontologyConnector.getLocalName(dependencyIndividual);

        ontologyConnector.addPropertyToIndividual(dependencyIndividual, resources.dependencySourceProperty, source);
        ontologyConnector.addPropertyToIndividual(dependencyIndividual, resources.dependencyTargetProperty, target);

        ontologyConnector.addPropertyToIndividual(dependencyIndividual, resources.dependencyTypeProperty, depName);
        ontologyConnector.addPropertyToIndividual(dependencyIndividual, resources.uuidProperty, uid);

    }

    @Override
    public IText getAnnotatedText() {
        if (lastAddedTextName == null || lastAddedTextName.isBlank()) {
            if (useCache) {
                return CachedOntologyText.get(ontologyConnector);
            } else {
                return OntologyText.get(ontologyConnector);
            }
        } else if (useCache) {
            return CachedOntologyText.get(ontologyConnector, lastAddedTextName);
        } else {
            return OntologyText.getWithName(ontologyConnector, lastAddedTextName);
        }

    }

    /**
     * Represents a data class that encapsulates certain ontology resources
     *
     * @author Jan Keim
     *
     */
    private final class OntResources {

        private OntClass textClass;
        private OntClass wordClass;
        private OntClass dependencyClass;
        private OntClass corefClusterClass;
        private OntClass corefMentionClass;

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
        private OntProperty mentionProperty;
        private OntProperty representativeMentionProperty;
        private OntProperty hasCorefClusterProperty;

        OntResources() {
            ontologyConnector.addOntologyImport(TEXT_ONTOLOGY_IRI);

            textClass = ontologyConnector.getClassByIri(CommonOntologyUris.TEXT_DOCUMENT_CLASS.getUri()).orElseThrow();
            wordClass = ontologyConnector.getClassByIri(CommonOntologyUris.WORD_CLASS.getUri()).orElseThrow();
            corefClusterClass = ontologyConnector.getClassByIri(CommonOntologyUris.COREF_CLUSTER_CLASS.getUri()).orElseThrow();
            corefMentionClass = ontologyConnector.getClassByIri(CommonOntologyUris.COREF_MENTION_CLASS.getUri()).orElseThrow();
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
            hasCorefClusterProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.HAS_COREF_CLUSTERS.getUri()).orElseThrow();
            mentionProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.HAS_MENTION_PROPERTY.getUri()).orElseThrow();
            representativeMentionProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.REPRESENTATIVE_MENTION_PROPERTY.getUri()).orElseThrow();
        }

    }

}
