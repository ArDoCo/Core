/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.IAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.RecommendationAgent;
import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.stage.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents.InitialRecommendationAgent;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents.PhraseRecommendationAgent;

/**
 * The Class RecommendationGenerator defines the recommendation stage.
 */
public class RecommendationGenerator extends AbstractExecutionStage {

    private final MutableList<RecommendationAgent> agents = Lists.mutable.of(new InitialRecommendationAgent(), new PhraseRecommendationAgent());

    @Configurable
    private List<String> enabledAgents = agents.collect(IAgent::getId);

    /**
     * Creates a new model connection agent with the given extraction state and ntr state.
     */
    public RecommendationGenerator() {
        // empty
    }

    @Override
    public void execute(DataStructure data, Map<String, String> additionalSettings) {
        // Init new connection states
        Arrays.stream(Metamodel.values()).forEach(mm -> data.setRecommendationState(mm, new RecommendationState(additionalSettings)));

        this.applyConfiguration(additionalSettings);
        for (RecommendationAgent agent : findByClassName(enabledAgents, agents)) {
            agent.applyConfiguration(additionalSettings);
            agent.execute(data);
        }
    }
}
