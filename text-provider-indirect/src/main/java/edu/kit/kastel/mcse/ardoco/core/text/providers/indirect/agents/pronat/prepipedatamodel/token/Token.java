package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.pronat.prepipedatamodel.token;

import java.util.Objects;

/**
 * This class represents the internal representation of a token. Each token consists of a word, position, pos tag, chunk
 * and an instruction number.
 *
 * @author Markus Kocybik
 * @author Sebastian Weigelt - let class extend MainHypothesisToken on 2016-12-08
 * @author Tobias Hey - added lemma and stem to Tokens on 2017-01-23
 */
public class Token extends MainHypothesisToken implements Comparable<Token> {
    private POSTag pos;
    private ChunkIOB chunkIOB;
    private int instructionNumber;
    private Chunk chunk;
    private String ner;
    private String lemma;
    private String stem;
    private int sentenceNumber;

    public Token(String word, POSTag pos, ChunkIOB chunkIOB, Chunk chunk, int position, int instructionNumber, String ner, String lemma, String stem,
            int sentenceNumber) {
        super(word, position);
        this.pos = pos;
        this.chunkIOB = chunkIOB;
        this.chunk = chunk;
        this.instructionNumber = instructionNumber;
        this.ner = ner;
        this.lemma = lemma;
        this.stem = stem;
        setSentenceNumber(sentenceNumber);
        resetHash();
    }

    public Token(String word, POSTag pos, ChunkIOB chunkIOB, Chunk chunk, int position, int instructionNumber, String ner) {
        this(word, pos, chunkIOB, chunk, position, instructionNumber, ner, null, null, 0);
    }

    public Token(String word, POSTag pos, ChunkIOB chunkIOB, Chunk chunk, int position, int instructionNumber) {
        this(word, pos, chunkIOB, chunk, position, instructionNumber, null);
    }

    public Token(String word, POSTag pos, ChunkIOB chunkIOB, Chunk chunk, int position, int instructionNumber, String lemma, String stem) {
        this(word, pos, chunkIOB, chunk, position, instructionNumber, null, lemma, stem, 0);
    }

    public Token(String word, POSTag pos, ChunkIOB chunkIOB, Chunk chunk, int position, int instructionNumber, String ner, String lemma, String stem) {
        this(word, pos, chunkIOB, chunk, position, instructionNumber, ner, lemma, stem, 0);
    }

    public Token(MainHypothesisToken feed, POSTag pos, ChunkIOB chunkIOB, Chunk chunk, int position, int instructionNumber, String ner, String lemma,
            String stem, int sentenceNumber) {
        super(feed.getWord(), feed.getPosition(), feed.getConfidence(), feed.getType(), feed.getStartTime(), feed.getEndTime(), feed.getAlternatives());
        this.pos = pos;
        this.chunkIOB = chunkIOB;
        this.chunk = chunk;
        this.instructionNumber = instructionNumber;
        this.ner = ner;
        this.lemma = lemma;
        this.stem = stem;
        setSentenceNumber(sentenceNumber);
        resetHash();
    }

    public Token(MainHypothesisToken feed, POSTag pos, ChunkIOB chunkIOB, Chunk chunk, int position, int instructionNumber, String ner) {
        this(feed, pos, chunkIOB, chunk, position, instructionNumber, ner, null, null, 0);
    }

    public Token(MainHypothesisToken feed, POSTag pos, ChunkIOB chunkIOB, Chunk chunk, int position, int instructionNumber) {
        this(feed, pos, chunkIOB, chunk, position, instructionNumber, null);
    }

    public Token(MainHypothesisToken feed, POSTag pos, ChunkIOB chunkIOB, Chunk chunk, int position, int instructionNumber, String lemma, String stem) {
        this(feed, pos, chunkIOB, chunk, position, instructionNumber, null, lemma, stem, 0);
    }

    public Token(MainHypothesisToken feed, POSTag pos, ChunkIOB chunkIOB, Chunk chunk, int position, int instructionNumber, String ner, String lemma,
            String stem) {
        this(feed, pos, chunkIOB, chunk, position, instructionNumber, ner, lemma, stem, 0);
    }

