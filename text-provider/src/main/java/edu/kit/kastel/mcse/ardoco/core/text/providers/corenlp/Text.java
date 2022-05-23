/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.ICorefCluster;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.ISentence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.stanford.nlp.pipeline.CoreDocument;

class Text implements IText {

    private final CoreDocument coreDocument;
    private ImmutableList<ISentence> sentences = Lists.immutable.empty();
    private ImmutableList<IWord> words = Lists.immutable.empty();

    Text(CoreDocument coreDocument) {
        this.coreDocument = coreDocument;
    }

    @Override
    public IWord getFirstWord() {
        return getWords().get(0);
    }

    @Override
    public ImmutableList<IWord> getWords() {
        if (words.isEmpty()) {
            iterateDocumentForWordsAndSentences();
        }
        return words;
    }

    @Override
    public ImmutableList<ISentence> getSentences() {
        if (sentences.isEmpty()) {
            iterateDocumentForWordsAndSentences();
        }
        return sentences;
    }

    private void iterateDocumentForWordsAndSentences() {
        MutableList<ISentence> sentenceList = Lists.mutable.empty();
        MutableList<IWord> wordList = Lists.mutable.empty();

        var coreSentences = coreDocument.sentences();
        int wordIndex = 0;
        for (int i = 0; i < coreSentences.size(); i++) {
            var coreSentence = coreSentences.get(i);
            var sentence = new Sentence(coreSentence, i);
            sentenceList.add(sentence);

            for (var token : coreSentence.tokens()) {
                var word = new Word(token, wordIndex, coreDocument);
                wordList.add(word);
                wordIndex++;
            }
        }

        sentences = sentenceList.toImmutable();
        words = wordList.toImmutable();
    }

    @Override
    public ImmutableList<ICorefCluster> getCorefClusters() {
        return Lists.immutable.empty();
    }

}
