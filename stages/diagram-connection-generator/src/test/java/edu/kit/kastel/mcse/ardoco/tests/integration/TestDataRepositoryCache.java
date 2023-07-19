package edu.kit.kastel.mcse.ardoco.tests.integration;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.function.Function;
import java.util.prefs.Preferences;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.common.util.FileBasedCache;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;

public class TestDataRepositoryCache extends FileBasedCache<TestDataRepositoryCache.Record> {
    private static final Logger logger = LoggerFactory.getLogger(TestDataRepositoryCache.class);
    private final Class<? extends AbstractExecutionStage> stage;
    private final DiagramProject diagramProject;
    private Record record;

    public static TestDataRepositoryCache getInstance(Class<? extends AbstractExecutionStage> stage, DiagramProject diagramProject) {
        return new TestDataRepositoryCache(stage, diagramProject);
    }

    private TestDataRepositoryCache(@NotNull Class<? extends AbstractExecutionStage> stage, DiagramProject diagramProject) {
        super(diagramProject.name(), ".ser", "test/data-repositories/" + stage.getSimpleName() + "/");
        this.stage = stage;
        this.diagramProject = diagramProject;
    }

    @Override
    public void save(Record content) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(getFile()))) {
            out.writeObject(content);
            logger.info("Saved {} file", getIdentifier());
        } catch (IOException e) {
            logger.error(e.getCause().getMessage());
        }
    }

    @Override
    public Record load(boolean allowReload) {
        if (record != null)
            return record;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(getFile()))) {
            logger.info("Reading {} file", getIdentifier());
            record = (Record) in.readObject();
            return record;
        } catch (InvalidClassException | ClassNotFoundException e) {
            if (allowReload) {
                //TODO Fix on change
                logger.warn("SerialVersionUID might have changed, resetting {} file", getIdentifier());
                resetFile();
                return load(false);
            } else {
                logger.error("Error reading {} file, reload is disabled", getIdentifier());
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            logger.error("Error reading {} file", getIdentifier());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Record getDefault() {
        return new Record(new HashMap<>());
    }

    public @NotNull DataRepository get(Function<DiagramProject, DataRepository> mappingFunction) {
        checkVersion();

        var record = load();
        if (!record.preRun().containsKey(diagramProject)) {
            record.preRun().put(diagramProject, mappingFunction.apply(diagramProject));
            save(record);
        }

        return record.preRun().get(diagramProject);
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
