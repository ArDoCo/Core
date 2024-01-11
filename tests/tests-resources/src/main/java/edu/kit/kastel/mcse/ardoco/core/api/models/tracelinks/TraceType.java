/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import java.io.Serializable;

/**
 * The {@link TraceType} refers to the type associated with a trace link. It can be used to annotate trace links in the gold standard with additional
 * information. Actual negatives can be marked using {@link #COMMON_NOUN}, {@link #SHARED_STEM} and {@link #OTHER_ENTITY}. The gold standard is not complete for
 * actual negatives, however the provided information can be used to determine potential causes of false positives.
 */
public enum TraceType implements Serializable {
    ENTITY(true),//Both endpoints point to the same entity
    COMMON_NOUN(false),//Created due to a common noun usage (e.g. Entity "test" and word "test" in the text)
    SHARED_STEM(false),//Created due to a shared word stem (e.g. Entity "testing" and word "testing" in the text)
    ENTITY_COREFERENCE(true),//Both endpoints point to the same entity, but the textual endpoint is a coreference
    OTHER_ENTITY(false),//Created due to another (similarly-named) entity
    UNCERTAIN(false);//Marker for discussion

    private final boolean actualPositive;

    TraceType(boolean actualPositive) {
        this.actualPositive = actualPositive;
    }

    /**
     * {@return whether a trace link of this type is an actual positive}
     */
    public boolean isActualPositive() {
        return this.actualPositive;
    }
}
