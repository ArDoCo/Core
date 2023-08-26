package edu.kit.kastel.mcse.ardoco.tests.eval;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.collections.impl.factory.SortedMaps;
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
public abstract class StageTest<T extends AbstractExecutionStage, U extends GoldStandardProject, V extends Record> implements Serializable {
    private static final String ENV_DEBUG = "debug";
    private static final String ENV_DEBUG_KEEP_CACHE = "debugKeepCache";
    private static final String CACHING = "stageTestCaching";
    private static final Logger logger = LoggerFactory.getLogger(StageTest.class);
    private transient final T stage;
    private transient final List<U> allProjects;
    private transient final Set<Class<? extends PipelineAgent>> agents;
    private transient final Map<Class<? extends PipelineAgent>, Set<Class<? extends Informant>>> informantsMap;
    private transient final Map<U, TestDataRepositoryCache<U>> dataRepositoryCaches = new HashMap<>();

    public StageTest(T stage, U[] allProjects) {
        this.stage = stage;
        this.agents = ConfigurationUtility.getAgents(stage);
        this.informantsMap = ConfigurationUtility.getInformantsMap(stage);
        this.allProjects = List.of(allProjects);
    }

    private DataRepository setup(U project) {
        logger.info("Run PreTestRunner for {}", project.getProjectName());
        var preRunDataRepository = runPreTestRunner(project);
        logger.info("Finished PreTestRunner for {}", project.getProjectName());
        return preRunDataRepository;
    }

    protected DataRepository run(U project, SortedMap<String, String> additionalConfigurations) {
        return run(project, additionalConfigurations, true);
    }

    protected DataRepository run(U project, SortedMap<String, String> additionalConfigurations, boolean cachePreRun) {
        var preRunDataRepository = getDataRepository(project, cachePreRun);
        logger.info("Run TestRunner for {}", project.getProjectName());
        var dataRepository = runTestRunner(project, additionalConfigurations, preRunDataRepository);
        logger.info("Finished TestRunner for {}", project.getProjectName());
        return dataRepository;
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
        try (TestDataCache<Serializable> drCache = new TestDataCache<>(stage.getClass(), obj.getClass(), id, "cache/") {
        }) {
            drCache.cache(obj);
        }
    }

    protected <W extends Serializable> W getCached(@NotNull String id, Class<W> cls) {
        try (TestDataCache<W> drCache = new TestDataCache<>(stage.getClass(), cls, id, "cache/")) {
            return drCache.getOrRead();
        }
    }

    @NotNull
    @DeepCopy
    protected DataRepository getDataRepository(U project, boolean cachePreRun) {
        if (!cachePreRun) {
            return this.setup(project);
        }

        try (TestDataRepositoryCache<U> drCache = dataRepositoryCaches.computeIfAbsent(project,
                dp -> new TestDataRepositoryCache<>(stage.getClass(), project))) {
            return drCache.get(this::setup);
        }
    }

    protected DataRepository run(U project) {
        return run(project, SortedMaps.mutable.empty());
    }

    protected abstract V runComparable(U project, SortedMap<String, String> additionalConfigurations, boolean cachePreRun);

    protected V runComparable(U project, SortedMap<String, String> additionalConfigurations) {
        return runComparable(project, additionalConfigurations, true);
    }

    protected V runComparable(U project, boolean cachePreRun) {
        return runComparable(project, SortedMaps.mutable.empty(), cachePreRun);
    }

    protected V runComparable(U project) {
        return runComparable(project, SortedMaps.mutable.empty(), true);
    }

    protected abstract DataRepository runPreTestRunner(U project);

    protected abstract DataRepository runTestRunner(U project, SortedMap<String, String> additionalConfigurations, DataRepository preRunDataRepository);

    private static final int repetitions = 2;

    @BeforeAll
    protected void resetAllTestDataRepositoryCaches() {
        resetAllTestDataRepositoryCaches(false);
    }

    protected void resetAllTestDataRepositoryCaches(boolean force) {
        debugAskCache();
        if (!force && Boolean.parseBoolean(System.getenv().getOrDefault(ENV_DEBUG_KEEP_CACHE, "false"))) {
            logger.warn("Keeping caches, careful! Set \"" + ENV_DEBUG_KEEP_CACHE + "=false\" to disable" + " persistent caching");
        } else {
            for (U d : allProjects) {
                try (var drCache = new TestDataRepositoryCache<>(stage.getClass(), d)) {
                    drCache.cache(null);
                }
            }
        }
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
