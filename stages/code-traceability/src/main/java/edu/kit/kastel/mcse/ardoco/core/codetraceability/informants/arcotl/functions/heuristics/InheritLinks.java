/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.functions.heuristics;

import java.util.SortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.Datatype;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.EndpointTuple;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.computation.Confidence;

public class InheritLinks extends DependentHeuristic {

    @Override
    protected Confidence calculateConfidence(ArchitectureComponent archComponent, CodeCompilationUnit compUnit) {
        return inheritLinks(archComponent, compUnit);
    }

    @Override
    protected Confidence calculateConfidence(ArchitectureInterface archInterface, CodeCompilationUnit compUnit) {
        if (!archInterface.getSignatures().isEmpty()) {
            return new Confidence();
        }
        return inheritLinks(archInterface, compUnit);
    }

    private Confidence inheritLinks(ArchitectureItem archEndpoint, CodeCompilationUnit compUnit) {
        if (!getNodeResult().getLinkedEndpoints(compUnit).isEmpty()) {
            return new Confidence();
        }
        Confidence maxConfidence = new Confidence();
        for (Datatype codeType : compUnit.getAllDataTypes()) {
            Confidence extendedConfidence = inheritLinks(archEndpoint, codeType);
            if (extendedConfidence.compareTo(maxConfidence) > 0) {
                maxConfidence = extendedConfidence;
            }
        }
        return maxConfidence;
    }

    private Confidence inheritLinks(ArchitectureItem archEndpoint, Datatype codeType) {
        SortedSet<Datatype> extendedTypes = MethodResemblance.getAllExtendedTypes(codeType);
        MethodResemblance.getAllImplementedInterfaces(codeType).forEach(i -> extendedTypes.addAll(MethodResemblance.getAllExtendedTypes(i)));

        Confidence maxConfidence = new Confidence();
        for (Datatype extendedType : extendedTypes) {
            if (areInDifferentPackages(codeType.getCompilationUnit(), extendedType.getCompilationUnit())) {
                continue;
            }
            Confidence extendedConfidence = getNodeResult().getConfidence(new EndpointTuple(archEndpoint, extendedType.getCompilationUnit()));
            if (extendedConfidence.compareTo(maxConfidence) > 0) {
                maxConfidence = extendedConfidence;
            }
        }
        return maxConfidence;
    }

    public static boolean areInDifferentPackages(CodeCompilationUnit fileA, CodeCompilationUnit fileB) {
        if (fileA.hasParent() != fileB.hasParent()) {
            return true;
        }
        return fileA.hasParent() && fileB.hasParent() && !fileA.getParent().equals(fileB.getParent());
    }

    @Override
    public String toString() {
        return "InheritExtendedLinks";
    }
}
