package edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.agents

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.informants.DiagramModelReferenceInformant

/**
 * Responsible for setting the references of [DiagramElements][edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement].
 *
 * @see DiagramModelReferenceInformant
 */
class DiagramReferenceAgent(dataRepository: DataRepository?) : PipelineAgent(
    listOf(DiagramModelReferenceInformant(dataRepository)),
    DiagramReferenceAgent::class.java.getSimpleName(),
    dataRepository
)
