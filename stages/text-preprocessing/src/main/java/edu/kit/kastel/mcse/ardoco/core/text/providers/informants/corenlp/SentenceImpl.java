/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp;

import java.util.List;
import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.Tree;

class SentenceImpl implements Sentence {
    private static final Logger logger = LoggerFactory.getLogger(SentenceImpl.class);

    private MutableList<Word> words = Lists.mutable.empty();
    private MutableList<Phrase> phrases = Lists.mutable.empty();

    private TextImpl parent;
    private final transient CoreSentence coreSentence;
    private int sentenceNumber;

    private final String text;

    public SentenceImpl(CoreSentence coreSentence, int sentenceNumber, TextImpl parent) {
        this.coreSentence = coreSentence;
        this.sentenceNumber = sentenceNumber;
        this.parent = parent;
        this.text = coreSentence.text();
    }

    @Override
    public int getSentenceNumber() {
        return sentenceNumber;
    }

    @Override
    public ImmutableList<Word> getWords() {
        if (words.isEmpty()) {
            this.words = Lists.mutable.ofAll(parent.getWords().select(w -> w.getSentenceNo() == sentenceNumber));
        }
        return words.toImmutable();
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public ImmutableList<Phrase> getPhrases() {
        if (phrases.isEmpty()) {
            MutableList<Phrase> newPhrases = Lists.mutable.empty();
            var constituencyParse = this.coreSentence.constituencyParse();
            for (var phrase : constituencyParse) {
                if (phrase.isPhrasal()) {
                    ImmutableList<Word> wordsForPhrase = Lists.immutable.withAll(getWordsForPhrase(phrase));
                    Phrase currPhrase = new PhraseImpl(phrase, wordsForPhrase, this);
                    newPhrases.add(currPhrase);
                }
            }
            phrases = newPhrases;
        }

        return phrases.toImmutable();
    }

    @Override
    public void addPhrase(Phrase phrase) {
        phrases.add(phrase);
    }

    protected List<Word> getWordsForPhrase(Tree phrase) {
        List<Word> phraseWords = Lists.mutable.empty();
        var coreLabels = phrase.taggedLabeledYield();
        var index = findIndexOfFirstWordInPhrase(coreLabels.get(0), this);
        logger.debug("phrase starting position: {}", index);
        for (int wordIndexInSentence = 0; wordIndexInSentence < coreLabels.size(); wordIndexInSentence++) {
            var phraseWord = parent.getWords().get(index++);
            phraseWords.add(phraseWord);
        }
        return phraseWords;
    }

    private static int findIndexOfFirstWordInPhrase(CoreLabel firstWordLabel, SentenceImpl sentence) {
        for (var word : sentence.getWords()) {
            if (word instanceof WordImpl sentenceWord) {
                var wordBegin = sentenceWord.getBeginCharPosition();
                int firstWordLabelBegin = firstWordLabel.beginPosition();
                if (wordBegin == firstWordLabelBegin) {
                    return sentenceWord.getPosition();
                }
            }
        }
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof Sentence sentence) {
            return isEqualTo(sentence);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSentenceNumber(), getText());
    }

    public SemanticGraph dependencyParse() {
        return this.coreSentence.dependencyParse();
    }
}
