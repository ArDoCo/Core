/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.id.tests.eval.helper;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.collections.impl.factory.SortedMaps;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.configuration.ConfigurationUtility;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.data.DeepCopy;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.id.tests.eval.GoldStandardProject;

/**
 * This test base class can be used for testing stages. Conceptually, a stage test is divided into the pre-test runner and the test runner. The pre-test runner
 * is executed once per project. The pre-test runner is defined and executed in {@link #runPreTestRunner} by the implementing subclasses. It should contain all
 * stages that are considered pre-requisites to the stage this test is supposed to test. The resulting data repository is cloned (deep copy) and used as a basis
 * for the test runner, which needs to be implemented in {@link #runTestRunner}. Each execution of the stage needs to produce an instance of the provided
 * record. Thus, subclasses should implement {@link #runComparable} to provide such a record.<br><br> This class also provides functionality to automatically
 * test if the results of a stage are invariant to a specific amount of repetitions. The repetition test occur on a stage, agent and informant level, where each
 * is individually tested for invariance.<br><br>The class provides the two environment variables {@link #ENV_DEBUG} and {@link #ENV_DEBUG_KEEP_CACHE} to
 * configure the caching behaviour. By default, no persistent caching occurs. {@link #ENV_DEBUG} enables CLI prompts at the beginning of the stage test, which
 * ask whether results should be cached (for debugging purposes, comparison to prior runs et cetera). {@link #ENV_DEBUG_KEEP_CACHE} prevents the pre-run cache
 * from being wiped prior before all tests are executed, which means that the pre-requisites do not have to be run again. This is useful, if a stage has a large
 * number of pre-requisite stages, which do not change frequently, but require a lot of time to execute.
 *
 * @param <T> The stage tested by this class
 * @param <U> The type of project that should be used
 * @param <V> The record produced by the execution of the test runners. The record must implement {@link V#equals(Object)} independent of the identity of its
 *            constituents. This is necessary, because the equality of the record is considered when checking for non-deterministic behaviour. The record should
 *            summarize the relevant results of the stage
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class StageTest<T extends AbstractExecutionStage, U extends GoldStandardProject, V extends Record> implements Serializable {
    /**
     * Enables a prompt that sets the {@link #CACHING} system property used by {@link #debugCacheIfCachingFlag}.
     */
    private static final String ENV_DEBUG = "debug";
    /**
     * Whether the cache of pre-test run data repositories should be cleared before all tests of this stage are executed.
     */
    private static final String ENV_DEBUG_KEEP_CACHE = "debugKeepCache";
    /**
     * Enables caching in {@link #debugCacheIfCachingFlag}.
     */
    private static final String CACHING = "stageTestCaching";
    private static final Logger logger = LoggerFactory.getLogger(StageTest.class);
    private final transient T stage;
    private final transient List<U> allProjects;
    private final transient Set<Class<? extends PipelineAgent>> agents;
    private final transient Map<Class<? extends PipelineAgent>, Set<Class<? extends Informant>>> informantsMap;
    private final transient Map<U, TestDataRepositoryCache<U>> dataRepositoryCaches = new HashMap<>();

    /**
     * Sole constructor of stage tests.
     *
     * @param stage       dummy instance of the stage that should be tested
     * @param allProjects all projects that should be used for testing
     */
    public StageTest(T stage, U[] allProjects) {
        this.stage = stage;
        this.agents = ConfigurationUtility.getAgents(stage);
        this.informantsMap = ConfigurationUtility.getInformantsMap(stage);
        this.allProjects = List.of(allProjects);
    }

    /**
     * {@return the data repository that is created by executing the pre-runner for the project}
     *
     * @param project the project that is executed by the pre-runner
     */
    private DataRepository setup(U project) {
        logger.info("Run PreTestRunner for {}", project.getProjectName());
        var preRunDataRepository = runPreTestRunner(project);
        logger.info("Finished PreTestRunner for {}", project.getProjectName());
        return preRunDataRepository;
    }

    /**
     * Runs the test-runner for the specified project. Before the test-runner is executed, the pre-runner data repository is retrieved (Either via execution of
     * the pre-runner or from the cache).
     *
     * @param project the project
     * @return the data repository produced by the test-runner
     */
    protected DataRepository run(U project) {
        return run(project, SortedMaps.mutable.empty());
    }

    /**
     * Runs the test-runner for the specified project using the supplied additional configurations. Before the test-runner is executed, the pre-runner data
     * repository is retrieved (Either via execution of the pre-runner or from the cache).
     *
     * @param project                  the project
     * @param additionalConfigurations a map of additional configurations
     * @return the data repository produced by the test-runner
     */
    protected DataRepository run(U project, SortedMap<String, String> additionalConfigurations) {
        return run(project, additionalConfigurations, true);
    }

    /**
     * Runs the test-runner for the specified project using the supplied additional configurations. Before the test-runner is executed, the pre-runner data
     * repository is retrieved (Either via execution of the pre-runner or from the cache).
     *
     * @param project                  the project
     * @param additionalConfigurations a map of additional configurations
     * @param cachePreRun              if false, the cache is ignored and the pre-runner is executed again
     * @return the data repository produced by the test-runner
     */
    protected DataRepository run(U project, SortedMap<String, String> additionalConfigurations, boolean cachePreRun) {
        var preRunDataRepository = getDataRepository(project, cachePreRun);
        logger.info("Run TestRunner for {}", project.getProjectName());
        var dataRepository = runTestRunner(project, additionalConfigurations, preRunDataRepository);
        logger.info("Finished TestRunner for {}", project.getProjectName());
        return dataRepository;
    }

    /**
     * If {@link #ENV_DEBUG} is set, a CLI prompt will ask the user whether they want to cache the provided object in the test data cache with the provided
     * identifier. This can be used to cache serializable objects for debugging between program executions.
     *
     * @param id  the identifier of the test data cache
     * @param obj the object that should be cached
     */
    protected void debugCacheWithPrompt(String id, Serializable obj) {
        if (Boolean.parseBoolean(System.getenv().getOrDefault(ENV_DEBUG, Boolean.FALSE.toString()))) {
            System.out.println("Cache " + obj.getClass().getSimpleName() + " at " + id + "? y/n:");
            if (new Scanner(System.in).nextLine().equals("y")) {
                cache(id, obj);
            }
        } else {
            logger.info("Set \"" + ENV_DEBUG + "=true\" to enable caching prompts");
        }
    }

    /**
     * If {@link #ENV_DEBUG} is set, a CLI prompt will ask the user whether they want to enable caching. If caching is disabled, calls to
     * {@link #debugCacheIfCachingFlag} are ignored.
     */
    protected void debugSetCachingFlag() {
        if (Boolean.parseBoolean(System.getenv().getOrDefault(ENV_DEBUG, Boolean.FALSE.toString()))) {
            System.out.println("Enable caching? y/n:");
            if (new Scanner(System.in).nextLine().equals("y")) {
                System.setProperty(CACHING, "true");
            }
        } else {
            logger.info("Set \"" + ENV_DEBUG + "=true\" to enable caching prompts");
        }
    }

    /**
     * If caching is enabled, the provided object is cached in the test data cache with the provided identifier. Otherwise, calls to this function are ignored.
     * This can be used to cache serializable objects for debugging between program executions.
     *
     * @param id  the identifier of the test data cache
     * @param obj the object that should be cached
     */
    protected void debugCacheIfCachingFlag(String id, Serializable obj) {
        if (Boolean.parseBoolean(System.getProperty(CACHING, "false"))) {
            cache(id, obj);
        }
    }

    /**
     * The provided object is cached in the test data cache with the provided identifier. This can be used to cache serializable objects for debugging between
     * program executions.
     *
     * @param id  the identifier of the test data cache
     * @param obj the object that should be cached
     */
    protected void cache(String id, Serializable obj) {
        try (TestDataCache<Serializable> drCache = new TestDataCache<>(stage.getClass(), obj.getClass(), id, "cache/") {
        }) {
            drCache.cache(obj);
        }
    }

    /**
     * {@return the cached object from the test data cache with the specified identifier} The provided class should match the serialized object.
     *
     * @param id  the identifier of the test data cache
     * @param cls the class object of the serialized object
     * @param <W> the type of the serialized object
     */
    protected <W extends Serializable> W getCached(String id, Class<W> cls) {
        try (TestDataCache<W> drCache = new TestDataCache<>(stage.getClass(), cls, id, "cache/")) {
            return drCache.getOrRead();
        }
    }

    /**
     * {@return the pre-runner data repository for the specified project} The first execution of the pre-runner is cached to speed up repeated calls to the
     * function for the same project and pipeline configuration. If the data repository is retrieved from the cache, a deep copy is provided that can be
     * modified without restraints.
     *
     * @param project     the project
     * @param cachePreRun if false, the cache is ignored and the pre-runner is executed again
     */

    @DeepCopy
    protected DataRepository getDataRepository(U project, boolean cachePreRun) {
        if (!cachePreRun) {
            return this.setup(project);
        }

        try (TestDataRepositoryCache<U> drCache = dataRepositoryCaches.computeIfAbsent(project, dp -> new TestDataRepositoryCache<>(stage.getClass(),
                project))) {
            return drCache.get(this::setup);
        }
    }

    /**
     * Runs the test-runner for the specified project with the supplied additional configurations using {@link #run} and creates a record that summarizes the
     * results of the run.
     *
     * @param project                  the project
     * @param additionalConfigurations a map of additional configurations
     * @param cachePreRun              if false, the cache is ignored and the pre-runner is executed again
     * @return the record that is produced from the test-runner data repository
     */
    protected abstract V runComparable(U project, SortedMap<String, String> additionalConfigurations, boolean cachePreRun);

    /**
     * Runs the test-runner for the specified project with the supplied additional configurations using {@link #run} and creates a record that summarizes the
     * results of the run.
     *
     * @param project                  the project
     * @param additionalConfigurations a map of additional configurations
     * @return the record that is produced from the test-runner data repository
     */
    protected V runComparable(U project, SortedMap<String, String> additionalConfigurations) {
        return runComparable(project, additionalConfigurations, true);
    }

    /**
     * Runs the test-runner for the specified project using {@link #run} and creates a record that summarizes the results of the run.
     *
     * @param project     the project
     * @param cachePreRun if false, the cache is ignored and the pre-runner is executed again
     * @return the record that is produced from the test-runner data repository
     */
    protected V runComparable(U project, boolean cachePreRun) {
        return runComparable(project, SortedMaps.mutable.empty(), cachePreRun);
    }

    /**
     * Runs the test-runner for the specified project using {@link #run} and creates a record that summarizes the results of the run.
     *
     * @param project the project
     * @return the record that is produced from the test-runner data repository
     */
    protected V runComparable(U project) {
        return runComparable(project, SortedMaps.mutable.empty(), true);
    }

    /**
     * Runs the pre-runner and returns the data repository produced by it. The pre-runner should include all stages that are considered pre-requisites to the
     * execution of the test-runner. For simple pre-runners, it is advisable to define an
     * {@link edu.kit.kastel.mcse.ardoco.core.execution.runner.AnonymousRunner AnonymousRunner} inside this function. A more complex pre-runner should be
     * encapsulated in a dedicated {@link edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner ArDoCoRunner} test class. It is advisable to run the
     * runner without saving, e.g. {@link ArDoCoRunner#runWithoutSaving()}.
     *
     * @param project the project that is executed by the pre-runner
     * @return the original data repository that was produced by the pre-runner
     */
    protected abstract DataRepository runPreTestRunner(U project);

    /**
     * Runs the test-runner and returns the data repository produced by it. The test-runner should include the stage being tested. For test-runners, it is
     * advisable to define an {@link edu.kit.kastel.mcse.ardoco.core.execution.runner.AnonymousRunner AnonymousRunner} inside this function and passing the
     * {@code preRunDataRepository} to it.
     *
     * @param project                  the project that is executed by the test-runner
     * @param additionalConfigurations additional configurations for the test-runner execution
     * @param preRunDataRepository     a deep copy of the pre-runner data repository
     * @return the original data repository that was produced by the test-runner
     */
    protected abstract DataRepository runTestRunner(U project, SortedMap<String, String> additionalConfigurations,
            @DeepCopy DataRepository preRunDataRepository);

    private static final int REPETITIONS = 2;

    /**
     * If {@link #ENV_DEBUG_KEEP_CACHE} is not set, this function resets the pre-runner data repository caches for all projects.
     */
    @BeforeAll
    protected void resetAllTestDataRepositoryCaches() {
        resetAllTestDataRepositoryCaches(false);
    }

    /**
     * If {@code force=true} or {@link #ENV_DEBUG_KEEP_CACHE} is not set, this function resets the pre-runner data repository caches for all projects.
     *
     * @param force whether a reset should be forced
     */
    protected void resetAllTestDataRepositoryCaches(boolean force) {
        debugSetCachingFlag();
        if (!force && Boolean.parseBoolean(System.getenv().getOrDefault(ENV_DEBUG_KEEP_CACHE, "false"))) {
            logger.warn("Keeping caches, careful! Set \"" + ENV_DEBUG_KEEP_CACHE + "=false\" to disable persistent caching");
        } else {
            for (U d : allProjects) {
                try (var drCache = new TestDataRepositoryCache<>(stage.getClass(), d)) {
                    drCache.cache(null);
                }
            }
        }
    }

    /**
     * Runs the stage multiple times using the same pre-runner data and checks whether the results are equal.
     */
    @DisplayName("Repetition Test Stage")
    @Test
    @Order(-1)
    void stageRepetitionTest() {
        var results = new ArrayList<V>(REPETITIONS);
        for (var i = 0; i < REPETITIONS; i++) {
            logger.info("Stage {} repetition {}/{}", stage.getClass().getSimpleName(), i + 1, REPETITIONS);
            results.add(runComparable(allProjects.get(0)));
        }
        Assertions.assertEquals(1, results.stream().distinct().toList().size());
    }

    /**
     * Runs the agent multiple times using the same pre-runner data and checks whether the results are equal. All other agents are disabled.
     *
     * @param clazzAgent the class object of the pipeline agent that is being tested
     */
    @DisplayName("Repetition Test Agents")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getAgents")
    @Order(-2)
    void agentRepetitionTest(Class<? extends PipelineAgent> clazzAgent) {
        var results = new ArrayList<V>(REPETITIONS);
        for (var i = 0; i < REPETITIONS; i++) {
            logger.info("Agent {} repetition {}/{}", clazzAgent.getSimpleName(), i + 1, REPETITIONS);
            results.add(runComparable(allProjects.get(0), ConfigurationUtility.enableAgents(stage.getClass(), Set.of(clazzAgent))));
        }
        Assertions.assertEquals(1, results.stream().distinct().toList().size());
    }

    /**
     * Runs the informant multiple times using the same pre-runner data and checks whether the results are equal. All other agents and all other informants are
     * disabled.
     *
     * @param clazzInformant the class object of the pipeline informants that is being tested
     */
    @DisplayName("Repetition Test Informants")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getInformants")
    @Order(-3)
    void informantRepetitionTest(Class<? extends Informant> clazzInformant) {
        var results = new ArrayList<V>(REPETITIONS);
        for (var i = 0; i < REPETITIONS; i++) {
            logger.info("Informant {} repetition {}/{}", clazzInformant.getSimpleName(), i + 1, REPETITIONS);
            results.add(runComparable(allProjects.get(0), ConfigurationUtility.enableInformants(stage, Set.of(clazzInformant))));
        }
        Assertions.assertEquals(1, results.stream().distinct().toList().size());
    }

    /**
     * {@return all class objects of the agents from the stage}
     */
    public Set<Class<? extends PipelineAgent>> getAgents() {
        return this.agents;
    }

    /**
     * {@return all class objects of the informants from the stage}
     */
    public Set<Class<? extends Informant>> getInformants() {
        return informantsMap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }
}
