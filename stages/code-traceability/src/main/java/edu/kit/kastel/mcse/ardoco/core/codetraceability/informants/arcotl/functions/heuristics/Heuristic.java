package edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.functions.heuristics;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.computation.Confidence;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.computation.EndpointTupleRepo;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.computation.NodeResult;

/**
 * A heuristic.
 */
public abstract class Heuristic {

    protected final NodeResult getNodeResult(ArchitectureModel archModel, CodeModel codeModel) {
        NodeResult confidences = new NodeResult();
        EndpointTupleRepo endpointTupleRepo = new EndpointTupleRepo(archModel, codeModel);
        for (var endpointTuple : endpointTupleRepo.getEndpointTuples()) {
            ArchitectureItem archEndpoint = endpointTuple.getArchitectureEndpoint();
            CodeCompilationUnit compUnit = endpointTuple.getCodeEndpoint();
            Confidence confidence = new Confidence();
            if (archEndpoint instanceof ArchitectureInterface archInterface) {
                confidence = calculateConfidence(archInterface, compUnit);
            }
            if (archEndpoint instanceof ArchitectureComponent archComponent) {
                confidence = calculateConfidence(archComponent, compUnit);
            }
            confidences.add(endpointTuple, confidence);
        }
        return confidences;
    }

    protected Confidence calculateConfidence(ArchitectureComponent archComponent, CodeCompilationUnit compUnit) {
        return new Confidence();
    }

    protected Confidence calculateConfidence(ArchitectureInterface archInterface, CodeCompilationUnit compUnit) {
        return new Confidence();
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return true;
    }
}
