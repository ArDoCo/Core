/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.computation;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.EndpointTuple;

public class SamCodeEndpointTuple extends EndpointTuple {

    public SamCodeEndpointTuple(ArchitectureItem architectureItem, CodeCompilationUnit codeItem) {
        super(architectureItem, codeItem);
    }

    public ArchitectureItem getArchitectureEndpoint() {
        return (ArchitectureItem) this.firstEndpoint();
    }

    public CodeCompilationUnit getCodeEndpoint() {
        return (CodeCompilationUnit) this.secondEndpoint();
    }
}
