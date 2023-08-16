package edu.kit.kastel.mcse.ardoco.tests.eval;

import java.io.Serializable;
import java.util.*;
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
import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class StageTest<T extends AbstractExecutionStage, U extends GoldStandardProject, V extends Record> {
    private static final String ENV_DEBUG = "debug";
    private static final String CACHING = "stageTestCaching";
    private static final Logger logger = LoggerFactory.getLogger(StageTest.class);
    private final T stage;
    private final List<U> allProjects;
    private final Set<Class<? extends PipelineAgent>> agents;
    private final Map<Class<? extends PipelineAgent>, Set<Class<? extends Informant>>> informantsMap;
    private final Map<U, TestDataRepositoryCache> dataRepositoryCaches = new HashMap<>();

    public StageTest(T stage, List<U> allProjects) {
        this.stage = stage;
        this.agents = ConfigurationUtility.getAgents(stage);
        this.informantsMap = ConfigurationUtility.getInformantsMap(stage);
        this.allProjects = allProjects;
    }

    private DataRepository setup(U project) {
        logger.info("Run PreTestRunner for {}", project.getProjectName());
        return runPreTestRunner(project);
    }

    protected DataRepository run(U project, Map<String, String> additionalConfigurations) {
        var dataRepository = getDataRepository(project, true);
        return runTestRunner(project, additionalConfigurations, dataRepository);
    }

    protected DataRepository run(U project, Map<String, String> additionalConfigurations, boolean cachePreRun) {
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
        new TestDataCache<Serializable>(stage.getClass(), id, "cache/").save(new TestData<>(obj));
    }

    protected <W extends Serializable> W getCached(@NotNull String id, Class<W> cls) {
        return new TestDataCache<W>(stage.getClass(), id, "cache/").load().data();
    }

    @NotNull
    @DeepCopy
    protected DataRepository getDataRepository(U project, boolean cachePreRun) {
        if (!cachePreRun) {
            return this.setup(project);
        }

        return dataRepositoryCaches.computeIfAbsent(project, dp -> new TestDataRepositoryCache<U>(stage.getClass(), project)).get(this::setup);
    }

    protected DataRepository run(U project) {
        return run(project, Map.of());
    }

    protected abstract V runComparable(U project, Map<String, String> additionalConfigurations, boolean cachePreRun);

    protected V runComparable(U project, Map<String, String> additionalConfigurations) {
        return runComparable(project, additionalConfigurations, true);
    }

    protected V runComparable(U project, boolean cachePreRun) {
        return runComparable(project, Map.of(), cachePreRun);
    }

    protected V runComparable(U project) {
        return runComparable(project, Map.of(), true);
    }

    protected abstract DataRepository runPreTestRunner(U project);

    protected abstract DataRepository runTestRunner(U project, Map<String, String> additionalConfigurations, DataRepository dataRepository);

    private static final int repetitions = 2;

    @BeforeAll
    void resetAllTestDataRepositoryCaches() {
        allProjects.forEach(d -> new TestDataRepositoryCache<U>(stage.getClass(), d).deleteFile());
        debugAskCache();
    }

    @DisplayName("Repetition Test Stage")
    @Test
    @Order(-1)
    void stageRepetitionTest() {
        var results = new ArrayList<V>(repetitions);
        for (var i = 0; i < repetitions; i++) {
            logger.info("Stage {} repetition {}/{}", stage.getClass().getSimpleName(), i + 1, repetitions);
            results.add(runComparable(allProjects.get(0)));
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
            results.add(runComparable(allProjects.get(0), ConfigurationUtility.enableAgents(stage.getClass(), Set.of(clazzAgent))));
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
            results.add(runComparable(allProjects.get(0), ConfigurationUtility.enableInformants(stage, Set.of(clazzInformant))));
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
