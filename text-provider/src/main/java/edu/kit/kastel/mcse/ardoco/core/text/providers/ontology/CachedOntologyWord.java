package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;

import java.util.Map;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.POSTag;

public class CachedOntologyWord implements IWord {

    private OntologyWord ontologyWord;

    private int sentenceNo = -1;
    private String text = null;
    private POSTag posTag = null;
    private IWord preWord = null;
    private IWord nextWord = null;
    private int position = -1;
    private String lemma = null;
    private Map<DependencyTag, ImmutableList<IWord>> outDependencies = Maps.mutable.empty();
    private Map<DependencyTag, ImmutableList<IWord>> inDependencies = Maps.mutable.empty();

    protected CachedOntologyWord(OntologyWord ontologyWord) {
        this.ontologyWord = ontologyWord;
    }

    @Override
    public synchronized int getSentenceNo() {
        if (sentenceNo == -1) {
            sentenceNo = ontologyWord.getSentenceNo();
        }
        return sentenceNo;
    }

    @Override
    public synchronized String getText() {
        if (text == null) {
            text = ontologyWord.getText();
        }
        return text;
    }

    @Override
    public synchronized POSTag getPosTag() {
        if (posTag == null) {
            posTag = ontologyWord.getPosTag();
        }
        return posTag;
    }

    @Override
    public synchronized IWord getPreWord() {
        if (preWord == null) {
            preWord = ontologyWord.getPreWord();
        }
        return preWord;
    }

    @Override
    public synchronized IWord getNextWord() {
        if (nextWord == null) {
            nextWord = ontologyWord.getNextWord();
        }
        return nextWord;
    }

    @Override
    public synchronized int getPosition() {
        if (position == -1) {
            position = ontologyWord.getPosition();
        }
        return position;
    }

    @Override
    public synchronized String getLemma() {
        if (lemma == null) {
            lemma = ontologyWord.getLemma();
        }
        return lemma;
    }

    @Override
    public synchronized ImmutableList<IWord> getWordsThatAreDependencyOfThis(DependencyTag dependencyTag) {
        return inDependencies.computeIfAbsent(dependencyTag, dt -> ontologyWord.getWordsThatAreDependencyOfThis(dt));
    }

    @Override
    public synchronized ImmutableList<IWord> getWordsThatAreDependentOnThis(DependencyTag dependencyTag) {
        return outDependencies.computeIfAbsent(dependencyTag, dt -> ontologyWord.getWordsThatAreDependentOnThis(dt));
    }

    public void setDirty() {
        sentenceNo = -1;
        text = null;
        posTag = null;
        preWord = null;
        nextWord = null;
        position = -1;
        lemma = null;
        outDependencies = Maps.mutable.empty();
        inDependencies = Maps.mutable.empty();
    }

}
