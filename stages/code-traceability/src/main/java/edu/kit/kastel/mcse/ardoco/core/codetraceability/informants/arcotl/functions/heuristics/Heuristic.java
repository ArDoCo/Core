/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.functions.heuristics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.computation.Confidence;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.computation.EndpointTupleRepo;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.computation.NodeResult;

/**
 * A heuristic.
 */
public abstract class Heuristic {
    protected static final Logger logger = LoggerFactory.getLogger(Heuristic.class);

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
        if (archComponent == null || compUnit == null) {
            logger.warn("null values when calculating confidence");
        }
        return new Confidence();
    }

    protected Confidence calculateConfidence(ArchitectureInterface archInterface, CodeCompilationUnit compUnit) {
        if (archInterface == null || compUnit == null) {
            logger.warn("null values when calculating confidence");
        }
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
        return (obj instanceof Heuristic);
    }
}
