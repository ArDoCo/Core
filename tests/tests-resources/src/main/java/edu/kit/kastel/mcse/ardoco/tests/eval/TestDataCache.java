package edu.kit.kastel.mcse.ardoco.tests.eval;

import java.io.Serializable;

import org.jetbrains.annotations.NotNull;
import com.fasterxml.jackson.core.type.TypeReference;

import edu.kit.kastel.mcse.ardoco.core.common.util.SerializableFileBasedCache;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;

public class TestDataCache<U extends Serializable> extends SerializableFileBasedCache<U> {
    protected final Class<? extends AbstractExecutionStage> stage;

    public TestDataCache(@NotNull Class<? extends AbstractExecutionStage> stage, Class<? extends U> cls, @NotNull String identifier, @NotNull String subFolder) {
        super(cls, identifier, "test/" + stage.getSimpleName() + "/" + subFolder);
        this.stage = stage;
    }
}
