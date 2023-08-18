package edu.kit.kastel.mcse.ardoco.core.common.util;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

public abstract class FileBasedCache<T> implements AutoCloseable {
    private static Logger logger = LoggerFactory.getLogger(FileBasedCache.class);
    private File file;
    private final String identifier;
    private final String fileExtension;
    private final String subFolder;
    private boolean flagRead = false;
    private boolean flagWrite = false;
    private T currentState = null;
    private T originalState = null;

    protected abstract void write(T content);

    public T getOrRead() {
        if (currentState == null) {
            try {
                originalState = read();
            } catch (CacheException e) {
                try {
                    resetFile();
                    originalState = read();
                } catch (CacheException ex) {
                    //If resetting doesn't solve the issue, fail entirely
                    throw new RuntimeException(ex);
                }
            }
            currentState = originalState;
            flagRead = true;
        }
        return currentState;
    }

    public Optional<T> get() {
        return Optional.ofNullable(currentState);
    }

    public void cache(T cache) {
        currentState = cache;
        flagWrite = true;
    }

    protected FileBasedCache(@NotNull String identifier, @NotNull String fileExtension, @NotNull String subFolder) {
        this.identifier = identifier;
        this.fileExtension = fileExtension;
        if (!subFolder.isEmpty() && !subFolder.endsWith("/"))
            throw new IllegalArgumentException();
        this.subFolder = subFolder;
    }

    protected abstract T read() throws CacheException;

    protected abstract T getDefault();

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

    protected boolean deleteFile() {
        try {
            if (file == null)
                file = getFileHandle();
            return file.delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected File getFileHandle() throws IOException {
        AppDirs appDirs = AppDirsFactory.getInstance();
        var arDoCoDataDir = appDirs.getUserDataDir("ArDoCo", null, "MCSE", true);
        file = new File(arDoCoDataDir + "/" + subFolder + this.identifier + this.fileExtension);
        if (file.getParentFile().mkdirs()) {
            logger.info("Created directory {}", file.getParentFile().getCanonicalPath());
        }
        return file;
    }

    protected File getFile() throws IOException {
        if (file != null && file.exists())
            return file;

        file = getFileHandle();

        if (file.createNewFile()) {
            logger.info("Created {} file {}", this.identifier, file.getCanonicalPath());
            T defaultContent = getDefault();
            write(defaultContent);
        }

        return file;
    }

    @Override
    public void close() {
        if (flagWrite) {
            if (currentState == null) {
                deleteFile();
            } else {
                if (currentState != originalState) {
                    write(currentState);
                }
            }
        }
    }
}
