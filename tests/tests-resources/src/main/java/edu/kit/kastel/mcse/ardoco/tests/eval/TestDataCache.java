package edu.kit.kastel.mcse.ardoco.tests.eval;

import java.io.Serializable;

import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.common.util.SerializableFileBasedCache;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;

public class TestDataCache<U extends Serializable> extends SerializableFileBasedCache<TestData> {
    protected final Class<? extends AbstractExecutionStage> stage;

    public TestDataCache(@NotNull Class<? extends AbstractExecutionStage> stage, @NotNull String identifier, @NotNull String subFolder) {
        super(TestData.class, identifier, "test/" + stage.getSimpleName() + "/" + subFolder);
        this.stage = stage;
    }

    @Override
    public TestData<U> load() {
        @SuppressWarnings("unchecked") TestData<U> typed = super.load();
        return typed;
    }
}
