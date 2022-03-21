/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Resource;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.informalin.ontology.OntologyConnector;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.ISentence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.POSTag;

public final class OntologyWord implements IWord {
    private OntologyConnector ontologyConnector = null;

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

    private final Individual wordIndividual;
    private final OntologyText text;

    private OntologyWord(OntologyConnector ontologyConnector, Individual wordIndividual, OntologyText text) {
        this.wordIndividual = wordIndividual;
        this.ontologyConnector = ontologyConnector;
        this.text = text;
    }

    static OntologyWord get(OntologyConnector ontologyConnector, Individual wordIndividual, OntologyText text) {
        if (ontologyConnector == null || wordIndividual == null) {
            return null;
        }

        var ow = new OntologyWord(ontologyConnector, wordIndividual, text);
        ow.init();
        return ow;
    }

    private void init() {
        textProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.TEXT_PROPERTY.getUri()).orElseThrow();
        posProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.POS_PROPERTY.getUri()).orElseThrow();
        lemmaProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.LEMMA_PROPERTY.getUri()).orElseThrow();
        positionProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.POSITION_PROPERTY.getUri()).orElseThrow();
        sentenceProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.SENTENCE_PROPERTY.getUri()).orElseThrow();
        hasItem = ontologyConnector.getPropertyByIri(CommonOntologyUris.HAS_ITEM_PROPERTY.getUri()).orElseThrow();
        hasNext = ontologyConnector.getPropertyByIri(CommonOntologyUris.HAS_NEXT_PROPERTY.getUri()).orElseThrow();
        hasPrevious = ontologyConnector.getPropertyByIri(CommonOntologyUris.HAS_PREVIOUS_PROPERTY.getUri()).orElseThrow();
        dependencySourceProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.DEP_SOURCE_PROPERTY.getUri()).orElseThrow();
        dependencyTargetProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.DEP_TARGET_PROPERTY.getUri()).orElseThrow();
        dependencyTypeProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.DEP_TYPE_PROPERTY.getUri()).orElseThrow();
    }

    @Override
    public int getSentenceNo() {
        var optInt = ontologyConnector.getPropertyIntValue(wordIndividual, sentenceProperty);
        return optInt.orElse(-1);
    }

    @Override
    public ISentence getSentence() {
        return text.getSentences().get(getSentenceNo());
    }

    @Override
    public String getText() {
        var optString = ontologyConnector.getPropertyStringValue(wordIndividual, textProperty);
        return optString.orElse(null);
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
                return get(ontologyConnector, prevIndividual.get(), text);
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
                return get(ontologyConnector, nextIndividual.get(), text);
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
        var hasItemSubjects = ontologyConnector.getSubjectsOf(hasItem, wordIndividual);
        for (var subject : hasItemSubjects) {
            var individualOpt = ontologyConnector.transformIntoIndividual(subject);
            if (individualOpt.isPresent()) {
                var individual = individualOpt.get();
                var label = individual.getLabel(null);
                if (!label.contains("Mention")) {
                    return individual;
                }
            }
        }
        throw new IllegalStateException("Word should be contained in a slot of a word list, but could not find the corresponding slot.");
    }

    @Override
    public int getPosition() {
        var optInt = ontologyConnector.getPropertyIntValue(wordIndividual, positionProperty);
        return optInt.orElse(-1);
    }

    @Override
    public String getLemma() {
        var optString = ontologyConnector.getPropertyStringValue(wordIndividual, lemmaProperty);
        return optString.orElse(null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPosition(), getSentenceNo(), getText());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        OntologyWord other = (OntologyWord) obj;
        return getPosition() == other.getPosition() && getSentenceNo() == other.getSentenceNo() && Objects.equals(getText(), other.getText());
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
            words.add(OntologyWord.get(ontologyConnector, individual, text));
        }
        return words.toImmutable();
    }

    private List<Individual> extractIndividualsInRelation(List<Individual> filteredDependencies, OntProperty property) {
        var targets = new ArrayList<Individual>();
        for (var dependency : filteredDependencies) {
            var targetNode = ontologyConnector.getPropertyValue(dependency, property);
            var target = ontologyConnector.transformIntoIndividual(targetNode);
            target.ifPresent(targets::add);
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

    @Override
    public String toString() {
        return String.format("%s (pos=%s, p=%d, s=%d)", getText(), getPosTag().getTag(), getPosition(), getSentenceNo());
    }
}