    public Token(MainHypothesisToken feed) {
        this(feed, POSTag.NONE, ChunkIOB.UNDEFINED, null, -1, -1, null, null, null, 0);
    }

    public void consumeHypothesisToken(MainHypothesisToken token) {
        super.setWord(token.getWord());
        super.setPosition(token.getPosition());
        super.setConfidence(token.getConfidence());
        super.setType(token.getType());
        super.setStartTime(token.getStartTime());
        super.setEndTime(token.getEndTime());
        super.setAlternatives(token.getAlternatives());
    }

    public ChunkIOB getChunkIOB() {
        return chunkIOB;
    }

    public void setChunkIOB(ChunkIOB chunkIOB) {
        this.chunkIOB = chunkIOB;
        resetHash();
    }

    public void setChunk(Chunk chunk) {
        this.chunk = chunk;
        resetHash();
    }

    public Chunk getChunk() {
        return chunk;
    }

    public int getInstructionNumber() {
        return instructionNumber;
    }

    public void setInstructionNumber(int instructionNumber) {
        this.instructionNumber = instructionNumber;
        resetHash();
    }

    public POSTag getPos() {
        return pos;
    }

    public void setPos(POSTag pos) {
        this.pos = pos;
        resetHash();
    }

    /**
     * @return the ner
     */
    public String getNer() {
        return ner;
    }

    /**
     * @param ner the ner to set
     */
    public void setNer(String ner) {
        this.ner = ner;
    }

    @Override
    public int hashCode() {
        if (hash != 0) {
            return hash;
        } else {
            hash = getWord().hashCode();
            hash = 31 * hash + getPosition();
            hash = getChunk() == null || getChunk().getName() == null ? hash : 31 * hash + getChunk().getName().hashCode();
            hash = getChunk() == null ? hash : 31 * hash + getChunk().getPredecessor();
            hash = getChunk() == null ? hash : 31 * hash + getChunk().getSuccessor();
            hash = getChunkIOB() == null ? hash : 31 * hash + getChunkIOB().toString().hashCode();
            hash = 31 * hash + getInstructionNumber();
            hash = getPos() == null ? hash : 31 * hash + getPos().hashCode();
            hash = getNer() == null ? hash : 31 * hash + getNer().hashCode();
            hash = getLemma() == null ? hash : 31 * hash + getLemma().hashCode();
            hash = getStem() == null ? hash : 31 * hash + getStem().hashCode();
            return hash;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Token) {
            final Token other = (Token) obj;
            return super.equals(obj) && getPosition() == other.getPosition() && getChunk().equals(other.getChunk()) && getChunkIOB().equals(other.getChunkIOB())
                    && getInstructionNumber() == other.getInstructionNumber() && getPos().equals(other.getPos()) && Objects.equals(getWord(), other.getWord())
                    && Objects.equals(getNer(), other.getNer()) && Objects.equals(getLemma(), other.getLemma()) && Objects.equals(getStem(), other.getStem());
        }
        return false;
    }

    @Override
    public String toString() {
        return super.getWord() + "(" + pos + "/" + instructionNumber + "/" + chunkIOB + "/" + chunk.getName() + "/" + chunk.getPredecessor() + "/"
                + chunk.getSuccessor() + "/" + ner + "/" + lemma + "/" + stem + ")";
    }

    private void resetHash() {
        hash = 0;
    }

    /**
     * @return the lemma
     */
    public String getLemma() {
        return lemma;
    }

    /**
     * @param lemma the lemma to set
     */
    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    /**
     * @return the stem
     */
    public String getStem() {
        return stem;
    }

    /**
     * @param stem the stem to set
     */
    public void setStem(String stem) {
        this.stem = stem;
    }

    /**
     * @return the sentenceNumber
     */
    public int getSentenceNumber() {
        return sentenceNumber;
    }

    /**
     * @param sentenceNumber the sentenceNumber to set
     */
    public void setSentenceNumber(int sentenceNumber) {
        this.sentenceNumber = sentenceNumber;
    }

    @Override
    public int compareTo(Token o) {
        return Integer.compare(getPosition(), o.getPosition());
    }
}
