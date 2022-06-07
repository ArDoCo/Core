/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.diagramdetection;

import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.DiagramDetectionAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.IAgent;
import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.api.stage.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.diagramdetection.agents.SketchRecognitionAgent;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import java.util.List;
import java.util.Map;

public class DiagramDetection extends AbstractExecutionStage {

    private final MutableList<DiagramDetectionAgent> agents = Lists.mutable.of(new SketchRecognitionAgent());

    @Configurable
    private List<String> enabledAgents = agents.collect(IAgent::getId);

    @Override
    public void execute(DataStructure data, Map<String, String> additionalSettings) {
        data.setDiagramDetectionState(new DiagramDetectionState(additionalSettings));
        this.applyConfiguration(additionalSettings);

        for (DiagramDetectionAgent agent : findByClassName(enabledAgents, agents)) {
            agent.applyConfiguration(additionalSettings);
            agent.execute(data);
        }
    }
}
