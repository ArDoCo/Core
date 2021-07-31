package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Resource;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.POSTag;
import edu.kit.kastel.mcse.ardoco.core.ontology.OntologyConnector;

public class OntologyWord implements IWord {
    private OntologyConnector ontologyConnector;
    private Individual wordIndividual;

    private DatatypeProperty textProperty = null;
    private DatatypeProperty posProperty = null;
    private DatatypeProperty lemmaProperty = null;
    private DatatypeProperty positionProperty = null;
    private DatatypeProperty sentenceProperty = null;
    private OntProperty hasItem = null;
    private ObjectProperty hasNext = null;
    private ObjectProperty hasPrevious = null;
    private ObjectProperty dependencySourceProperty = null;
    private ObjectProperty dependencyTargetProperty = null;
    private AnnotationProperty dependencyTypeProperty = null;

    private OntologyWord(OntologyConnector ontologyConnector, Individual wordIndividual) {
        this.ontologyConnector = ontologyConnector;
        this.wordIndividual = wordIndividual;
    }

    protected static OntologyWord get(OntologyConnector ontologyConnector, Individual wordIndividual) {
        if (ontologyConnector == null || wordIndividual == null) {
            return null;
        }
        var ontoWord = new OntologyWord(ontologyConnector, wordIndividual);
        ontoWord.init(ontologyConnector);
        return ontoWord;
    }

    private void init(OntologyConnector ontologyConnector) {
        textProperty = ontologyConnector.getDataProperty("has text").orElseThrow();
        posProperty = ontologyConnector.getDataProperty("has POS").orElseThrow();
        lemmaProperty = ontologyConnector.getDataProperty("has lemma").orElseThrow();
        positionProperty = ontologyConnector.getDataProperty("has position").orElseThrow();
        sentenceProperty = ontologyConnector.getDataProperty("contained in sentence").orElseThrow();
        hasItem = ontologyConnector.getProperty("has item").orElseThrow();
        hasNext = ontologyConnector.getObjectProperty("has next").orElseThrow();
        hasPrevious = ontologyConnector.getObjectProperty("has previous").orElseThrow();
        dependencySourceProperty = ontologyConnector.getObjectProperty("has source").orElseThrow();
        dependencyTargetProperty = ontologyConnector.getObjectProperty("has target").orElseThrow();
        dependencyTypeProperty = ontologyConnector.getAnnotationProperty("dependencyType").orElseThrow();
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

    private Optional<Individual> extractItemOutOfSlot(Individual slot) {
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

    private ImmutableList<IWord> createWordsFromIndividuals(List<Individual> individuals) {
        MutableList<IWord> words = Lists.mutable.empty();
        for (var individual : individuals) {
            words.add(OntologyWord.get(ontologyConnector, individual));
        }
        return words.toImmutable();
    }

    private List<Individual> extractIndividualsInRelation(List<Individual> filteredDependencies, OntProperty property) {
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

    private List<Individual> filterDependencyResourcesByType(DependencyTag dependencyTag, List<Resource> dependencies) {
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
