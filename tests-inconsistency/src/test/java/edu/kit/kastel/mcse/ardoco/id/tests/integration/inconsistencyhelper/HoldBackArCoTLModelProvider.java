/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.id.tests.integration.inconsistencyhelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelElement;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArCoTLModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.Extractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.pcm.PcmExtractor;

public class HoldBackArCoTLModelProvider {

    private final File inputArchitectureModel;
    private int currentHoldBackIndex = -1;
    private final ArchitectureModel initialModel;
    private final ImmutableList<ArchitectureComponent> components;

    public HoldBackArCoTLModelProvider(File inputArchitectureModel) {
        this.inputArchitectureModel = inputArchitectureModel;
        var model = getExtractor().extractModel();
        assert model instanceof ArchitectureModel;
        initialModel = (ArchitectureModel) model;
        components = Lists.immutable.fromStream(initialModel.getContent()
                .stream()
                .filter(it -> it instanceof ArchitectureComponent)
                .map(it -> (ArchitectureComponent) it));
    }

    private Extractor getExtractor() {
        return new PcmExtractor(inputArchitectureModel.getAbsolutePath());
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
     * Returns the number of actual instances (including all held back elements)
     *
     * @return the number of actual instances (including all held back elements)
     */
    public int numberOfActualInstances() {
        return components.size();
    }

    /**
     * Returns the ModelInstance that is held back. If nothing is held back, returns null
     *
     * @return the ModelInstance that is held back. If nothing is held back, returns null
     */
    public ModelElement getCurrentHoldBack() {
        if (currentHoldBackIndex < 0) {
            return null;
        }
        return components.get(currentHoldBackIndex);
    }

    public ArCoTLModelProviderAgent get(SortedMap<String, String> additionalConfigs, DataRepository dataRepository) {
        ArCoTLModelProviderAgent agent = new ArCoTLModelProviderAgent(dataRepository, List.of(new Extractor("") {
            @Override
            public Model extractModel() {
                var elements = new ArrayList<>(initialModel.getContent());
                var elementToRemove = getCurrentHoldBack();
                elements.remove(elementToRemove);
                return new ArchitectureModel(elements);
            }

            @Override
            public ModelType getModelType() {
                return ArchitectureModelType.PCM;
            }
        }));
        agent.applyConfiguration(additionalConfigs);
        return agent;
    }

}
