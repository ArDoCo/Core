package edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.functions.heuristics;

import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.computation.NodeResult;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.computation.computationtree.DependentHeuristicNode;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.computation.computationtree.Node;

/**
 * A heuristic that depends on exactly one computation node's result. To apply
 * this heuristic there need to already exist some calculated confidences that
 * can be used.
 */
public abstract class DependentHeuristic extends Heuristic {

    private NodeResult nodeResult;
    private ArchitectureModel archModel;

    public DependentHeuristicNode getNode(Node child) {
        return new DependentHeuristicNode(this, child);
    }

    protected NodeResult getNodeResult() {
        return nodeResult;
    }

    protected ArchitectureModel getArchModel() {
        return archModel;
    }

    public final NodeResult calculateConfidences(ArchitectureModel archModel, CodeModel codeModel, NodeResult nodeResult) {
        this.nodeResult = nodeResult;
        this.archModel = archModel;
        return getNodeResult(archModel, codeModel);
    }
}
