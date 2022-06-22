/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator;

import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.ConnectionAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.IAgent;
import edu.kit.kastel.mcse.ardoco.core.api.stage.AbstractExecutionStage;
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

    private final MutableList<ConnectionAgent> agents;

    @Configurable
    private List<String> enabledAgents;

    /**
     * Create the module.
     */
    public ConnectionGenerator(DataRepository dataRepository) {
        super("ConnectionGenerator", dataRepository);

        agents = Lists.mutable.of(new InitialConnectionAgent(dataRepository), new ReferenceAgent(dataRepository), new InstanceConnectionAgent(dataRepository));
        enabledAgents = agents.collect(IAgent::getId);
    }

    @Override
    public void run() {
        var connectionStates = ConnectionStates.build();
        getDataRepository().addData(ConnectionStates.ID, connectionStates);

        for (ConnectionAgent agent : findByClassName(enabledAgents, agents)) {
            this.addPipelineStep(agent);
        }

        super.run();
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        super.delegateApplyConfigurationToInternalObjects(additionalConfiguration);
        for (var agent : agents) {
            agent.applyConfiguration(additionalConfiguration);
        }
    }

}
