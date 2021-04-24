package edu.kit.kastel.mcse.ardoco.core.datastructures.agents;

/**
 * This class holds different states for objects. This enum can be used for parallelization and dependency structuring.
 *
 * @author Sophie
 *
 */
public enum DependencyType {
    /**
     * The object is dependent of the textual analysis, as well as the extracted model.
     */
    TEXT_MODEL,
    /**
     * The object is only dependent of the textual analysis.
     */
    TEXT,

    /**
     * The object is dependent from the textual analysis, the extracted model, and the recommendations.
     */
    TEXT_MODEL_RECOMMENDATION,

    /**
     * The object is dependent from the textual analysis and the recommendations.
     */
    TEXT_RECOMMENDATION,

    /**
     * The object is dependent from the extracted model and the recommendations.
     */
    MODEL_RECOMMENDATION,

    /**
     * The object is dependent from the textual analysis, the extracted model, the recommendations, and the connection
     * state.
     */
    TEXT_MODEL_RECOMMENDATION_CONNECTION,
    /**
     * The object is dependent from the extracted model, the recommendations, and the connection state.
     */
    MODEL_RECOMMENDATION_CONNECTION,

    /**
     * The object is only dependent of the recommendation state.
     */
    RECOMMENDATION;

}
