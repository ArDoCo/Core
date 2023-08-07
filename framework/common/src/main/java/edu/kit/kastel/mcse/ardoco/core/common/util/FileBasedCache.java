package edu.kit.kastel.mcse.ardoco.core.common.util;

import java.io.File;
import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

public abstract class FileBasedCache<T> {
    private static Logger logger = LoggerFactory.getLogger(FileBasedCache.class);
    private File file;
    private final String identifier;
    private final String fileExtension;
    private final String subFolder;

    public abstract void save(T content);

    protected FileBasedCache(@NotNull String identifier, @NotNull String fileExtension, @NotNull String subFolder) {
        this.identifier = identifier;
        this.fileExtension = fileExtension;
        if (!subFolder.isEmpty() && !subFolder.endsWith("/"))
            throw new IllegalArgumentException();
        this.subFolder = subFolder;
    }

    public T load() {
        return load(true);
    }

    public abstract T load(boolean allowReload);

    public abstract T getDefault();

    public String getIdentifier() {
        return this.identifier;
    }

    public void resetFile() {
        try {
            deleteFile();
            getFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteFile() {
        try {
            if (file == null)
                file = getFileHandle();
            return file.delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File getFileHandle() throws IOException {
        AppDirs appDirs = AppDirsFactory.getInstance();
        var arDoCoDataDir = appDirs.getUserDataDir("ArDoCo", null, "MCSE", true);
        file = new File(arDoCoDataDir + "/" + subFolder + this.identifier + this.fileExtension);
        if (file.getParentFile().mkdirs()) {
            logger.info("Created directory {}", file.getParentFile().getCanonicalPath());
        }
        return file;
    }

    public File getFile() throws IOException {
        if (file != null && file.exists())
            return file;

        file = getFileHandle();

        if (file.createNewFile()) {
            logger.info("Created {} file {}", this.identifier, file.getCanonicalPath());
            T defaultContent = getDefault();
            save(defaultContent);
        }

        return file;
    }
}
