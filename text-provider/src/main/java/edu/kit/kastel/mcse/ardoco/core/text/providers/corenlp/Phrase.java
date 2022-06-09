/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.IPhrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.ISentence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.PhraseType;
import edu.stanford.nlp.trees.Tree;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.List;
import java.util.Objects;

class Phrase implements IPhrase {
    private final Tree tree;
    private final ImmutableList<IWord> words;
    private final Sentence sentence;

    private String text = null;

    public Phrase(Tree tree, List<IWord> words) {
        this.tree = tree;
        this.words = Lists.immutable.withAll(words);
        this.sentence = retrieveSentence(words.get(0));
    }

    private Sentence retrieveSentence(IWord firstWord) {
        ISentence iSentence = firstWord.getSentence();
        if (iSentence instanceof Sentence cSentence) {
            return cSentence;
        }
        throw new IllegalStateException("Words do not fit to sentence type");
    }

    @Override
    public int getSentenceNo() {
        return words.get(0).getSentenceNo();
    }

    @Override
    public ISentence getSentence() {
        return sentence;
    }

    @Override
    public String getWord() {
        if (text == null) {
            text = tree.spanString();
        }
        return text;
    }

    @Override
    public PhraseType getPhraseType() {
        String type = tree.label().toString();
        return PhraseType.get(type);
    }

    @Override
    public ImmutableList<IWord> getContainedWords() {
        return words;
    }

    @Override
    public ImmutableList<IPhrase> getSubPhrases() {
        MutableList<IPhrase> subPhrases = Lists.mutable.empty();
        for (var subTree : tree) {
            if (subTree.isPhrasal() && tree.dominates(subTree)) {
                var wordsForPhrase = Sentence.getWordsForPhrase(subTree, this.sentence);
                Phrase currPhrase = new Phrase(subTree, wordsForPhrase);
                subPhrases.add(currPhrase);
            }
        }
        return subPhrases.toImmutable();
    }

    @Override
    public boolean isSuperPhraseOf(IPhrase other) {
        if (other instanceof Phrase otherPhrase) {
            return tree.dominates(otherPhrase.tree);
        } else {
            var currText = getWord();
            var otherText = other.getWord();
            return currText.contains(otherText) && currText.length() != otherText.length();
        }
    }

    @Override
    public boolean isSubPhraseOf(IPhrase other) {
        if (other instanceof Phrase otherPhrase) {
            return otherPhrase.tree.dominates(this.tree);
        } else {
            var currText = getWord();
            var otherText = other.getWord();
            return otherText.contains(currText) && currText.length() != otherText.length();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getSentenceNo(), this.getWord(), this.getPhraseType(), this.getContainedWords().get(0).getPosition());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || this.getClass() != obj.getClass())
            return false;
        Phrase other = (Phrase) obj;
        return Objects.equals(this.getSentenceNo(), other.getSentenceNo()) && Objects.equals(this.getWord(), other.getWord())
                && Objects.equals(this.getPhraseType(), other.getPhraseType())
                && Objects.equals(this.getContainedWords().get(0).getPosition(), other.getContainedWords().get(0).getPosition());
    }
}
