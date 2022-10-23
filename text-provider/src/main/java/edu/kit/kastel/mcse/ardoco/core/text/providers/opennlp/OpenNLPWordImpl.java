package edu.kit.kastel.mcse.ardoco.core.text.providers.opennlp;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.*;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import org.eclipse.collections.api.list.ImmutableList;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

class OpenNLPWordImpl implements Word {

    private final int indexInSentence;
    private final int sentenceNo;
    private final String text;
    private final OpenNLPTextImpl parent;
    private final POSTag posTag;
    private String lemma;
    private Word preWord;
    private Word nextWord;

    private Phrase phrase;

    public OpenNLPWordImpl (int indexInSentence, int sentenceNo, String text, OpenNLPTextImpl parent, POSTag posTag) {
        this.indexInSentence = indexInSentence;
        this.sentenceNo = sentenceNo;
        this.text = text;
        this.parent = parent;
        this.posTag = posTag;
    }



    @Override
    public int getSentenceNo() {
        return this.sentenceNo;
    }

    @Override
    public Sentence getSentence() {
        return this.parent.getSentences().get(this.sentenceNo);
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public POSTag getPosTag() {
        return this.posTag;
    }

    @Override
    public Word getPreWord() {
        int preWordIndex = indexInSentence - 1;
        if (preWord == null && preWordIndex > 0) {
            preWord = parent.words().get(preWordIndex);
        }
        return this.preWord;
    }

    @Override
    public Word getNextWord() {
        int nextWordIndex = indexInSentence + 1;
        if (nextWord == null && nextWordIndex < parent.getLength()) {
            nextWord = parent.words().get(nextWordIndex);
        }
        return this.nextWord;
    }

    @Override
    public int getPosition() {
        return this.indexInSentence;
    }

    @Override
    public String getLemma() {
        if (this.lemma == null) {
            InputStream dictLemmatizer = null;
            try {
                dictLemmatizer = new FileInputStream("../en-lemmatizer.dict.txt");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            DictionaryLemmatizer lemmatizer = null;
            try {
                lemmatizer = new DictionaryLemmatizer(dictLemmatizer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            this.lemma = Arrays.toString(lemmatizer.lemmatize(new String[]{this.text}, new String[]{this.posTag.toString()}));
        }
        return this.lemma;
    }

    @Override
    public ImmutableList<Word> getOutgoingDependencyWordsWithType(DependencyTag dependencyTag) {
        return null;
    }

    @Override
    public ImmutableList<Word> getIncomingDependencyWordsWithType(DependencyTag dependencyTag) {
        return null;
    }

    @Override
    public Phrase getPhrase() {
        if (this.phrase == null) {
            this.phrase = loadPhrase();
        }
        return this.phrase;
    }

    private Phrase loadPhrase() {
        Phrase currentPhrase = getSentence().getPhrases().stream().filter(p -> p.getContainedWords().contains(this)).findFirst().orElseThrow();
//        List<Phrase> subPhrases = List.of(currentPhrase);
//        while (!subPhrases.isEmpty()) {
//            currentPhrase = subPhrases.get(0);
//            subPhrases = currentPhrase.getSubPhrases().stream().filter(p -> p.getContainedWords().contains(this)).toList();
//        }
        return currentPhrase;
    }
}
