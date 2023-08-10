package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.prefs.Preferences;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.LoggerFactory;

/**
 * Interface for all Project extensions
 */
public interface GoldStandardProject extends Serializable {
    /**
     * {@return the project the instance is based on}
     */
    Project getProject();

    /**
     * {@return the name of all resources associated with instances relative to the class)
     */
    Set<String> getResourceNames();

    /**
     * {@return the version of the source files of this project}
     */
    default long getSourceFilesVersion() {
        getResourceNames().forEach(this::validateResourceChecksum);
        return Preferences.userNodeForPackage(getClass()).getLong("version", -1L);
    }

    /**
     * Calculates an MD5 checksum for the resource at the given path. Will bump the source files version if the checksum doesn't match.
     *
     * @param resourceName the resource relative to the class
     * @return true, if the checksum matches, false otherwise
     * @see #getSourceFilesVersion()
     */
    private boolean validateResourceChecksum(String resourceName) {
        var cls = getClass();
        var logger = LoggerFactory.getLogger(cls);
        try (var resource = cls.getResourceAsStream(resourceName)) {
            if (resource == null)
                throw new IllegalArgumentException("No such resource at path " + resourceName);
            String md5 = DigestUtils.md5Hex(resource);
            if (!Objects.equals(Preferences.userNodeForPackage(cls).get(resourceName, null), md5)) {
                Preferences.userNodeForPackage(cls).put(resourceName, md5);
                Preferences.userNodeForPackage(cls).putLong("version", System.currentTimeMillis());
                logger.info("Checksum for source file {} doesn't match", resourceName);
                return false;
            }
            logger.info("Checksum for source file {} matches", resourceName);
            return true;
        } catch (IOException e) {
            logger.error("Couldn't calculate checksum for resource at " + resourceName, e);
            return false;
        }
    }
}
