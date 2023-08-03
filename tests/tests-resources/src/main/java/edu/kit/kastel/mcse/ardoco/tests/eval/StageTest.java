package edu.kit.kastel.mcse.ardoco.tests.eval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
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

import edu.kit.kastel.mcse.ardoco.core.configuration.ConfigurationUtility;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.data.DeepCopy;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class StageTest<T extends AbstractExecutionStage, V extends Record> {
    private static final Logger logger = LoggerFactory.getLogger(StageTest.class);
    private final T stage;
    private final Set<Class<? extends PipelineAgent>> agents;
    private final Map<Class<? extends PipelineAgent>, Set<Class<? extends Informant>>> informantsMap;
    private final Map<DiagramProject, TestDataRepositoryCache> dataRepositoryCaches = new HashMap<>();

    public StageTest(T stage) {
        this.stage = stage;
        this.agents = ConfigurationUtility.getAgents(stage);
        this.informantsMap = ConfigurationUtility.getInformantsMap(stage);
    }

    private DataRepository setup(DiagramProject project) {
        logger.info("Run PreTestRunner for {}", project.name());
        return runPreTestRunner(project);
    }

    protected DataRepository run(DiagramProject project, Map<String, String> additionalConfigurations) {
        var dataRepository = getDataRepository(project);
        return runTestRunner(project, additionalConfigurations, dataRepository);
    }

    @NotNull
    @DeepCopy
    protected DataRepository getDataRepository(DiagramProject diagramProject) {
        return dataRepositoryCaches.computeIfAbsent(diagramProject, dp -> TestDataRepositoryCache.getInstance(stage.getClass(), diagramProject))
                .get(this::setup);
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
            logger.info("Stage {} repetition {}/{}", stage.getClass().getSimpleName(), i + 1, repetitions);
            results.add(runComparable(DiagramProject.TEAMMATES));
        }
        Assertions.assertEquals(1, results.stream().distinct().toList().size());
    }

    @DisplayName("Repetition Test Agents")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getAgents")
    @Order(-2)
    void agentRepetitionTest(Class<? extends PipelineAgent> clazzAgent) {
        var results = new ArrayList<V>(repetitions);
        for (var i = 0; i < repetitions; i++) {
            logger.info("Agent {} repetition {}/{}", clazzAgent.getSimpleName(), i + 1, repetitions);
            results.add(runComparable(DiagramProject.TEAMMATES, ConfigurationUtility.enableAgents(stage.getClass(), Set.of(clazzAgent))));
        }
        Assertions.assertEquals(1, results.stream().distinct().toList().size());
    }

    @DisplayName("Repetition Test Informants")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getInformants")
    @Order(-3)
    void informantRepetitionTest(Class<? extends Informant> clazzInformant) {
        var results = new ArrayList<V>(repetitions);
        for (var i = 0; i < repetitions; i++) {
            logger.info("Informant {} repetition {}/{}", clazzInformant.getSimpleName(), i + 1, repetitions);
            results.add(runComparable(DiagramProject.TEAMMATES, ConfigurationUtility.enableInformants(stage, Set.of(clazzInformant))));
        }
        Assertions.assertEquals(1, results.stream().distinct().toList().size());
    }

    public Set<Class<? extends PipelineAgent>> getAgents() {
        return this.agents;
    }

    public Set<Class<? extends Informant>> getInformants() {
        return informantsMap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }
}
