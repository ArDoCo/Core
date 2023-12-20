/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.common.util;

import java.io.*;
import java.lang.reflect.ParameterizedType;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

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
    public SerializableFileBasedCache(Class<? extends T> contentClass, String identifier, String subFolder) {
        super(identifier, ".ser", subFolder + contentClass.getSimpleName() + File.separator);
        this.contentClass = contentClass;
    }

    public SerializableFileBasedCache(TypeReference<? extends T> typeReference, String identifier, String subFolder) {
        super(identifier, ".ser", subFolder + sanitizeFileName(processTypeReference(typeReference).getSimpleName()) + File.separator);
        this.contentClass = (Class<? extends T>) processTypeReference(typeReference);
    }

    private static String sanitizeFileName(String name) {
        var noForbiddenChars = name.replaceAll("[\\\\/:*?\"<>|]", "");
        return noForbiddenChars.replace('.', '-');
    }

    private static Class<?> processTypeReference(TypeReference<?> typeReference) {
        var type = typeReference.getType();
        if (type instanceof ParameterizedType parameterizedType) {
            type = parameterizedType.getRawType();
        }
        if (type instanceof Class<?> cls) {
            return cls;
        } else {
            throw new IllegalArgumentException("TypeReference type could not be resolved to a class");
        }
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
