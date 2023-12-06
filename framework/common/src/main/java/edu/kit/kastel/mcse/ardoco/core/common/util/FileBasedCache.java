/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.common.util;

import java.io.File;
import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

/**
 * This abstract class provides the structure for a file-based cache. Only one instance should be created for each cache file. The cache files are saved in the
 * user data directory folder of ArDoCo.
 *
 * @param <T> the type of cached content
 */
public abstract class FileBasedCache<T> implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(FileBasedCache.class);
    private File file;
    private final String identifier;
    private final String fileExtension;
    private final String subFolder;
    private boolean flagRead = false;
    private boolean flagWrite = false;
    private T currentState = null;
    private int originalStateHash;

    /**
     * Writes the content to the file at {@link #getFile()}
     *
     * @param content the content
     */
    protected abstract void write(T content);

    /**
     * {@return the cached content}
     */
    public T getOrRead() {
        if (currentState == null) {
            T fileState = null;
            try {
                fileState = read();
            } catch (CacheException e) {
                try {
                    resetFile();
                    fileState = read();
                } catch (CacheException ex) {
                    //If resetting doesn't solve the issue, fail entirely
                    throw new RuntimeException(ex);
                }
            }
            originalStateHash = fileState.hashCode();
            currentState = fileState;
            flagRead = true;
        }
        return currentState;
    }

    /**
     * Caches the content. This does not write it to the disk immediately.
     *
     * @param content the content
     */
    public void cache(T content) {
        currentState = content;
        flagWrite = true;
    }

    /**
     * Constructor for the file-based cache
     *
     * @param identifier    name of the cache file
     * @param fileExtension extension of the cache file
     * @param subFolder     sub-folder in the user directory, must end with "/"
     */
    protected FileBasedCache(@NotNull String identifier, @NotNull String fileExtension, @NotNull String subFolder) {
        this.identifier = identifier;
        this.fileExtension = fileExtension;
        if (!subFolder.isEmpty() && !subFolder.endsWith("/"))
            throw new IllegalArgumentException();
        this.subFolder = subFolder;
    }

    /**
     * Reads the content of the file at {@link #getFile()}
     *
     * @throws CacheException thrown if an error occurs while reading
     */
    protected abstract T read() throws CacheException;

    /**
     * {@return the default content that is written if the file is reset using {@link #resetFile()}}
     */
    protected abstract T getDefault();

    /**
     * {@return the name of the cache file}
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * Resets the cache file to default content.
     */
    public void resetFile() {
        try {
            deleteFile();
            getFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes the file at {@link #getFileHandle()}
     *
     * @return whether the file was deleted successfully by the file system
     */
    protected boolean deleteFile() {
        try {
            if (file == null)
                file = getFileHandle();
            return file.delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@return the file handle of the cache file}
     *
     * @throws IOException on a file system exception
     */
    protected File getFileHandle() throws IOException {
        AppDirs appDirs = AppDirsFactory.getInstance();
        var arDoCoDataDir = appDirs.getUserDataDir("ArDoCo", null, "MCSE", true);
        file = new File(arDoCoDataDir + "/" + subFolder + this.identifier + this.fileExtension);
        if (file.getParentFile().mkdirs()) {
            logger.info("Created directory {}", file.getParentFile().getCanonicalPath());
        }
        return file;
    }

    /**
     * {@return the cache file}
     *
     * @throws IOException on a file system exception
     */
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
                if (currentState.hashCode() != originalStateHash) {
                    write(currentState);
                }
            }
        }
    }
}
