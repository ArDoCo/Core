package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;

public class DiagramBackedNounMappingImpl extends NounMappingImpl {
    private final DiagramElement diagramElement;

    public DiagramBackedNounMappingImpl(@NotNull NounMappingImpl nounMapping, @Nullable DiagramElement diagramElement) {
        super(NounMappingImpl.earliestCreationTime(nounMapping), nounMapping.getWords(), nounMapping.getDistribution().toSortedMap().toImmutable(),
                nounMapping.getReferenceWords(), nounMapping.getSurfaceForms(), nounMapping.getReference());
        this.diagramElement = diagramElement;
    }

    public Optional<DiagramElement> getDiagramElement() {
        return Optional.ofNullable(diagramElement);
    }
}
