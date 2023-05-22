/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator;

import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.connectiongenerator.ConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents.InitialConnectionAgent;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents.InstanceConnectionAgent;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents.ProjectNameFilterAgent;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents.ReferenceAgent;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Agent;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

/**
 * The ModelConnectionAgent runs different analyzers and solvers. This agent creates recommendations as well as
 * matchings between text and model. The order is important: All connections should run after the recommendations have
 * been made.
 */
public class ConnectionGenerator extends AbstractExecutionStage {

    private final MutableList<PipelineAgent> agents;

    @Configurable
    private List<String> enabledAgents;

    /**
     * Create the module.
     *
     * @param dataRepository the {@link DataRepository}
     */
    public ConnectionGenerator(DataRepository dataRepository) {
        super("ConnectionGenerator", dataRepository);

        agents = Lists.mutable.of(new InitialConnectionAgent(dataRepository), new ReferenceAgent(dataRepository), new ProjectNameFilterAgent(dataRepository),
                new InstanceConnectionAgent(dataRepository));
        enabledAgents = agents.collect(Agent::getId);
    }

    /**
     * Creates a {@link ConnectionGenerator} and applies the additional configuration to it.
     *
     * @param additionalConfigs the additional configuration
     * @param dataRepository    the data repository
     * @return an instance of connectionGenerator
     */
    public static ConnectionGenerator get(Map<String, String> additionalConfigs, DataRepository dataRepository) {
        var connectionGenerator = new ConnectionGenerator(dataRepository);
        connectionGenerator.applyConfiguration(additionalConfigs);
        return connectionGenerator;
    }

    @Override
    protected void initializeState() {
        var connectionStates = ConnectionStatesImpl.build();
        getDataRepository().addData(ConnectionStates.ID, connectionStates);
    }

    @Override
    protected List<PipelineAgent> getEnabledAgents() {
        return findByClassName(enabledAgents, agents);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        super.delegateApplyConfigurationToInternalObjects(additionalConfiguration);
        for (var agent : agents) {
            agent.applyConfiguration(additionalConfiguration);
        }
    }

}
