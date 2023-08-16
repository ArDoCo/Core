package edu.kit.kastel.mcse.ardoco.tests.eval;

import java.util.HashMap;
import java.util.function.Function;
import java.util.prefs.Preferences;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.data.DeepCopy;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject;

public class TestDataRepositoryCache<T extends GoldStandardProject> extends TestDataCache<HashMap<T, DataRepository>> {
    private static final Logger logger = LoggerFactory.getLogger(TestDataRepositoryCache.class);
    private final T project;

    public TestDataRepositoryCache(@NotNull Class<? extends AbstractExecutionStage> stage, T project) {
        super(stage, project.getProjectName(), "data-repositories/");
        this.project = project;
    }

    @Override
    public TestData<HashMap<T, DataRepository>> getDefault() {
        return new TestData<>(new HashMap<>());
    }

    @Override
    public TestData<HashMap<T, DataRepository>> load() {
        return super.load();
    }

    @NotNull
    @DeepCopy
    public DataRepository get(Function<T, DataRepository> mappingFunction) {
        checkVersion();

        var testData = load();
        if (!testData.data().containsKey(project)) {
            testData.data().put(project, mappingFunction.apply(project));
            save(testData);
        }

        return testData.data().get(project).deepCopy();
    }

    private void checkVersion() {
        var versionPref = "version-" + getClass().getSimpleName() + "-" + stage.getSimpleName();
        var versionDiagramProject = project.getSourceFilesVersion();
        if (Preferences.userNodeForPackage(DiagramProject.class).getLong(versionPref, -1L) != versionDiagramProject) {
            Preferences.userNodeForPackage(DiagramProject.class).putLong(versionPref, versionDiagramProject);
            logger.warn("{}'s source files have changed, resetting {} file", project.getProjectName(), getIdentifier());
            resetFile();
        }
    }
}
