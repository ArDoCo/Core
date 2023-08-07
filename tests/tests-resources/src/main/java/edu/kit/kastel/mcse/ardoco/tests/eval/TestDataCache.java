package edu.kit.kastel.mcse.ardoco.tests.eval;

import java.io.Serializable;

import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.common.util.SerializableFileBasedCache;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;

public class TestDataCache<T extends Serializable> extends SerializableFileBasedCache<T> {
    protected final Class<? extends AbstractExecutionStage> stage;

    public TestDataCache(@NotNull Class<? extends AbstractExecutionStage> stage, Class<? extends T> contentClass, @NotNull String identifier,
            @NotNull String subFolder) {
        super(contentClass, identifier, "test/" + stage.getSimpleName() + "/" + subFolder);
        this.stage = stage;
    }
}
