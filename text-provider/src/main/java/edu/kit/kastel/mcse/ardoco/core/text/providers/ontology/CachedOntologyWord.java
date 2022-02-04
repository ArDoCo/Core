/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.text.ISentence;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.text.POSTag;

public final class CachedOntologyWord implements IWord {
    private static final Map<IWord, CachedOntologyWord> cache = new HashMap<>();

    private IWord ontologyWord;

    private int sentenceNo = -1;
    private ISentence sentence = null;
    private String text = null;
    private POSTag posTag = null;
    private IWord preWord = null;
    private IWord nextWord = null;
    private int position = -1;
    private String lemma = null;
    private Map<DependencyTag, ImmutableList<IWord>> outDependencies = Maps.mutable.empty();
    private Map<DependencyTag, ImmutableList<IWord>> inDependencies = Maps.mutable.empty();

    private CachedOntologyWord(IWord ontologyWord) {
        if (ontologyWord == null) {
            throw new IllegalArgumentException("Word cannot be null");
        }
        this.ontologyWord = ontologyWord;
    }

    static CachedOntologyWord get(IWord ontologyWord) {
        synchronized (cache) {
            return cache.computeIfAbsent(ontologyWord, CachedOntologyWord::new);
        }
    }

    @Override
    public synchronized int getSentenceNo() {
        if (sentenceNo <= -1) {
            sentenceNo = ontologyWord.getSentenceNo();
        }
        return sentenceNo;
    }

    @Override
    public synchronized ISentence getSentence() {
        if (sentence == null) {
            sentence = ontologyWord.getSentence();
        }
        return sentence;
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
            var newPreWord = ontologyWord.getPreWord();
            if (newPreWord == null) {
                return null;
            }
            preWord = get(newPreWord);
        }
        return preWord;
    }

    @Override
    public synchronized IWord getNextWord() {
        if (nextWord == null) {
            var newNextWord = ontologyWord.getNextWord();
            if (newNextWord == null) {
                return null;
            }
            nextWord = get(newNextWord);
        }
        return nextWord;
    }

    @Override
    public synchronized int getPosition() {
        if (position <= -1) {
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
        return inDependencies.computeIfAbsent(dependencyTag, dt -> ontologyWord.getWordsThatAreDependencyOfThis(dt).collect(CachedOntologyWord::get));
    }

    @Override
    public synchronized ImmutableList<IWord> getWordsThatAreDependentOnThis(DependencyTag dependencyTag) {
        return outDependencies.computeIfAbsent(dependencyTag, dt -> ontologyWord.getWordsThatAreDependentOnThis(dt).collect(CachedOntologyWord::get));
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
        CachedOntologyWord other = (CachedOntologyWord) obj;
        return getPosition() == other.getPosition() && getSentenceNo() == other.getSentenceNo() && Objects.equals(getText(), other.getText());
    }

    @Override
    public String toString() {
        return String.format("%s (pos=%s, p=%d, s=%d)", getText(), getPosTag().getTag(), getPosition(), getSentenceNo());
    }

}
