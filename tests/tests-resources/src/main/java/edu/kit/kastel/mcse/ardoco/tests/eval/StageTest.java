package edu.kit.kastel.mcse.ardoco.tests.eval;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
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
    private static final String ENV_DEBUG = "debug";
    private static final String CACHING = "stageTestCaching";
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
        var dataRepository = getDataRepository(project, true);
        return runTestRunner(project, additionalConfigurations, dataRepository);
    }

    protected DataRepository run(DiagramProject project, Map<String, String> additionalConfigurations, boolean cachePreRun) {
        var dataRepository = getDataRepository(project, cachePreRun);
        return runTestRunner(project, additionalConfigurations, dataRepository);
    }

    protected void debugAskCache(String id, Serializable obj) {
        if (Boolean.parseBoolean(System.getenv().getOrDefault(ENV_DEBUG, "false"))) {
            System.out.println("Cache " + obj.getClass().getSimpleName() + " at " + id + "? y/n:");
            if (new Scanner(System.in).nextLine().equals("y")) {
                cache(id, obj);
            }
        } else {
            logger.info("Set \"" + ENV_DEBUG + "=true\" to enable caching prompts");
        }
    }

    protected void debugAskCache() {
        if (Boolean.parseBoolean(System.getenv().getOrDefault(ENV_DEBUG, "false"))) {
            System.out.println("Enable caching? y/n:");
            if (new Scanner(System.in).nextLine().equals("y")) {
                System.setProperty(CACHING, "true");
            }
        } else {
            logger.info("Set \"" + ENV_DEBUG + "=true\" to enable caching prompts");
        }
    }

    protected void debugCache(@NotNull String id, @NotNull Serializable obj) {
        if (Boolean.parseBoolean(System.getenv().getOrDefault(ENV_DEBUG, "false"))) {
            cache(id, obj);
        }
    }

    protected void cache(@NotNull String id, @NotNull Serializable obj) {
        new TestDataCache<Serializable>(stage.getClass(), obj.getClass(), id, "cache/").save(obj);
    }

    protected <W extends Serializable> W getCached(@NotNull String id, Class<W> cls) {
        return new TestDataCache<W>(stage.getClass(), cls, id, "cache/").load();
    }

    @NotNull
    @DeepCopy
    protected DataRepository getDataRepository(DiagramProject diagramProject, boolean cachePreRun) {
        if (!cachePreRun) {
            return this.setup(diagramProject);
        }

        return dataRepositoryCaches.computeIfAbsent(diagramProject, dp -> TestDataRepositoryCache.getInstance(stage.getClass(), diagramProject))
                .get(this::setup);
    }

    protected DataRepository run(DiagramProject project) {
        return run(project, Map.of());
    }

    protected abstract V runComparable(DiagramProject project, Map<String, String> additionalConfigurations, boolean cachePreRun);

    protected V runComparable(DiagramProject project, Map<String, String> additionalConfigurations) {
        return runComparable(project, additionalConfigurations, true);
    }

    protected V runComparable(DiagramProject project, boolean cachePreRun) {
        return runComparable(project, Map.of(), cachePreRun);
    }

    protected V runComparable(DiagramProject project) {
        return runComparable(project, Map.of(), true);
    }

    protected abstract DataRepository runPreTestRunner(DiagramProject project);

    protected abstract DataRepository runTestRunner(DiagramProject project, Map<String, String> additionalConfigurations, DataRepository dataRepository);

    private static final int repetitions = 2;

    @BeforeAll
    void resetAllTestDataRepositoryCaches() {
        Arrays.stream(DiagramProject.values()).forEach(d -> TestDataRepositoryCache.getInstance(stage.getClass(), d).deleteFile());
        debugAskCache();
    }

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
