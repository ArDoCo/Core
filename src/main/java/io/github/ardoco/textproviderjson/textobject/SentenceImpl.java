/* Licensed under MIT 2022-2023. */
package io.github.ardoco.textproviderjson.textobject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;

public class SentenceImpl implements Sentence {

    private final ImmutableList<Word> words;
    private ImmutableList<Phrase> phrases = Lists.immutable.empty();

    private final int sentenceNumber;

    private final String text;

    public SentenceImpl(int sentenceNumber, String text, ImmutableList<Word> words) {
        this.sentenceNumber = sentenceNumber;
        this.text = text;
        this.words = words;
    }

    public void setPhrases(ImmutableList<Phrase> phrases) {
        this.phrases = phrases;
    }

    @Override
    public int getSentenceNumber() {
        return sentenceNumber;
    }

    @Override
    public ImmutableList<Word> getWords() {
        return words;
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public ImmutableList<Phrase> getPhrases() {
        List<Phrase> allPhrases = new ArrayList<>(this.phrases.toList());
        for (Phrase phrase : this.phrases.toList()) {
            allPhrases.addAll(phrase.getSubPhrases().toList());
        }
        return Lists.immutable.ofAll(allPhrases);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SentenceImpl sentence))
            return false;
        return sentenceNumber == sentence.sentenceNumber && Objects.equals(words, sentence.words) && Objects.equals(phrases, sentence.phrases) && Objects
                .equals(text, sentence.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(words, phrases, sentenceNumber, text);
    }
}
