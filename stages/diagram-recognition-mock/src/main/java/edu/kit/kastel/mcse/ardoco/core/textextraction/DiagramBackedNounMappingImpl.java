package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.Optional;

import org.eclipse.collections.impl.factory.Maps;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.NounMapping;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DiagramBackedNounMappingImpl extends NounMappingImpl {
    private final DiagramElement diagramElement;

    public DiagramBackedNounMappingImpl(@NotNull NounMappingImpl nounMapping, @Nullable DiagramElement diagramElement) {
        super(NounMappingImpl.earliestCreationTime(nounMapping), nounMapping.getWords(), Maps.mutable.ofMapIterable(nounMapping.getDistribution()), nounMapping.getReferenceWords(),
                nounMapping.getSurfaceForms(), nounMapping.getReference());
        this.diagramElement = diagramElement;
    }

    public Optional<DiagramElement> getDiagramElement() {
        return Optional.ofNullable(diagramElement);
    }
}
