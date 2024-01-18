package edu.kit.kastel.mcse.ardoco.core.textextraction

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.NounMapping

/**
 * Represents a [NounMapping] where mappings are associated with a [DiagramElement].
 */
class DiagramBackedNounMappingImpl(nounMapping: NounMappingImpl, private val diagramElement: DiagramElement?) : NounMappingImpl(
    CREATION_TIME_COUNTER.incrementAndGet(),
    nounMapping.words,
    nounMapping.distribution.toSortedMap().toImmutable(),
    nounMapping.referenceWords,
    nounMapping.surfaceForms,
    nounMapping.reference
) {
    /**
     * {@return the associated {@link DiagramElement}, {@link Optional#EMPTY} if none is associated}
     */
    fun getDiagramElement(): DiagramElement? = diagramElement
}
