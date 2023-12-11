/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tests.eval;

import java.io.Serializable;

import edu.kit.kastel.mcse.ardoco.core.common.util.SerializableFileBasedCache;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;

/**
 * Persistent cache for {@link StageTest} test data. For any given stage with the name X, the test data is cached in ArDoCo's user directory folder in the
 * {@code /test/X} directory.
 *
 * @param <U> the type of serializable object that can be contained in this cache
 */
public class TestDataCache<U extends Serializable> extends SerializableFileBasedCache<U> {
    protected final Class<? extends AbstractExecutionStage> stage;

    /**
     * Creates a new test data cache with the specified identifier in the specified subfolder of the test data cache directory.
     *
     * @param stage      the stage that this test data is associated with
     * @param cls        the class object of objects that are contained by this cache
     * @param identifier the identifier of the cache
     * @param subFolder  the sub-folder in the /test/X directory, must end with '/'
     */
    public TestDataCache(Class<? extends AbstractExecutionStage> stage, Class<? extends U> cls, String identifier, String subFolder) {
        super(cls, identifier, "test/" + stage.getSimpleName() + "/" + subFolder);
        this.stage = stage;
    }
}
