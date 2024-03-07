/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.id.tests.eval.helper;

import java.io.File;
import java.io.Serializable;

import com.fasterxml.jackson.core.type.TypeReference;

import edu.kit.kastel.mcse.ardoco.core.common.util.SerializableFileBasedCache;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;

/**
 * Persistent cache for {@link StageTest} test data. For any given stage with the name X, the test data is cached in ArDoCo's user directory folder in the
 * {@code /test/X} directory.
 *
 * @param <T> the type of serializable object that can be contained in this cache
 */
public class TestDataCache<T extends Serializable> extends SerializableFileBasedCache<T> {
    protected final Class<? extends AbstractExecutionStage> stage;

    /**
     * Creates a new test data cache with the specified identifier in the specified subfolder of the test data cache directory.
     *
     * @param stage      the stage that this test data is associated with
     * @param cls        the class object of objects that are contained by this cache
     * @param identifier the identifier of the cache
     * @param subFolder  the sub-folder in the /test/X directory, must end with '/'
     */
    public TestDataCache(Class<? extends AbstractExecutionStage> stage, Class<? extends T> cls, String identifier, String subFolder) {
        super(cls, identifier, "test" + File.separator + stage.getSimpleName() + File.separator + subFolder);
        this.stage = stage;
    }

    public TestDataCache(Class<? extends AbstractExecutionStage> stage, TypeReference<? extends T> typeReference, String identifier, String subFolder) {
        super(typeReference, identifier, "test" + File.separator + stage.getSimpleName() + File.separator + subFolder);
        this.stage = stage;
    }
}
