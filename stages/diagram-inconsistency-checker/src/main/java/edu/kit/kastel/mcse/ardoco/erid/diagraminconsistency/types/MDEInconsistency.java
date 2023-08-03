package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.types;

import java.util.Locale;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Lists;
import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;

public record MDEInconsistency(RecommendedInstance recommendedInstance) implements Inconsistency, Comparable<MDEInconsistency> {
    private final static String type = "MissingDiagramElement";

    @Override
    public String getReason() {
        return String.format(Locale.US, "Text indicates an Entity \"%s\" (confidence: %.2f) that is not contained by a diagram.", recommendedInstance.getName(),
                recommendedInstance.getProbability());
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
    public int compareTo(@NotNull MDEInconsistency o) {
        return recommendedInstance().getName().compareTo(o.recommendedInstance().getName());
    }
}
