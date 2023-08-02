package edu.kit.kastel.mcse.ardoco.tests.integration;

import static edu.kit.kastel.mcse.ardoco.core.configuration.AbstractConfigurable.CLASS_ATTRIBUTE_CONNECTOR;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class StageTest<T extends AbstractExecutionStage, V extends Record> {
    private static final Logger logger = LoggerFactory.getLogger(StageTest.class);
    private final Class<? extends AbstractExecutionStage> stage;
    private final Set<Class<? extends PipelineAgent>> agents;
    private final Map<Class<? extends PipelineAgent>, Set<Class<? extends Informant>>> informantsMap;
    private final Map<DiagramProject, TestDataRepositoryCache> dataRepositoryCaches = new HashMap<>();

    public StageTest(T stageInstance) {
        this.stage = stageInstance.getClass();
        var agentInstances = stageInstance.getAgents();
        this.agents = agentInstances.stream().map(PipelineAgent::getClass).collect(Collectors.toSet());
        this.informantsMap = agentInstances.stream()
                .collect(Collectors.toMap(PipelineAgent::getClass,
                        agent -> agent.getPipelineSteps().stream().map(Informant::getClass).collect(Collectors.toSet())));
    }

    private DataRepository setup(DiagramProject project) {
        logger.info("Run PreTestRunner for {}", project.name());
        return runPreTestRunner(project);
    }

    protected DataRepository run(DiagramProject project, Map<String, String> additionalConfigurations) {
        var dataRepository = getDataRepository(project);
        return runTestRunner(project, additionalConfigurations, dataRepository);
    }

    protected DataRepository getDataRepository(DiagramProject diagramProject) {
        return dataRepositoryCaches.computeIfAbsent(diagramProject, dp -> TestDataRepositoryCache.getInstance(stage, diagramProject)).get(this::setup);
    }

    protected DataRepository run(DiagramProject project) {
        return run(project, Map.of());
    }

    protected abstract V runComparable(DiagramProject project, Map<String, String> additionalConfigurations);

    protected V runComparable(DiagramProject project) {
        return runComparable(project, Map.of());
    }

    protected abstract DataRepository runPreTestRunner(DiagramProject project);

    protected abstract DataRepository runTestRunner(DiagramProject project, Map<String, String> additionalConfigurations, DataRepository dataRepository);

    private static final int repetitions = 2;

    @DisplayName("Repetition Test Stage")
    @Test
    @Order(-1)
    void stageRepetitionTest() {
        var results = new ArrayList<V>(repetitions);
        for (var i = 0; i < repetitions; i++) {
            logger.info("Stage {} repetition {}/{}", stage.getSimpleName(), i + 1, repetitions);
            results.add(runComparable(DiagramProject.TEAMMATES));
        }
        assertEquals(1, results.stream().distinct().toList().size());
    }

    @DisplayName("Repetition Test Agents")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getAgents")
    @Order(-2)
    void agentRepetitionTest(Class<? extends PipelineAgent> clazzAgent) {
        var results = new ArrayList<V>(repetitions);
        for (var i = 0; i < repetitions; i++) {
            logger.info("Agent {} repetition {}/{}", clazzAgent.getSimpleName(), i + 1, repetitions);
            results.add(runComparable(DiagramProject.TEAMMATES, enableAgents(Set.of(clazzAgent))));
        }
        assertEquals(1, results.stream().distinct().toList().size());
    }

    @DisplayName("Repetition Test Informants")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getInformants")
    @Order(-3)
    void informantRepetitionTest(Class<? extends Informant> clazzInformant) {
        var results = new ArrayList<V>(repetitions);
        for (var i = 0; i < repetitions; i++) {
            logger.info("Informant {} repetition {}/{}", clazzInformant.getSimpleName(), i + 1, repetitions);
            results.add(runComparable(DiagramProject.TEAMMATES, enableInformants(Set.of(clazzInformant))));
        }
        assertEquals(1, results.stream().distinct().toList().size());
    }

    /**
     * {@return a map of configurations that will enable the specified agents}
     *
     * @param enabledAgents Set of agents that should be enabled
     */
    protected Map<String, String> enableAgents(Set<Class<? extends PipelineAgent>> enabledAgents) {
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
    protected Map<String, String> enableInformants(Set<Class<? extends Informant>> enabledInformant) {
        var map = new HashMap<String, String>();
        var agentsToEnable = new HashSet<Class<? extends PipelineAgent>>();
        for (var informant : enabledInformant) {
            this.informantsMap.entrySet().stream().filter(e -> e.getValue().contains(informant)).forEach(e -> {
                agentsToEnable.add(e.getKey());
                var listString = List.of(informant.getSimpleName()).toString();
                map.put(e.getKey().getSimpleName() + CLASS_ATTRIBUTE_CONNECTOR + "enabledInformants", listString.substring(1, listString.length() - 1));
            });
        }
        //Make sure we enable the agents which run the informants
        map.putAll(enableAgents(agentsToEnable));
        return map;
    }

    public Set<Class<? extends PipelineAgent>> getAgents() {
        return this.agents;
    }

    public Set<Class<? extends Informant>> getInformants() {
        return informantsMap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }
}
