/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency;

/**
 * This interface extends the interface {@link Inconsistency} by stating that the inconsistency stems from a concrete textual
 * component, i.e., a sentence. This way, we can use information on the text-side to give more details.
 */
public interface TextInconsistency extends Inconsistency {

    int getSentenceNumber();

}
