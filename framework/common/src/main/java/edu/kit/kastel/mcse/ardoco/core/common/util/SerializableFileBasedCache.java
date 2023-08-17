package edu.kit.kastel.mcse.ardoco.core.common.util;

import java.io.*;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerializableFileBasedCache<T extends Serializable> extends FileBasedCache<T> {
    private static final Logger logger = LoggerFactory.getLogger(SerializableFileBasedCache.class);

    private T content;
    private final Class<? extends T> contentClass;

    public SerializableFileBasedCache(Class<? extends T> contentClass, @NotNull String identifier, @NotNull String subFolder) {
        super(identifier, ".ser", subFolder);
        this.contentClass = contentClass;
    }

    @Override
    public void save(T content) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(getFile()))) {
            out.writeObject(content);
            logger.info("Saved {} file", getIdentifier());
        } catch (IOException e) {
            logger.error("Error reading file", e);
        }
    }

    @Override
    @SuppressWarnings("uncecked")
    public @Nullable T load(boolean allowReload) {
        if (content != null)
            return content;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(getFile()))) {
            logger.info("Reading {} file", getIdentifier());
            var dObj = in.readObject();
            if (dObj == null || contentClass.isInstance(dObj)) {
                content = (T) dObj;
                return content;
            }
            throw new ClassCastException();
        } catch (InvalidClassException | ClassNotFoundException | ClassCastException | EOFException e) {
            if (allowReload) {
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
    public Optional<T> get() {
        return Optional.ofNullable(content);
    }

    @Override
    public T getDefault() {
        return null;
    }
}
