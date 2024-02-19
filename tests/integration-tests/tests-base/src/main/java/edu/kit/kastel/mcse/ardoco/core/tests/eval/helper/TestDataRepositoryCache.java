/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval.helper;

import java.util.HashMap;
import java.util.function.Function;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.data.DeepCopy;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject;

/**
 * Persistent cache for {@link StageTest} a pre-runner data repository. For any given project, the pre-runner data repository is cached in ArDoCo's user
 * directory folder in the {@code data-repositories/} sub-folder of the {@link TestDataCache test data directory}.
 *
 * @param <T> the type of project
 */
public class TestDataRepositoryCache<T extends GoldStandardProject> extends TestDataCache<HashMap<T, DataRepository>> {
    private static final Logger logger = LoggerFactory.getLogger(TestDataRepositoryCache.class);
    private final T project;

    /**
     * Creates a new test data repository cache for the given stage and project.
     *
     * @param stage   the stage
     * @param project the project
     */
    public TestDataRepositoryCache(Class<? extends AbstractExecutionStage> stage, T project) {
        super(stage, new TypeReference<HashMap<T, DataRepository>>() {
        }, project.getProjectName(), "data-repositories/");
        this.project = project;
    }

    @Override
    public HashMap<T, DataRepository> getDefault() {
        return new HashMap<>();
    }

    /**
     * Gets a deep copy of the cached data repository. If no data repository is cached, the mapping function is used to compute it and the result is cached,
     * deep copied and returned. The cache is automatically invalidated and reset, if the source files of a project (such as the text, gold standards, etc.)
     * have changed.
     *
     * @param mappingFunction a function to compute the data repository of a project
     * @return a deep copy of the data repository
     */
    @DeepCopy
    public DataRepository get(Function<T, DataRepository> mappingFunction) {
        checkVersion();

        var testData = getOrRead();
        if (!testData.containsKey(project)) {
            testData.put(project, mappingFunction.apply(project));
            this.write(testData);
        }

        return testData.get(project).deepCopy();
    }

    /**
     * Checks whether the version of the source files has changed.
     */
    private void checkVersion() {
        var versionPref = "version-" + getClass().getSimpleName() + "-" + stage.getSimpleName();
        var versionProject = project.getSourceFilesVersion();
        if (Preferences.userNodeForPackage(project.getClass()).getLong(versionPref, -1L) != versionProject) {
            Preferences.userNodeForPackage(project.getClass()).putLong(versionPref, versionProject);
            logger.warn("{}'s source files have changed, resetting {} file", project.getProjectName(), getIdentifier());
            resetFile();
        }
    }
}
