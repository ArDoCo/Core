/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramdetection;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Agent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.api.data.diagram.DiagramDetectionState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.diagramdetection.agents.SketchRecognitionAgent;

public class DiagramDetection extends AbstractExecutionStage {

    private final MutableList<PipelineAgent> agents;

    @Configurable
    private List<String> enabledAgents;

    public DiagramDetection(DataRepository dataRepository, File diagramDirectory) {
        super(DiagramDetection.class.getSimpleName(), dataRepository);

        agents = Lists.mutable.of(new SketchRecognitionAgent(dataRepository, diagramDirectory));
        enabledAgents = agents.collect(Agent::getId);
    }

    @Override
    protected void initializeState() {
        getDataRepository().addData(DiagramDetectionState.ID, new DiagramDetectionStateImpl());
    }

    @Override
    protected List<PipelineAgent> getEnabledAgents() {
        return findByClassName(enabledAgents, agents);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        super.delegateApplyConfigurationToInternalObjects(additionalConfiguration);
        for (var agent : agents) {
            agent.applyConfiguration(additionalConfiguration);
        }
    }
}
