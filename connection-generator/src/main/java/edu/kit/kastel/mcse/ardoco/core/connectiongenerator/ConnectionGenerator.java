/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator;

import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.agent.ConnectionAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.IAgent;
import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.api.stage.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.common.Configurable;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents.InitialConnectionAgent;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents.InstanceConnectionAgent;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents.ReferenceAgent;

/**
 * The ModelConnectionAgent runs different analyzers and solvers. This agent creates recommendations as well as
 * matchings between text and model. The order is important: All connections should run after the recommendations have
 * been made.
 *
 * @author Sophie
 */
public class ConnectionGenerator extends AbstractExecutionStage {

    private MutableList<ConnectionAgent> agents = Lists.mutable.of(new InitialConnectionAgent(), new ReferenceAgent(), new InstanceConnectionAgent());

    @Configurable
    private List<String> enabledAgents = agents.collect(IAgent::getId);

    /**
     * Create the module.
     */
    public ConnectionGenerator() {
    }

    @Override
    public void execute(DataStructure data, Map<String, String> additionalSettings) {
        // Init new connection states
        data.getModelIds().forEach(mid -> data.setConnectionState(mid, new ConnectionState(additionalSettings)));

        this.applyConfiguration(additionalSettings);
        for (ConnectionAgent agent : findByClassName(enabledAgents, agents)) {
            agent.applyConfiguration(additionalSettings);
            agent.execute(data);
        }
    }
}
