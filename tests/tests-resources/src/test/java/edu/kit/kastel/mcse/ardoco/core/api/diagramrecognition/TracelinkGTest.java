package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.TraceType;

public class TracelinkGTest {
    public static final TraceLinkG DUMMY_TRACE_LINK_G = new TraceLinkG("SomeName", new int[] { 0, 42, 404 },
            new TypedTraceLinkG[] { new TypedTraceLinkG(new int[] { 3, 7, 11 }, TraceType.ENTITY_COREFERENCE) });
}
