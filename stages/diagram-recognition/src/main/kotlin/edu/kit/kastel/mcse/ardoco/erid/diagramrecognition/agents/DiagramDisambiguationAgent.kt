package edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.agents

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.informants.DiagramDisambiguationInformant

/**
 * Responsible for disambiguating abbreviations in [DiagramElements][edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement].
 *
 * @see DiagramDisambiguationInformant
 */
class DiagramDisambiguationAgent(dataRepository: DataRepository?) : PipelineAgent(
    listOf(DiagramDisambiguationInformant(dataRepository)),
    DiagramDisambiguationAgent::class.java.getSimpleName(),
    dataRepository
)
