/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp;

import java.util.List;
import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.IPhrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.ISentence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.trees.Tree;

class Sentence implements ISentence {
    private static final Logger logger = LoggerFactory.getLogger(Sentence.class);

    private ImmutableList<IWord> words = Lists.immutable.empty();
    private ImmutableList<IPhrase> phrases = Lists.immutable.empty();

    private final CoreSentence coreSentence;
    private final int sentenceNumber;

    public Sentence(CoreSentence coreSentence, int sentenceNumber) {
        this.coreSentence = coreSentence;
        this.sentenceNumber = sentenceNumber;
    }

    @Override
    public int getSentenceNumber() {
        return sentenceNumber;
    }

    @Override
    public ImmutableList<IWord> getWords() {
        if (words.isEmpty()) {
            final MutableList<IWord> wordsList = Lists.mutable.empty();
            var coreDocument = coreSentence.document();
            var wordIndex = 0;
            for (var token : coreDocument.tokens()) {
                var currSentenceNo = token.sentIndex();
                if (currSentenceNo == sentenceNumber) {
                    wordsList.add(new Word(token, wordIndex, coreDocument));
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

    public ImmutableList<IPhrase> getPhrases() {
        if (phrases.isEmpty()) {
            MutableList<IPhrase> newPhrases = Lists.mutable.empty();
            var constituencyParse = this.coreSentence.constituencyParse();
            for (var phrase : constituencyParse) {
                if (phrase.isPhrasal()) {
                    var wordsForPhrase = getWordsForPhrase(phrase, this);
                    Phrase currPhrase = new Phrase(phrase, wordsForPhrase);
                    newPhrases.add(currPhrase);
                }
            }
            phrases = newPhrases.toImmutable();
        }

        return phrases;
    }

    protected static List<IWord> getWordsForPhrase(Tree phrase, Sentence sentence) {
        List<IWord> phraseWords = Lists.mutable.empty();
        var coreLabels = phrase.taggedLabeledYield();
        var index = findIndexOfFirstWordInPhrase(coreLabels.get(0), sentence);
        logger.debug("phrase starting position: {}", index);
        for (var word : coreLabels) {
            var phraseWord = new Word(word, index++, sentence.coreSentence.document());
            phraseWords.add(phraseWord);
        }
        return phraseWords;
    }

    private static int findIndexOfFirstWordInPhrase(CoreLabel firstWordLabel, Sentence sentence) {
        for (var word : sentence.getWords()) {
            if (word instanceof Word sentenceWord) {
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
        if (o instanceof ISentence sentence) {
            return isEqualTo(sentence);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sentenceNumber, getText());
    }
}
