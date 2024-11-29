/* Licensed under MIT 2024. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator;

import java.io.Serializable;

public interface RecommendationStateStrategy extends Serializable {
    boolean areRecommendedInstanceTypesSimilar(String typeA, String typeB);

    boolean areRecommendedInstanceNamesSimilar(String nameA, String nameB);
}
