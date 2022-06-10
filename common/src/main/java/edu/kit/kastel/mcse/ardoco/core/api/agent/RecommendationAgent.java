/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.agent;

import edu.kit.kastel.informalin.data.DataRepository;

public abstract class RecommendationAgent extends AbstractAgent {

    protected RecommendationAgent(String id, DataRepository dataRepository) {
        super(id, dataRepository);
    }
}
