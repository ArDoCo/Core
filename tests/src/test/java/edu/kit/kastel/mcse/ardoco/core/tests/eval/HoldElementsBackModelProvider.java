package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelConnector;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;

public class HoldElementsBackModelProvider implements ModelConnector {

    private final ModelConnector actualModelConnector;
    private int currentHoldBackId = 0;

    public HoldElementsBackModelProvider(ModelConnector actualModelConnector) {
        this.actualModelConnector = actualModelConnector;
    }

    @Override
    public String getModelId() {
        return actualModelConnector.getModelId();
    }

    @Override
    public Metamodel getMetamodel() {
        return actualModelConnector.getMetamodel();
    }

    @Override
    public ImmutableList<ModelInstance> getInstances() {
        var actualInstances = actualModelConnector.getInstances();
        return actualInstances.newWithout(actualInstances.get(currentHoldBackId));
    }

    public void setCurrentHoldBackId(int currentHoldBackId) {
        this.currentHoldBackId = currentHoldBackId;
    }

    public ModelInstance getCurrentHoldBack() {
        return actualModelConnector.getInstances().get(currentHoldBackId);
    }
}
