/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp;

import java.util.List;

import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.IPhrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.ISentence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.PhraseType;
import edu.stanford.nlp.trees.Tree;

class Phrase implements IPhrase {
    private final Tree tree;
    private final List<IWord> words;
    private Sentence sentence;

    private String text = null;

    public Phrase(Tree tree, List<IWord> words) {
        this.tree = tree;
        this.words = words;
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
    public String getText() {
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
    public List<IWord> getContainedWords() {
        return words;
    }

    @Override
    public List<IPhrase> getSubPhrases() {
        List<IPhrase> subPhrases = Lists.mutable.empty();
        for (var subTree : tree) {
            if (subTree.isPhrasal() && tree.dominates(subTree)) {
                var wordsForPhrase = Sentence.getWordsForPhrase(subTree, this.sentence);
                Phrase currPhrase = new Phrase(subTree, wordsForPhrase);
                subPhrases.add(currPhrase);
            }
        }
        return subPhrases;
    }

    @Override
    public boolean isSuperPhraseOf(IPhrase other) {
        if (other instanceof Phrase otherPhrase) {
            return tree.dominates(otherPhrase.tree);
        } else {
            var currText = getText();
            var otherText = other.getText();
            if (currText.contains(otherText) && currText.length() != otherText.length()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSubPhraseOf(IPhrase other) {
        if (other instanceof Phrase otherPhrase) {
            return otherPhrase.tree.dominates(this.tree);
        } else {
            var currText = getText();
            var otherText = other.getText();
            if (otherText.contains(currText) && currText.length() != otherText.length()) {
                return true;
            }
        }
        return false;
    }
}
