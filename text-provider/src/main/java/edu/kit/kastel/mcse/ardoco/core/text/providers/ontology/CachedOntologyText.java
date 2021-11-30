/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;


import edu.kit.kastel.informalin.ontology.OntologyConnector;
import edu.kit.kastel.informalin.ontology.OntologyInterface;
import edu.kit.kastel.informalin.ontology.OrderedOntologyList;
import edu.kit.kastel.mcse.ardoco.core.text.ICorefCluster;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;
import java.util.Objects;
import java.util.Optional;
import org.apache.jena.ontology.Individual;
import org.eclipse.collections.api.list.ImmutableList;

public class CachedOntologyText implements IText {

    private OrderedOntologyList textList = null;
    private ImmutableList<IWord> words = null;
    private ImmutableList<ICorefCluster> corefClusters = null;

    private OntologyText ontologyText;

    protected CachedOntologyText(OntologyText ontologyText) {
        this.ontologyText = ontologyText;
    }

    protected static CachedOntologyText get(OntologyConnector ontologyConnector, Individual textIndividual) {
        var ontologyText = OntologyText.get(ontologyConnector, textIndividual);
        return new CachedOntologyText(ontologyText);
    }

    protected static CachedOntologyText get(OntologyConnector ontologyConnector) {
        var ontologyText = OntologyText.get(ontologyConnector);
        return new CachedOntologyText(ontologyText);
    }

    protected static CachedOntologyText get(OntologyConnector ontologyConnector, String name) {
        var ontologyText = OntologyText.getWithName(ontologyConnector, name);
        return new CachedOntologyText(ontologyText);
    }

    protected static Optional<Individual> getTextIndividual(OntologyInterface ontologyConnector) {
        var textIndividuals = ontologyConnector.getIndividualsOfClass("TextDocument");
        if (textIndividuals.isEmpty()) {
            return Optional.empty();
        } else {
            // Assumption: We only have one text right now and always retrieve only the first one
            return Optional.of(textIndividuals.get(0));
        }
    }

    @Override
    public IWord getFirstWord() {
        return CachedOntologyWord.get(ontologyText.getFirstWord());
    }

    @Override
    public int getLength() {
        return getOrderedOntologyListOfText().size();
    }

    @Override
    public synchronized ImmutableList<IWord> getWords() {
        if (words != null && !words.isEmpty()) {
            return words;
        }
        var ontoWords = ontologyText.getWords();
        words = ontoWords.collect(CachedOntologyWord::get);
        return words;
    }

    private synchronized OrderedOntologyList getOrderedOntologyListOfText() {
        if (textList != null) {
            return textList;
        }
        textList = ontologyText.getOrderedOntologyListOfText();
        return textList;
    }

    @Override
    public synchronized ImmutableList<ICorefCluster> getCorefClusters() {
        if (corefClusters == null) {
            corefClusters = ontologyText.getCorefClusters().collect(CachedOntologyCorefCluster::get);
        }
        return corefClusters;
    }

    public void setDirty() {
        textList = null;
        words = null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ontologyText);
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
        CachedOntologyText other = (CachedOntologyText) obj;
        return Objects.equals(ontologyText, other.ontologyText);
    }
}
