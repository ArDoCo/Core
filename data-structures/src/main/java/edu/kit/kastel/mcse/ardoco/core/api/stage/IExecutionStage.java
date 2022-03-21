/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.stage;

import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;

/**
 * The Interface IExecutionStage defines one execution stage of the approach.
 */
public interface IExecutionStage {
    /**
     * Runs the agent with its analyzers and extractors.
     */
    void execute(DataStructure data, Map<String, String> additionalSettings);
}
