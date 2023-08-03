package edu.kit.kastel.mcse.ardoco.core.configuration;

import static edu.kit.kastel.mcse.ardoco.core.configuration.AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

public class ConfigurationUtility {
    private ConfigurationUtility() {
        throw new IllegalStateException("Cannot be instantiated");
    }

    /**
     * {@return a map of configurations that will enable the specified agents}
     *
     * @param enabledAgents Set of agents that should be enabled
     */
    public static Map<String, String> enableAgents(Class<? extends AbstractExecutionStage> stage, Set<Class<? extends PipelineAgent>> enabledAgents) {
        var map = new HashMap<String, String>();
        for (var agent : enabledAgents) {
            var listString = List.of(agent.getSimpleName()).toString();
            map.put(stage.getSimpleName() + CLASS_ATTRIBUTE_CONNECTOR + "enabledAgents", listString.substring(1, listString.length() - 1));
        }
        return map;
    }

    /**
     * {@return a map of configurations that will enable the specified informants} Will also enable the necessary agents.
     *
     * @param enabledInformant Set of informants that should be enabled
     */
    public static Map<String, String> enableInformants(AbstractExecutionStage stage, Set<Class<? extends Informant>> enabledInformant) {
        var map = new HashMap<String, String>();
        var agentsToEnable = new HashSet<Class<? extends PipelineAgent>>();
        for (var informant : enabledInformant) {
            getInformantsMap(stage).entrySet().stream().filter(e -> e.getValue().contains(informant)).forEach(e -> {
                agentsToEnable.add(e.getKey());
                var listString = List.of(informant.getSimpleName()).toString();
                map.put(e.getKey().getSimpleName() + CLASS_ATTRIBUTE_CONNECTOR + "enabledInformants", listString.substring(1, listString.length() - 1));
            });
        }
        //Make sure we enable the agents which run the informants
        map.putAll(enableAgents(stage.getClass(), agentsToEnable));
        return map;
    }

    public static Set<Class<? extends PipelineAgent>> getAgents(AbstractExecutionStage stage) {
        return stage.getAgents().stream().map(PipelineAgent::getClass).collect(Collectors.toSet());
    }

    public static Set<Class<? extends Informant>> getInformants(PipelineAgent agent) {
        return agent.getInformants().stream().map(Informant::getClass).collect(Collectors.toSet());
    }

    public static Map<Class<? extends PipelineAgent>, Set<Class<? extends Informant>>> getInformantsMap(AbstractExecutionStage stage) {
        return stage.getAgents()
                .stream()
                .collect(Collectors.toMap(PipelineAgent::getClass,
                        agent -> agent.getInformants().stream().map(Informant::getClass).collect(Collectors.toSet())));
    }
}
