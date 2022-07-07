/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelConnector;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;

/**
 * This class represents a special {@link ModelConnector} that can be manipulated to hold back a single element when
 * accessing the instances. By setting the index of the element that should be hold back, this element is then removed
 * from results of the typical {@link ModelConnector} methods. By setting the index to a negative number, all elements
 * will be returned. This {@link ModelConnector} does not implement own logic for getting elements etc., but uses an
 * existing {@link ModelConnector} like {@link edu.kit.kastel.mcse.ardoco.core.model.PcmXMLModelConnector} instead. You
 * can set this connector via the constructor.
 *
 */
public class HoldElementsBackModelConnector implements ModelConnector {

    private final ModelConnector actualModelConnector;
    private int currentHoldBackIndex = -1;

    /**
     * Constructor that uses the provided {@link ModelConnector} as underlying connector.
     * 
     * @param actualModelConnector the connector that is used for actually retrieving elements
     */
    public HoldElementsBackModelConnector(ModelConnector actualModelConnector) {
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
        if (currentHoldBackIndex < 0) {
            return actualInstances;
        }
        return actualInstances.newWithout(actualInstances.get(currentHoldBackIndex));
    }

    /**
     * Set the index of the element that should be hold back. Set the index to <0 if nothing should be held back.
     * 
     * @param currentHoldBackIndex the index of the element to be hold back. If negative, nothing is held back
     */
    public void setCurrentHoldBackIndex(int currentHoldBackIndex) {
        this.currentHoldBackIndex = currentHoldBackIndex;
    }

    /**
     * @return the ModelInstance that is held back. If nothing is held back, returns null
     */
    public ModelInstance getCurrentHoldBack() {
        if (currentHoldBackIndex < 0) {
            return null;
        }
        return actualModelConnector.getInstances().get(currentHoldBackIndex);
    }

    /**
     * @return the number of actual instances (including all held back elements)
     */
    public int numberOfActualInstances() {
        return actualModelConnector.getInstances().size();
    }
}
