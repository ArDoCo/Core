/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.types;

import java.util.Locale;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.inconsistency.Inconsistency;

/**
 * {@link MTDEInconsistency} stands for Missing Text for Diagram Element Inconsistency and should be created for each {@link DiagramElement} that is not part of
 * the {@link edu.kit.kastel.mcse.ardoco.core.api.text.Text Text}.
 *
 * @param diagramElement the {@link DiagramElement}
 */
public record MTDEInconsistency(DiagramElement diagramElement) implements Inconsistency, Comparable<MTDEInconsistency> {
    private static final String type = "MissingTextForDiagramElement";

    @Override
    public String getReason() {
        return String.format(Locale.US, "Diagram \"%s\" contains a Diagram Element \"%s\" that seems to be undocumented.", diagramElement.getDiagram()
                .getResourceName(), diagramElement.getName());
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public ImmutableCollection<String[]> toFileOutput() {
        String[] entry = { getType(), diagramElement.getName() };
        var list = Lists.mutable.<String[]>empty();
        list.add(entry);
        return list.toImmutable();
    }

    @Override
    public int compareTo(MTDEInconsistency o) {
        return diagramElement().compareTo(o.diagramElement());
    }
}
