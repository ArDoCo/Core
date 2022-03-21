/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.pronat.prepipedatamodel.token;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.POSTag;

/**
 * This class represents the internal representation of a token. Each token consists of a word, position, pos tag, chunk
 * and an instruction number.
 *
 * @author Markus Kocybik
 * @author Sebastian Weigelt - let class extend MainHypothesisToken on 2016-12-08
 * @author Tobias Hey - added lemma and stem to Tokens on 2017-01-23
 * @author Jan Keim - refactor constructors and add TokenBuilder
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

    public static class TokenBuilder {
        private Token token;

        public TokenBuilder(String word, int position) {
            token = new Token(word, position);
        }

        public TokenBuilder(MainHypothesisToken feed) {
            token = new Token(feed);
        }

        public Token build() {
            token.resetHash();
            return token;
        }

        public TokenBuilder withPos(POSTag pos) {
            token.pos = pos;
            return this;
        }

        public TokenBuilder withChunkIOB(ChunkIOB chunkIOB) {
            token.chunkIOB = chunkIOB;
            return this;
        }

        public TokenBuilder withChunk(Chunk chunk) {
            token.chunk = chunk;
            return this;
        }

        public TokenBuilder withInstructionNr(int instructionNumber) {
            token.instructionNumber = instructionNumber;
            return this;
        }

        public TokenBuilder withNer(String ner) {
            token.ner = ner;
            return this;
        }

        public TokenBuilder withLemma(String lemma) {
            token.lemma = lemma;
            return this;
        }

        public TokenBuilder withStem(String stem) {
            token.stem = stem;
            return this;
        }

        public TokenBuilder withSentenceNr(int sentenceNumber) {
            token.sentenceNumber = sentenceNumber;
            return this;
        }
    }

    protected Token(String word, int position) {
        super(word, position);
    }

    protected Token(MainHypothesisToken feed) {
        super(feed.getWord(), feed.getPosition());
        pos = POSTag.NONE;
        chunkIOB = ChunkIOB.UNDEFINED;
        chunk = null;
        instructionNumber = -1;
        ner = null;
        lemma = null;
        stem = null;
        setSentenceNumber(0);
        resetHash();
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
        }
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

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj.getClass() == this.getClass()) {
            final Token other = (Token) obj;
            return super.equals(obj) && getPosition() == other.getPosition() && getChunk().equals(other.getChunk()) && getChunkIOB() == other.getChunkIOB()
                    && getInstructionNumber() == other.getInstructionNumber() && getPos() == other.getPos() && Objects.equals(getWord(), other.getWord())
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
