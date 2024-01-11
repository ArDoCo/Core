/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;

/**
 * Represents a {@link edu.kit.kastel.mcse.ardoco.core.api.textextraction.NounMapping NounMapping} where mappings are associated with a {@link DiagramElement}.
 */
public class DiagramBackedNounMappingImpl extends NounMappingImpl {
    private final DiagramElement diagramElement;

    /**
     * Creates a noun mapping based on a {@link NounMappingImpl} and an associated {@link DiagramElement}
     *
     * @param nounMapping    the mapping
     * @param diagramElement the diagram element, nullable if none is associated
     */
    public DiagramBackedNounMappingImpl(NounMappingImpl nounMapping, @Nullable DiagramElement diagramElement) {
        super(CREATION_TIME_COUNTER.incrementAndGet(), nounMapping.getWords(), nounMapping.getDistribution().toSortedMap().toImmutable(), nounMapping
                .getReferenceWords(), nounMapping.getSurfaceForms(), nounMapping.getReference());
        this.diagramElement = diagramElement;
    }

    /**
     * {@return the associated {@link DiagramElement}, {@link Optional#EMPTY} if none is associated}
     */
    public Optional<DiagramElement> getDiagramElement() {
        return Optional.ofNullable(diagramElement);
    }
}
