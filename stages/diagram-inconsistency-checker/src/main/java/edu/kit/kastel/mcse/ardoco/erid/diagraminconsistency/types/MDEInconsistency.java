/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.types;

import java.util.Locale;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;

/**
 * {@link MDEInconsistency} stands for Missing Diagram Element Inconsistency and should be created for each {@link RecommendedInstance} that is not part of a
 * {@link edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram Diagram}.
 * 
 * @param recommendedInstance the {@link RecommendedInstance}
 */
public record MDEInconsistency(RecommendedInstance recommendedInstance) implements Inconsistency, Comparable<MDEInconsistency> {
    private static final String type = "MissingDiagramElement";

    @Override
    public String getReason() {
        return String.format(Locale.ENGLISH, "Text indicates an Entity \"%s\" (confidence: %.2f) that is not contained by a diagram.", recommendedInstance
                .getName(), recommendedInstance.getProbability());
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public ImmutableCollection<String[]> toFileOutput() {
        String[] entry = { getType(), recommendedInstance.getName(), Double.toString(recommendedInstance.getProbability()) };
        var list = Lists.mutable.<String[]>empty();
        list.add(entry);
        return list.toImmutable();
    }

    @Override
    public int compareTo(MDEInconsistency o) {
        return recommendedInstance().getName().compareTo(o.recommendedInstance().getName());
    }
}
