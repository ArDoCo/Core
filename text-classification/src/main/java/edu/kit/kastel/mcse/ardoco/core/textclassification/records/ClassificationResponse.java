/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.textclassification.records;

import java.util.Map;

/**
 * ClassificationResponse represents the result of the classifications of a set of phrases.
 * 
 * @param classifications a map with the identifiers of the phrases and the corresponding classification labels.
 */
public record ClassificationResponse(Map<Integer, String> classifications) {
}
