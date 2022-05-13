/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp;

import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.ISentence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.stanford.nlp.pipeline.CoreSentence;

class Sentence implements ISentence {
    private static final Logger logger = LoggerFactory.getLogger(Sentence.class);

    private ImmutableList<IWord> words = Lists.immutable.empty();

    private CoreSentence coreSentence;
    private int sentenceNumber = -1;

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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Sentence sentence = (Sentence) o;

        return sentenceNumber == sentence.sentenceNumber && sentence.getText().equals(this.getText());
    }

    @Override
    public int hashCode() {
        return Objects.hash(sentenceNumber, getText());
    }
}
