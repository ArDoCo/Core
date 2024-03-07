/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.id.tests.eval;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for {@link GoldStandardProject} implementations.
 */
public class ProjectHelper {
    /**
     * If set to false. The CodeProject will place the codeModel.acm file from the benchmark to the project directory.
     */
    public static final AtomicBoolean ANALYZE_CODE_DIRECTLY = new AtomicBoolean(false);
    private static final Logger logger = LoggerFactory.getLogger(ProjectHelper.class);

    private ProjectHelper() {
        throw new IllegalAccessError();
    }

    /**
     * Load a resource to a temporary file
     *
     * @param resource the resource path
     * @return the file if loaded or null if not possible
     */
    public static File loadFileFromResources(String resource) {
        InputStream is = ProjectHelper.class.getResourceAsStream(resource);
        if (is == null)
            return null;
        try {
            File temporaryFile = File.createTempFile("ArDoCo", ".tmp");
            temporaryFile.deleteOnExit();
            try (FileOutputStream fos = new FileOutputStream(temporaryFile)) {
                try (is) {
                    is.transferTo(fos);
                }
            }
            return temporaryFile;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
