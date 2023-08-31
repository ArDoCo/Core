/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.functions.heuristics;

import java.util.List;
import java.util.SortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodePackage;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.EndpointTuple;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.NameComparisonUtils;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.computation.Confidence;

public class SubpackageFilter extends DependentHeuristic {

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
        List<CodePackage> thisPackages = NameComparisonUtils.getMatchedPackages(archEndpoint, compUnit);
        SortedSet<Entity> linkedArchitectureEndpoints = getNodeResult().getLinkedEndpoints(compUnit);
        linkedArchitectureEndpoints.remove(archEndpoint);
        for (var linkedArchitectureEndpoint : linkedArchitectureEndpoints) {
            List<CodePackage> otherPackages = NameComparisonUtils.getMatchedPackages(linkedArchitectureEndpoint, compUnit);
            if (thisPackages.isEmpty() || otherPackages.isEmpty()) {
                return new Confidence();
            }
            List<CodePackage> parentPackages = NameComparisonUtils.getPackageList(thisPackages.get(0));
            parentPackages.remove(thisPackages.get(0));
            if (parentPackages.contains(otherPackages.get(otherPackages.size() - 1))) {
                return new Confidence(1.0);
            }
        }
        return new Confidence();
    }

    @Override
    public String toString() {
        return "SubpackageFilter";
    }
}
