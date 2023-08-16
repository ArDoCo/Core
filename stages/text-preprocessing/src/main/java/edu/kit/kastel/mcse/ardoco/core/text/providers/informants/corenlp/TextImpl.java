/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.stanford.nlp.pipeline.CoreDocument;

class TextImpl implements Text {

    final transient CoreDocument coreDocument;
    private ImmutableList<Sentence> sentences = Lists.immutable.empty();
    private ImmutableList<Word> words = Lists.immutable.empty();

    TextImpl(CoreDocument coreDocument) {
        this.coreDocument = coreDocument;
    }

    @Override
    public ImmutableList<Word> getWords() {
        if (words.isEmpty()) {
            iterateDocumentForWordsAndSentences();
        }
        return words;
    }

    @Override
    public ImmutableList<Phrase> getPhrases() {
        return Lists.immutable.fromStream(getSentences().stream().flatMap(s -> s.getPhrases().stream()));
    }

    @Override
    public ImmutableList<Sentence> getSentences() {
        if (sentences.isEmpty()) {
            iterateDocumentForWordsAndSentences();
        }
        return sentences;
    }

    private void iterateDocumentForWordsAndSentences() {
        MutableList<Sentence> sentenceList = Lists.mutable.empty();
        MutableList<Word> wordList = Lists.mutable.empty();

        var coreSentences = coreDocument.sentences();
        int wordIndex = 0;
        for (int i = 0; i < coreSentences.size(); i++) {
            var coreSentence = coreSentences.get(i);
            var sentence = new SentenceImpl(coreSentence, i, this);
            sentenceList.add(sentence);

            for (var token : coreSentence.tokens()) {
                var word = new WordImpl(token, wordIndex, this);
                wordList.add(word);
                wordIndex++;
            }
        }

        sentences = sentenceList.toImmutable();
        words = wordList.toImmutable();
    }
}
