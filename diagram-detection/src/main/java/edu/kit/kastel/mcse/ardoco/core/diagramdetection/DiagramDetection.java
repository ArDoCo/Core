/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.diagramdetection;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Agent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.api.data.diagram.DiagramDetectionState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.diagramdetection.agents.SketchRecognitionAgent;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import java.io.File;
import java.util.List;

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
    public void run() {
        getDataRepository().addData(DiagramDetectionState.ID, new DiagramDetectionStateImpl());

        for (var agent : findByClassName(enabledAgents, agents)) {
            this.addPipelineStep(agent);
        }
        super.run();
    }
}
