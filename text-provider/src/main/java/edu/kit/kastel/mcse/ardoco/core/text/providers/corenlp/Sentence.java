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
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.Tree;

class Sentence implements ISentence {
    private static final Logger logger = LoggerFactory.getLogger(Sentence.class);

    private ImmutableList<IWord> words = Lists.immutable.empty();
    private ImmutableList<IPhrase> phrases = Lists.immutable.empty();

    private final Text parent;

    private final CoreSentence coreSentence;
    private final int sentenceNumber;

    public Sentence(CoreSentence coreSentence, int sentenceNumber, Text parent) {
        this.coreSentence = coreSentence;
        this.sentenceNumber = sentenceNumber;
        this.parent = parent;
    }

    @Override
    public int getSentenceNumber() {
        return sentenceNumber;
    }

    @Override
    public ImmutableList<IWord> getWords() {
        if (words.isEmpty()) {
            this.words = parent.getWords().select(w -> w.getSentenceNo() == sentenceNumber).toImmutable();
        }
        return words;
    }

    @Override
    public String getText() {
        return coreSentence.text();
    }

    @Override
    public ImmutableList<IPhrase> getPhrases() {
        if (phrases.isEmpty()) {
            MutableList<IPhrase> newPhrases = Lists.mutable.empty();
            var constituencyParse = this.coreSentence.constituencyParse();
            for (var phrase : constituencyParse) {
                if (phrase.isPhrasal()) {
                    ImmutableList<IWord> wordsForPhrase = Lists.immutable.withAll(getWordsForPhrase(phrase));
                    Phrase currPhrase = new Phrase(phrase, wordsForPhrase, this);
                    newPhrases.add(currPhrase);
                }
            }
            phrases = newPhrases.toImmutable();
        }

        return phrases;
    }

    protected List<IWord> getWordsForPhrase(Tree phrase) {
        List<IWord> phraseWords = Lists.mutable.empty();
        var coreLabels = phrase.taggedLabeledYield();
        var index = findIndexOfFirstWordInPhrase(coreLabels.get(0), this);
        logger.debug("phrase starting position: {}", index);
        for (int wordIndexInSentence = 0; wordIndexInSentence < coreLabels.size(); wordIndexInSentence++) {
            var phraseWord = parent.getWords().get(index++);
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
        return Objects.hash(getSentenceNumber(), getText());
    }

    public SemanticGraph dependencyParse() {
        return this.coreSentence.dependencyParse();
    }
}
