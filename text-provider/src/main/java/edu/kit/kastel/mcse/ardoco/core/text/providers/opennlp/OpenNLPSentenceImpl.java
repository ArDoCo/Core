package edu.kit.kastel.mcse.ardoco.core.text.providers.opennlp;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

class OpenNLPSentenceImpl implements Sentence {

    private ImmutableList<Word> words = Lists.immutable.empty();
    private ImmutableList<Phrase> phrases = Lists.immutable.empty();
    private final String text;
    private final OpenNLPTextImpl parent;
    private final int sentenceNumber;

    public OpenNLPSentenceImpl(String text, OpenNLPTextImpl parent, int sentenceNumber) {
        this.text = text;
        this.parent = parent;
        this.sentenceNumber = sentenceNumber;
    }

    @Override
    public int getSentenceNumber() {
        return this.sentenceNumber;
    }

    public OpenNLPTextImpl getParentText() {
        return this.parent;
    }

    @Override
    public ImmutableList<Word> getWords() {
        if (this.words.isEmpty()) {
            this.words = parent.words().select(w -> w.getSentenceNo() == sentenceNumber).toImmutable();
        }
        return this.words;
    }

    @Override
    public String getText() {
        return this.text;
    }


    public ImmutableList<Phrase> getPhrases() {
        if (this.phrases.isEmpty()) {
            MutableList<Phrase> newPhrases = Lists.mutable.empty();
            Parse[] parses = this.parse();
            for (Parse p: parses) {
                newPhrases.add(new OpenNLPPhraseImpl(this, parses, p));
            }
            this.phrases = newPhrases.toImmutable();
        }
        return this.phrases;
    }

    private Parse[] parse() {
        Parse[] parsedText = null;
        try {
            InputStream inputStream = new FileInputStream("../en-parser-chunking.bin");
            ParserModel model = new ParserModel(inputStream);
            Parser parser = ParserFactory.create(model);
            parsedText = ParserTool.parseLine(this.getText(), parser, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parsedText;
    }

    /*@Override
    public ImmutableList<Phrase> getPhrases() {
        if (this.phrases.isEmpty()) {
            MutableList<Phrase> newPhrases = Lists.mutable.empty();
            String[] chunkedSentence = chunk(this.text);
            for (int i = 0; i < chunkedSentence.length; i++) {
                MutableList<Word> wordsOfNewPhrase = Lists.mutable.empty();
                wordsOfNewPhrase.add(getWords().get(i));
                while (i+1 < chunkedSentence.length && chunkedSentence[i+1].charAt(0) == 'I') {
                    wordsOfNewPhrase.add(getWords().get(i+1));
                    i++;
                }
                newPhrases.add(new OpenNLPPhraseImpl(wordsOfNewPhrase.toImmutable(), this));
            }
            this.phrases = newPhrases.toImmutable();
        }
        return this.phrases;
    }

    private String[] chunk(String sentence) {
        // B: beginning of phrase; I: ongoing phrase; O: no phrase
        String[] chunkedSentence = null;
        try {
            WhitespaceTokenizer whitespaceTokenizer= WhitespaceTokenizer.INSTANCE;
            String[] tokens = whitespaceTokenizer.tokenize(sentence);

            File file = new File("../en-pos-maxent.bin");
            POSModel model = new POSModelLoader().load(file);
            POSTaggerME tagger = new POSTaggerME(model);
            String[] tags = tagger.tag(tokens);

            InputStream inputStream = new FileInputStream("../en-chunker.bin");
            ChunkerModel chunkerModel = new ChunkerModel(inputStream);
            ChunkerME chunkerME = new ChunkerME(chunkerModel);
            chunkedSentence = chunkerME.chunk(tokens, tags);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return chunkedSentence;
    }*/

}
