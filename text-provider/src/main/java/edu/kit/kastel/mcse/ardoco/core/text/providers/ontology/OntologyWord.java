package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.POSTag;
import edu.kit.kastel.mcse.ardoco.core.ontology.OntologyConnector;
import edu.kit.kastel.mcse.ardoco.core.ontology.OntologyInterface;

public class OntologyWord implements IWord {
    private static Logger logger = LogManager.getLogger(OntologyWord.class);

    private static OntologyConnector ontologyConnector = null;

    private static String textPropertyUri = "https://informalin.github.io/knowledgebases/informalin_base_text.owl#OWLDataProperty_2abdbab4_07a1_44e4_86b4_1dd2db60d093";
    private static String posPropertyUri = "https://informalin.github.io/knowledgebases/informalin_base_text.owl#OWLDataProperty_4a42b4d3_f585_4d3a_ac8d_12efcd2f41ed";
    private static String lemmaPropertyUri = "https://informalin.github.io/knowledgebases/informalin_base_text.owl#OWLDataProperty_8641228e_89c1_4094_8770_d6db4cff934d";
    private static String positionPropertyUri = "https://informalin.github.io/knowledgebases/informalin_base_text.owl#OWLDataProperty_24a1fab1_8d82_4a64_a569_26181935ae92";
    private static String sentencePropertyUri = "https://informalin.github.io/knowledgebases/informalin_base_text.owl#OWLDataProperty_cca1bf08_930a_4c38_b1b4_4b127db235a3";
    private static String hasItemPropertyUri = "https://informalin.github.io/knowledgebases/external/olo/orderedlistontology.owl#item";
    private static String hasNextPropertyUri = "https://informalin.github.io/knowledgebases/external/olo/orderedlistontology.owl#next";
    private static String hasPreviousPropertyUri = "https://informalin.github.io/knowledgebases/external/olo/orderedlistontology.owl#previous";
    private static String depSourcePropertyUri = "https://informalin.github.io/knowledgebases/informalin_base_text.owl#OWLObjectProperty_338dfb91_e78b_4145_bf8c_a952e927b6e9";
    private static String depTargetPropertyUri = "https://informalin.github.io/knowledgebases/informalin_base_text.owl#OWLObjectProperty_82e64c17_5998_4d50_941f_a2b859c1a95b";
    private static String depTypePropertyUri = "https://informalin.github.io/knowledgebases/informalin_base_text.owl#OWLAnnotationProperty_79e191d9_7e85_461e_ae42_62df5078719b";

    private OntProperty textProperty = null;
    private OntProperty posProperty = null;
    private OntProperty lemmaProperty = null;
    private OntProperty positionProperty = null;
    private OntProperty sentenceProperty = null;
    private OntProperty hasItem = null;
    private OntProperty hasNext = null;
    private OntProperty hasPrevious = null;
    private OntProperty dependencySourceProperty = null;
    private OntProperty dependencyTargetProperty = null;
    private OntProperty dependencyTypeProperty = null;

    private Individual wordIndividual;

    private OntologyWord(Individual wordIndividual) {
        this.wordIndividual = wordIndividual;
    }

    protected static OntologyWord get(OntologyConnector ontologyConnector, Individual wordIndividual) {
        if (ontologyConnector == null || wordIndividual == null) {
            return null;
        }
        if (OntologyWord.ontologyConnector == null) {
            OntologyWord.ontologyConnector = ontologyConnector;
        }
        if (!OntologyWord.ontologyConnector.equals(ontologyConnector)) {
            logger.warn("There is a change in the OntologyConnector. This might cause illegal states!");
            OntologyWord.ontologyConnector = ontologyConnector;
        }

        var ow = new OntologyWord(wordIndividual);
        ow.init(ontologyConnector);
        return ow;
    }

    private void init(OntologyInterface ontologyConnector) {
        textProperty = ontologyConnector.getPropertyByIri(textPropertyUri).orElseThrow();
        posProperty = ontologyConnector.getPropertyByIri(posPropertyUri).orElseThrow();
        lemmaProperty = ontologyConnector.getPropertyByIri(lemmaPropertyUri).orElseThrow();
        positionProperty = ontologyConnector.getPropertyByIri(positionPropertyUri).orElseThrow();
        sentenceProperty = ontologyConnector.getPropertyByIri(sentencePropertyUri).orElseThrow();
        hasItem = ontologyConnector.getPropertyByIri(hasItemPropertyUri).orElseThrow();
        hasNext = ontologyConnector.getPropertyByIri(hasNextPropertyUri).orElseThrow();
        hasPrevious = ontologyConnector.getPropertyByIri(hasPreviousPropertyUri).orElseThrow();
        dependencySourceProperty = ontologyConnector.getPropertyByIri(depSourcePropertyUri).orElseThrow();
        dependencyTargetProperty = ontologyConnector.getPropertyByIri(depTargetPropertyUri).orElseThrow();
        dependencyTypeProperty = ontologyConnector.getPropertyByIri(depTypePropertyUri).orElseThrow();
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
        var prevSlotNode = ontologyConnector.getPropertyValue(slot, hasPrevious);
        var prevSlotIndividual = ontologyConnector.transformIntoIndividual(prevSlotNode);
        if (prevSlotIndividual.isPresent()) {
            var prevIndividual = extractItemOutOfSlot(prevSlotIndividual.get());
            if (prevIndividual.isPresent()) {
                return get(ontologyConnector, prevIndividual.get());
            }
        }
        return null;
    }

