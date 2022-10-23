package edu.kit.kastel.mcse.ardoco.core.text.providers.opennlp;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.ImmutableMap;

import java.util.stream.Collectors;

public class OpenNLPPhraseImpl implements Phrase {

    private final ImmutableList<Word> words;

    private final OpenNLPSentenceImpl parent;

    private String text = null;

    public OpenNLPPhraseImpl(ImmutableList<Word> words, OpenNLPSentenceImpl parent) {
        this.words = words;
        this.parent = parent;
    }

    @Override
    public int getSentenceNo() {
        return this.words.get(0).getSentenceNo();
    }

    @Override
    public String getText() {
        if(this.text == null) {
            this.text = this.words.stream().map(Word::getText).collect(Collectors.joining(" "));
        }
        return this.text;
    }

    @Override
    public ImmutableList<Word> getContainedWords() {
        return this.words;
    }



    @Override
    public PhraseType getPhraseType() {
        return null;
    }

    @Override
    public ImmutableList<Phrase> getSubPhrases() {
        return null;
    }

    @Override
    public boolean isSuperPhraseOf(Phrase other) {
        return false;
    }

    @Override
    public boolean isSubPhraseOf(Phrase other) {
        return false;
    }

    @Override
    public ImmutableMap<Word, Integer> getPhraseVector() {
        return null;
    }
}
