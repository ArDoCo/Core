package edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.agents;

import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.informants.DiagramDisambiguationInformant;
import java.util.List;

public class DiagramDisambiguationAgent extends PipelineAgent {
  @Configurable
  private final List<String> enabledInformants;

  public DiagramDisambiguationAgent(DataRepository dataRepository) {
    super(DiagramDisambiguationAgent.class.getSimpleName(), dataRepository,
            List.of(new DiagramDisambiguationInformant(dataRepository)));
    enabledInformants = getInformantClassNames();
  }

  @Override
  protected List<Informant> getEnabledPipelineSteps() {
    return findByClassName(enabledInformants, getInformants());
  }
}
