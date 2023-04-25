package edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.functions.heuristics;

import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.Datatype;
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
        if (getNodeResult().getLinkedEndpoints(compUnit).size() > 0) {
            return new Confidence();
        }
        Confidence maxConfidence = new Confidence();
        for (Datatype codeType : compUnit.getAllDatatypes()) {
            Confidence extendedConfidence = inheritLinks(archEndpoint, codeType);
            if (extendedConfidence.compareTo(maxConfidence) > 0) {
                maxConfidence = extendedConfidence;
            }
        }
        return maxConfidence;
    }

    private Confidence inheritLinks(ArchitectureItem archEndpoint, Datatype codeType) {
        Set<Datatype> extendedTypes = MethodResemblance.getAllExtendedTypes(codeType);
        MethodResemblance.getAllImplementedInterfaces(codeType).forEach(i -> extendedTypes.addAll(MethodResemblance.getAllExtendedTypes(i)));

        Confidence maxConfidence = new Confidence();
        for (Datatype extendedType : extendedTypes) {
            if (!areInSamePackage(codeType.getCompilationUnit(), extendedType.getCompilationUnit())) {
                continue;
            }
            Confidence extendedConfidence = getNodeResult().getConfidence(new EndpointTuple(archEndpoint, extendedType.getCompilationUnit()));
            if (extendedConfidence.compareTo(maxConfidence) > 0) {
                maxConfidence = extendedConfidence;
            }
        }
        return maxConfidence;
    }

    public static boolean areInSamePackage(CodeCompilationUnit fileA, CodeCompilationUnit fileB) {
        if (fileA.hasParent() != fileB.hasParent()) {
            return false;
        }
        if (fileA.hasParent() && fileB.hasParent()) {
            if (!fileA.getParent().equals(fileB.getParent())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "InheritExtendedLinks";
    }
}
