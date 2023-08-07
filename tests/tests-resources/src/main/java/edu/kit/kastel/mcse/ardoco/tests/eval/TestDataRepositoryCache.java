package edu.kit.kastel.mcse.ardoco.tests.eval;

import java.io.Serializable;
import java.util.HashMap;
import java.util.function.Function;
import java.util.prefs.Preferences;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.common.util.SerializableFileBasedCache;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.data.DeepCopy;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;

public class TestDataRepositoryCache extends TestDataCache<TestDataRepositoryCache.Record> {
    private static final Logger logger = LoggerFactory.getLogger(TestDataRepositoryCache.class);
    private final DiagramProject diagramProject;

    public static TestDataRepositoryCache getInstance(Class<? extends AbstractExecutionStage> stage, DiagramProject diagramProject) {
        return new TestDataRepositoryCache(stage, diagramProject);
    }

    private TestDataRepositoryCache(@NotNull Class<? extends AbstractExecutionStage> stage, DiagramProject diagramProject) {
        super(stage, TestDataRepositoryCache.Record.class, diagramProject.name(), "data-repositories/");
        this.diagramProject = diagramProject;
    }

    @Override
    public Record getDefault() {
        return new Record(new HashMap<>());
    }

    @NotNull
    @DeepCopy
    public DataRepository get(Function<DiagramProject, DataRepository> mappingFunction) {
        checkVersion();

        var record = load();
        if (!record.preRun().containsKey(diagramProject)) {
            record.preRun().put(diagramProject, mappingFunction.apply(diagramProject));
            save(record);
        }

        return record.preRun().get(diagramProject).deepCopy();
    }

    public record Record(HashMap<DiagramProject, DataRepository> preRun) implements Serializable {
    }

    private void checkVersion() {
        var versionPref = "version-" + getClass().getSimpleName() + "-" + stage.getSimpleName();
        var versionDiagramProject = diagramProject.getSourceFilesVersion();
        if (Preferences.userNodeForPackage(DiagramProject.class).getLong(versionPref, -1L) != versionDiagramProject) {
            Preferences.userNodeForPackage(DiagramProject.class).putLong(versionPref, versionDiagramProject);
            logger.warn("{}'s source files have changed, resetting {} file", diagramProject.name(), getIdentifier());
            resetFile();
        }
    }
}
