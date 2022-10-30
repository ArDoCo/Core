package edu.kit.kastel.mcse.ardoco.core.text.providers.opennlp;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.POSTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

class OpenNLPTextImpl implements Text {

    private final String text;
    private ImmutableList<Sentence> sentences = Lists.immutable.empty();
    private ImmutableList<Word> words = Lists.immutable.empty();


    protected OpenNLPTextImpl(String text) {
        this.text = text;
    }

    private void setWordsAndSentences() {
        MutableList<Sentence> sentenceList = Lists.mutable.empty();
        MutableList<Word> wordList = Lists.mutable.empty();

        String[] sentenceArray = detectSentences(this.text);
        for (int i = 0; i < sentenceArray.length; i++) {
            // sentences
            sentenceList.add(new OpenNLPSentenceImpl(sentenceArray[i], this, i));
            // words
            String[] wordArray = detectWords(sentenceArray[i], false);
            String[] posTags = getPosTags(wordArray);
            for (int k = 0; k < wordArray.length; k++){
                POSTag posTag = POSTag.get(posTags[k]);
                wordList.add(new OpenNLPWordImpl(k, i, wordArray[k], this, posTag));
            }
        }
        this.sentences = sentenceList.toImmutable();
        this.words = wordList.toImmutable();
    }

    private String[] getPosTags(String[] words) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream("../en-pos-maxent.bin");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        POSModel model = null;
        try {
            model = new POSModel(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        POSTaggerME tagger = new POSTaggerME(model);
        return tagger.tag(words);
    }

    @Override
    public ImmutableList<Word> words() {
        if (this.words.isEmpty()) {
            setWordsAndSentences();
        }
        return this.words;
    }

    private String[] detectWords(String sentence, boolean whiteSpaceTokens) {
        String[] tokens = null;
        if (whiteSpaceTokens) {
            WhitespaceTokenizer whitespaceTokenizer= WhitespaceTokenizer.INSTANCE;
            tokens = whitespaceTokenizer.tokenize(sentence);
        } else {
            try {
            TokenizerME tokenizer = new TokenizerME("en");
            tokens = tokenizer.tokenize(sentence);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tokens;
    }

    @Override
    public ImmutableList<Sentence> getSentences() {
        if (this.sentences.isEmpty()) {
            setWordsAndSentences();
        }
        return this.sentences;
    }

    private String[] detectSentences(String textTest) {
        String[] sentences = null;
        try {
            SentenceDetectorME s = new SentenceDetectorME("en");
            sentences = s.sentDetect(textTest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sentences;
    }
}
