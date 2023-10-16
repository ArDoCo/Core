package edu.kit.kastel.mcse.ardoco.core.common.util;

import java.io.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link FileBasedCache} that is implemented using Java's default serialization.
 *
 * @param <T> serializable content
 */
public class SerializableFileBasedCache<T extends Serializable> extends FileBasedCache<T> {
    private static final Logger logger = LoggerFactory.getLogger(SerializableFileBasedCache.class);

    private final Class<? extends T> contentClass;

    /**
     * Creates a new serializable file based cache that contains content of the given class and is saved in a file with the given identifier and sub-folder.
     *
     * @param contentClass the class of serializable content
     * @param identifier   the identifier of the cache
     * @param subFolder    the sub-folder of the cache
     */
    public SerializableFileBasedCache(@NotNull Class<? extends T> contentClass, @NotNull String identifier, @NotNull String subFolder) {
        super(identifier, ".ser", subFolder + contentClass.getSimpleName() + "/");
        this.contentClass = contentClass;
    }

    @Override
    protected void write(T content) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(getFile()))) {
            out.writeObject(content);
            logger.info("Saved {} file", getIdentifier());
        } catch (IOException e) {
            logger.error("Error reading file", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected @Nullable T read() throws CacheException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(getFile()))) {
            logger.info("Reading {} file", getIdentifier());
            var dObj = in.readObject();
            if (dObj == null || contentClass.isInstance(dObj)) {
                return (T) dObj;
            }
            throw new ClassCastException();
        } catch (InvalidClassException | ClassNotFoundException | ClassCastException | EOFException e) {
            logger.warn("SerialVersionUID might have changed, resetting {} file", getIdentifier());
            throw new CacheException(e);
        } catch (IOException e) {
            logger.error("Error reading {} file", getIdentifier());
            throw new CacheException(e);
        }
    }

    @Override
    protected T getDefault() {
        return null;
    }
}
