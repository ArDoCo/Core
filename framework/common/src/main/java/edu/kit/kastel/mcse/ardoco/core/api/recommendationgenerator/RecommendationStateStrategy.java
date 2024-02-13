/* Licensed under MIT 2024. */
package edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator;

import java.io.Serializable;

public interface RecommendationStateStrategy extends Serializable {
    boolean areRITypesSimilar(String typeA, String typeB);

    boolean areRINamesSimilar(String nameA, String nameB);
}
