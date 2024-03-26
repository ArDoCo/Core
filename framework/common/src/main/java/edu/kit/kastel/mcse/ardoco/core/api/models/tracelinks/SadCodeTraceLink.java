/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;

public class SadCodeTraceLink extends TraceLink<SentenceEntity, CodeCompilationUnit> {

    public SadCodeTraceLink(SentenceEntity e1, CodeCompilationUnit e2) {
        super(e1, e2);
    }
}
