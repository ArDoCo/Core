/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramdetection.agents;

import java.io.File;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.diagramdetection.informants.SketchRecognitionInformant;

public class SketchRecognitionAgent extends PipelineAgent {
    private final List<Informant> informants;

    @Configurable
    private List<String> enabledInformants;

    public SketchRecognitionAgent(DataRepository dataRepository, File diagramDirectory) {
        super(SketchRecognitionAgent.class.getSimpleName(), dataRepository);
        informants = List.of(new SketchRecognitionInformant(dataRepository, diagramDirectory));
        enabledInformants = informants.stream().map(e -> e.getClass().getSimpleName()).toList();
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledInformants, informants);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        informants.forEach(e -> e.applyConfiguration(additionalConfiguration));
    }
}
