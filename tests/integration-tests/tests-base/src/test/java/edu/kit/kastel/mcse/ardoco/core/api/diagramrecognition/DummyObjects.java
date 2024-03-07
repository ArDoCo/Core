/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.diagrams.TraceType;

public class DummyObjects {
    public static final BoundingBox DUMMY_BOUNDING_BOX = new BoundingBox(0, 0, 100, 100);
    public static final TextBox DUMMY_TEXT_BOX = new TextBox(DUMMY_BOUNDING_BOX, 1.0, "Lorem Ipsum");
    public static final TraceLinkGS DUMMY_TRACE_LINK_GS = new TraceLinkGS("SomeName", new int[] { 0, 42, 404 }, new TypedTraceLinkGS[] { new TypedTraceLinkGS(
            new int[] { 3, 7, 11 }, TraceType.ENTITY_COREFERENCE) });
}
