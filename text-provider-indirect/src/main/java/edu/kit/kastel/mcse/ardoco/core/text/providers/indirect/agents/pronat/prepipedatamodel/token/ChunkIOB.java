package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.agents.pronat.prepipedatamodel.token;

/**
 * This class represents all valid chunks (IOB-format)
 *
 * @author Sebastian Weigelt
 * @author Markus Kocybik
 */
public enum ChunkIOB {
    //@formatter:off
	NOUN_PHRASE_BEGIN("B-NP"),
	NOUN_PHRASE_INSIDE("I-NP"),
	PREPOSITIONAL_PHRASE_BEGIN("B-PP"),
	PREPOSITIONAL_PHRASE_INSIDE("I-PP"),
	VERB_PHRASE_BEGIN("B-VP"),
	VERB_PHRASE_INSIDE("I-VP"),
	ADVERB_PHRASE_BEGIN("B-ADVP"),
	ADVERB_PHRASE_INSIDE("I-ADVP"),
	ADJECTIVE_PHRASE_BEGIN("B-ADJP"),
	ADJECTIVE_PHRASE_INSIDE("I-ADJP"),
	SUBORDINATING_CONJUNCTION_BEGIN("B-SBAR"),
	SUBORDINATING_CONJUNCTION_INSIDE("I-SBAR"),
	PARTICLE_BEGIN("B-PRT"),
	PARTICLE_INSIDE("I-PRT"),
	INTERJECTION_BEGIN("B-INTJ"),
	INTERJECTION_INSIDE("I-INTJ"),
	CONJUNCTION_PHRASE_BEGIN("B-CONJP"),
	CONJUNCTION_PHRASE_INSIDE("I-CONJP"),
	UNLIKE_COODINATED_PHRASE_BEGIN("B-UCP"),
	UNLIKE_COODINATED_PHRASE_INSIDE("I-UCP"),
	LIST_MARKER_BEGIN("B-LST"),
	LIST_MARKER_INSIDE("I-LST"),
	OUTSIDE("O"),
	UNDEFINED("UNDEF");
	//@formatter:on
    private final String tag;

    ChunkIOB(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return getTag();
    }

    protected String getTag() {
        return tag;
    }

    public static ChunkIOB get(String value) {
        for (ChunkIOB v : values()) {
            if (value.equals(v.getTag())) {
                return v;
            }
        }

        throw new IllegalArgumentException("Unknown part of speech: '" + value + "'.");
    }
}
