/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.functions.heuristics;

import java.util.SortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.EndpointTuple;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.computation.Confidence;

public class SubpackageFilter2 extends DependentHeuristic {

    @Override
    protected Confidence calculateConfidence(ArchitectureComponent archComponent, CodeCompilationUnit compUnit) {
        return calculateSubpackageFilter(archComponent, compUnit);
    }

    @Override
    protected Confidence calculateConfidence(ArchitectureInterface archInterface, CodeCompilationUnit compUnit) {
        if (!archInterface.getSignatures().isEmpty()) {
            return new Confidence();
        }
        return calculateSubpackageFilter(archInterface, compUnit);
    }

    private Confidence calculateSubpackageFilter(ArchitectureItem archEndpoint, CodeCompilationUnit compUnit) {
        EndpointTuple thisTuple = new EndpointTuple(archEndpoint, compUnit);
        if (!getNodeResult().getConfidence(thisTuple).hasValue()) {
            return new Confidence();
        }
        int i = 0;
        SortedSet<Entity> linkedArchEndpoints = getNodeResult().getLinkedEndpoints(compUnit);
        for (var linkedArchEndpoint : linkedArchEndpoints) {
            if (linkedArchEndpoint instanceof ArchitectureComponent) {
                i++;
            }
            if (i > 1) {
                return new Confidence(1.0);
            }
        }
        return new Confidence();
    }

    @Override
    public String toString() {
        return "SubpackageFilter2";
    }
}