    @Override
    public IWord getNextWord() {
        Individual slot = getSlot();
        var nextSlotNode = ontologyConnector.getPropertyValue(slot, hasNext);
        var nextSlotIndividual = ontologyConnector.transformIntoIndividual(nextSlotNode);
        if (nextSlotIndividual.isPresent()) {
            var nextIndividual = extractItemOutOfSlot(nextSlotIndividual.get());
            if (nextIndividual.isPresent()) {
                return get(ontologyConnector, nextIndividual.get());
            }
        }
        return null;
    }

    private static Optional<Individual> extractItemOutOfSlot(Individual slot) {
        if (slot == null) {
            return Optional.empty();
        }
        var itemNode = ontologyConnector.getPropertyValue(slot, hasItem);
        return ontologyConnector.transformType(itemNode, Individual.class);
    }

    private Individual getSlot() {
        var optSlot = ontologyConnector.getFirstSubjectOf(hasItem, wordIndividual);
        if (optSlot.isPresent()) {
            var slot = optSlot.get();
            return ontologyConnector.transformIntoIndividual(slot).orElse(null);
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
    public int hashCode() {
        final var prime = 31;
        var result = 1;
        result = prime * result + getPosition();
        result = prime * result + getSentenceNo();
        var text = getText();
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        OntologyWord other = (OntologyWord) obj;
        if (getPosition() != other.getPosition()) {
            return false;
        }
        if (getSentenceNo() != other.getSentenceNo()) {
            return false;
        }
        var text = getText();
        var otherText = other.getText();
        if (text == null) {
            if (otherText != null) {
                return false;
            }
        } else if (!text.equals(otherText)) {
            return false;
        }
        return true;
    }

    /**
     * Outgoing dependencies
     */
    @Override
    public ImmutableList<IWord> getWordsThatAreDependencyOfThis(DependencyTag dependencyTag) {
        if (dependencyTag == null) {
            return Lists.immutable.empty();
        }
        List<Resource> dependencies = ontologyConnector.getSubjectsOf(dependencySourceProperty, wordIndividual);
        var filteredDependencies = filterDependencyResourcesByType(dependencyTag, dependencies);
        var targets = extractIndividualsInRelation(filteredDependencies, dependencyTargetProperty);
        return createWordsFromIndividuals(targets);
    }

    /**
     * Incoming dependencies
     */
    @Override
    public ImmutableList<IWord> getWordsThatAreDependentOnThis(DependencyTag dependencyTag) {
        if (dependencyTag == null) {
            return Lists.immutable.empty();
        }
        List<Resource> dependencies = ontologyConnector.getSubjectsOf(dependencyTargetProperty, wordIndividual);
        var filteredDependencies = filterDependencyResourcesByType(dependencyTag, dependencies);
        var targets = extractIndividualsInRelation(filteredDependencies, dependencySourceProperty);
        return createWordsFromIndividuals(targets);
    }

    private static ImmutableList<IWord> createWordsFromIndividuals(List<Individual> individuals) {
        MutableList<IWord> words = Lists.mutable.empty();
        for (var individual : individuals) {
            words.add(OntologyWord.get(ontologyConnector, individual));
        }
        return words.toImmutable();
    }

    private static List<Individual> extractIndividualsInRelation(List<Individual> filteredDependencies, OntProperty property) {
        var targets = new ArrayList<Individual>();
        for (var dependency : filteredDependencies) {
            var targetNode = ontologyConnector.getPropertyValue(dependency, property);
            var target = ontologyConnector.transformIntoIndividual(targetNode);
            if (target.isPresent()) {
                targets.add(target.get());
            }
        }
        return targets;
    }

    private static List<Individual> filterDependencyResourcesByType(DependencyTag dependencyTag, List<Resource> dependencies) {
        var filteredDependencies = new ArrayList<Individual>();
        for (var dependency : dependencies) {
            var depIndividual = ontologyConnector.transformIntoIndividual(dependency);
            if (depIndividual.isEmpty()) {
                continue;
            }
            Individual dependencyIndividual = depIndividual.get();
            var depType = ontologyConnector.getPropertyStringValue(dependencyIndividual, dependencyTypeProperty);
            if (depType.isPresent() && dependencyTag.name().equalsIgnoreCase(depType.get())) {
                filteredDependencies.add(dependencyIndividual);
            }
        }
        return filteredDependencies;
    }

}
