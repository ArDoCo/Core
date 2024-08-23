/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Helper class for {@link GoldStandardProject} implementations.
 */
public class ProjectHelper {
    /**
     * If set to false. The CodeProject will place the codeModel.acm file from the benchmark to the project directory.
     */
    public static final AtomicBoolean ANALYZE_CODE_DIRECTLY = new AtomicBoolean(false);

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
            throw new IllegalArgumentException("Resource not found: " + resource);
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
            throw new UncheckedIOException(e);
        }
    }
}
