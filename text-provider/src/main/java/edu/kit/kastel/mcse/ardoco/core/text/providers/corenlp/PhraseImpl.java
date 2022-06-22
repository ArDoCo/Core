/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp;

import java.util.List;

import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.stanford.nlp.trees.Tree;

class PhraseImpl implements Phrase {
    private final Tree tree;
    private final List<Word> words;
    private SentenceImpl sentence;

    private String text = null;

    public PhraseImpl(Tree tree, List<Word> words) {
        this.tree = tree;
        this.words = words;
        this.sentence = retrieveSentence(words.get(0));
    }

    private SentenceImpl retrieveSentence(Word firstWord) {
        Sentence iSentence = firstWord.getSentence();
        if (iSentence instanceof SentenceImpl cSentence) {
            return cSentence;
        }
        throw new IllegalStateException("Words do not fit to sentence type");
    }

    @Override
    public int getSentenceNo() {
        return words.get(0).getSentenceNo();
    }

    @Override
    public Sentence getSentence() {
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
    public List<Word> getContainedWords() {
        return words;
    }

    @Override
    public List<Phrase> getSubPhrases() {
        List<Phrase> subPhrases = Lists.mutable.empty();
        for (var subTree : tree) {
            if (subTree.isPhrasal() && tree.dominates(subTree)) {
                var wordsForPhrase = SentenceImpl.getWordsForPhrase(subTree, this.sentence);
                PhraseImpl currPhrase = new PhraseImpl(subTree, wordsForPhrase);
                subPhrases.add(currPhrase);
            }
        }
        return subPhrases;
    }

    @Override
    public boolean isSuperPhraseOf(Phrase other) {
        if (other instanceof PhraseImpl otherPhrase) {
            return tree.dominates(otherPhrase.tree);
        } else {
            var currText = getText();
            var otherText = other.getText();
            return currText.contains(otherText) && currText.length() != otherText.length();
        }
    }

    @Override
    public boolean isSubPhraseOf(Phrase other) {
        if (other instanceof PhraseImpl otherPhrase) {
            return otherPhrase.tree.dominates(this.tree);
        } else {
            var currText = getText();
            var otherText = other.getText();
            return otherText.contains(currText) && currText.length() != otherText.length();
        }
    }
}
