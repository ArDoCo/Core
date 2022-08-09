/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp;

import java.util.List;
import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.trees.Tree;

class SentenceImpl implements Sentence {
    private static final Logger logger = LoggerFactory.getLogger(SentenceImpl.class);

    private ImmutableList<Word> words = Lists.immutable.empty();
    private ImmutableList<Phrase> phrases = Lists.immutable.empty();

    private final CoreSentence coreSentence;
    private final int sentenceNumber;

    public SentenceImpl(CoreSentence coreSentence, int sentenceNumber) {
        this.coreSentence = coreSentence;
        this.sentenceNumber = sentenceNumber;
    }

    @Override
    public int getSentenceNumber() {
        return sentenceNumber;
    }

    @Override
    public int getSentenceNumberForOutput() {
        return sentenceNumber + 1;
    }

    @Override
    public ImmutableList<Word> getWords() {
        if (words.isEmpty()) {
            final MutableList<Word> wordsList = Lists.mutable.empty();
            var coreDocument = coreSentence.document();
            var wordIndex = 0;
            for (var token : coreDocument.tokens()) {
                var currSentenceNo = token.sentIndex();
                if (currSentenceNo == sentenceNumber) {
                    wordsList.add(new WordImpl(token, wordIndex, coreDocument));
                } else if (currSentenceNo > sentenceNumber) {
                    break;
                }
                wordIndex++;
            }
            this.words = wordsList.toImmutable();
        }
        return words;
    }

    @Override
    public String getText() {
        return coreSentence.text();
    }

    @Override
    public ImmutableList<Phrase> getPhrases() {
        if (phrases.isEmpty()) {
            MutableList<Phrase> newPhrases = Lists.mutable.empty();
            var constituencyParse = this.coreSentence.constituencyParse();
            for (var phrase : constituencyParse) {
                if (phrase.isPhrasal()) {
                    var wordsForPhrase = getWordsForPhrase(phrase, this);
                    PhraseImpl currPhrase = new PhraseImpl(phrase, wordsForPhrase);
                    newPhrases.add(currPhrase);
                }
            }
            phrases = newPhrases.toImmutable();
        }

        return phrases;
    }

    protected static List<Word> getWordsForPhrase(Tree phrase, SentenceImpl sentence) {
        List<Word> phraseWords = Lists.mutable.empty();
        var coreLabels = phrase.taggedLabeledYield();
        var index = findIndexOfFirstWordInPhrase(coreLabels.get(0), sentence);
        logger.debug("phrase starting position: {}", index);
        for (var word : coreLabels) {
            var phraseWord = new WordImpl(word, index++, sentence.coreSentence.document());
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
        return Objects.hash(sentenceNumber, getText());
    }
}
