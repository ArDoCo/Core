package edu.kit.kastel.mcse.ardoco.core.common.util;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

public abstract class FileBasedCache<T> {
    private static Logger logger = LoggerFactory.getLogger(AbbreviationDisambiguationHelper.class);
    private File file;

    public abstract void save(T content);

    public abstract T load();

    public abstract String getIdentifier();

    public abstract T getDefault();

    public File getFile() throws IOException {
        if (file != null)
            return file;

        AppDirs appDirs = AppDirsFactory.getInstance();
        var arDoCoDataDir = appDirs.getUserDataDir("ArDoCo", null, "MCSE", true);
        //var projectDataDir = arDoCoDataDir + "/projects/" + DataRepositoryHelper.getProjectPipelineData(dataRepository).getProjectName();
        file = new File(arDoCoDataDir + "/" + getIdentifier() + ".json");
        if (file.getParentFile().mkdirs()) {
            logger.info("Created directory {}", file.getParentFile().getCanonicalPath());
        }
        if (file.createNewFile()) {
            logger.info("Created {} file {}", getIdentifier(), file.getCanonicalPath());
            T defaultContent = getDefault();
            save(defaultContent);
        }

        return file;
    }
}
