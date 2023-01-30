package io.github.ardoco.textproviderjson;

import java.io.IOException;
import com.fasterxml.jackson.annotation.*;

public enum PosTag {
    //@formatter:off
    ADJECTIVE("JJ"), ADJECTIVE_COMPARATIVE(ADJECTIVE + "R"), ADJECTIVE_SUPERLATIVE(ADJECTIVE + "S"), ADVERB("RB"), ADVERB_COMPARATIVE(ADVERB + "R"), ADVERB_SUPERLATIVE(ADVERB + "S"), ADVERB_WH(
            "W" + ADVERB), CONJUNCTION_COORDINATING("CC"), CONJUNCTION_SUBORDINATING("IN"), CARDINAL_NUMBER("CD"), DETERMINER("DT"), DETERMINER_WH("W" + DETERMINER), EXISTENTIAL_THERE(
            "EX"), FOREIGN_WORD("FW"), LIST_ITEM_MARKER("LS"), NOUN("NN"), NOUN_PLURAL(NOUN + "S"), NOUN_PROPER_SINGULAR(NOUN + "P"), NOUN_PROPER_PLURAL(NOUN + "PS"), PREDETERMINER(
            "PDT"), POSSESSIVE_ENDING("POS"), PRONOUN_PERSONAL("PRP"), PRONOUN_POSSESSIVE("PRP$"), PRONOUN_POSSESSIVE_WH("WP$"), PRONOUN_WH("WP"), PARTICLE("RP"), SYMBOL("SYM"), TO(
            "TO"), INTERJECTION("UH"), VERB("VB"), VERB_PAST_TENSE(VERB + "D"), VERB_PARTICIPLE_PRESENT(VERB + "G"), VERB_PARTICIPLE_PAST(VERB + "N"), VERB_SINGULAR_PRESENT_NONTHIRD_PERSON(
            VERB + "P"), VERB_SINGULAR_PRESENT_THIRD_PERSON(VERB + "Z"), VERB_MODAL("MD"), CLOSER("."), COMMA(","), COLON(":"), LEFT_PAREN("-LRB-"), RIGHT_PAREN("-RRB-"), NONE("-NONE-"), OPEN_QUOTE(
            "``"), CLOSE_QUOTE("''"), DOLLAR("$"), HASHTAG("#"), HYPH("HYPH"), NFP("NFP"), ADD("ADD"), AFX("AFX"), GW("GW"), XX("XX");
    //@formatter:on

    private final String tag;

    PosTag(String tag) {
        this.tag = tag;
    }

    /**
     * Returns the encoding for this part-of-speech.
     *
     * @return A string representing a Penn Treebank encoding for an English part-of-speech.
     */
    @Override
    public String toString() {
        return getTag();
    }

    /**
     * Gets the tag information.
     *
     * @return the tag
     */
    public String getTag() {
        return tag;
    }
    @JsonValue
    public String toValue() {
        return getTag();
    }

    @JsonCreator
    public static PosTag forValue(String value) throws IOException {
        for (PosTag v : values()) {
            if (value.equals(v.getTag())) {
                return v;
            }
        }
        throw new IOException("Cannot deserialize PosTag");
    }
}
