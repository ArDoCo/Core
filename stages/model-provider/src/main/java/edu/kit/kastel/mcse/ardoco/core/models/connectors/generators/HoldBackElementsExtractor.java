package edu.kit.kastel.mcse.ardoco.core.models.connectors.generators;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.HoldBackModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;

public class HoldBackElementsExtractor extends Extractor {

    private final Extractor actualExtractor;
    private int currentHoldBackContentIndex = -1;
    private int currentHoldBackEndpointIndex = -1;

    public HoldBackElementsExtractor(String path, Extractor actualExtractor) {
        super(path);
        this.actualExtractor = actualExtractor;
    }

    @Override
    public Model extractModel() {
        List<? extends Entity> content = actualExtractor.extractModel().getContent();
        content.remove(currentHoldBackContentIndex);
        List<? extends Entity> endpoints = actualExtractor.extractModel().getEndpoints();
        endpoints.remove(currentHoldBackEndpointIndex);
        return new HoldBackModel(content, endpoints);
    }

    @Override
    public ModelType getModelType() {
        return actualExtractor.getModelType();
    }

    public void setCurrentHoldBackContentIndex(int currentHoldBackContentIndex) {
        this.currentHoldBackContentIndex = currentHoldBackContentIndex;
    }

    public void setCurrentHoldBackEndpointIndex(int currentHoldBackEndpointIndex) {
        this.currentHoldBackEndpointIndex = currentHoldBackEndpointIndex;
    }

    public Entity getCurrentHoldBackEndpoint() {
        if (currentHoldBackEndpointIndex < 0) {
            return null;
        }
        return actualExtractor.extractModel().getEndpoints().get(currentHoldBackEndpointIndex);
    }

    public Entity getCurrentHoldBackContent() {
        if (currentHoldBackContentIndex < 0) {
            return null;
        }
        return actualExtractor.extractModel().getContent().get(currentHoldBackContentIndex);
    }

    public int numberOfActualContents() {
        return actualExtractor.extractModel().getContent().size();
    }

    public int numberOfActualEndpoints() {
        return actualExtractor.extractModel().getEndpoints().size();
    }
}
