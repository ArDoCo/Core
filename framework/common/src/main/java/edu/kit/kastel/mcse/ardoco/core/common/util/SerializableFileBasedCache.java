package edu.kit.kastel.mcse.ardoco.core.common.util;

import java.io.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerializableFileBasedCache<T extends Serializable> extends FileBasedCache<T> {
    private static final Logger logger = LoggerFactory.getLogger(SerializableFileBasedCache.class);

    private final Class<? extends T> contentClass;

    protected SerializableFileBasedCache(Class<? extends T> contentClass, @NotNull String identifier, @NotNull String subFolder) {
        super(identifier, ".ser", subFolder);
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
    @SuppressWarnings("uncecked")
    protected @Nullable T read() throws CacheException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(getFile()))) {
            logger.info("Reading {} file", getIdentifier());
            var dObj = in.readObject();
            if (dObj == null || contentClass.isInstance(dObj)) {
                var content = (T) dObj;
                return content;
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
