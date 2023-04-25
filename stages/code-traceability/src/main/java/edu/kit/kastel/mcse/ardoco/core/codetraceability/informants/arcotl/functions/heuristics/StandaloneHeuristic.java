package edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.functions.heuristics;

import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.computation.NodeResult;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.computation.computationtree.StandaloneHeuristicNode;

/**
 * A heuristic that does not depend on any existing computation node's result.
 */
public abstract class StandaloneHeuristic extends Heuristic {

    private CodeModel codeModel;

    public StandaloneHeuristicNode getNode() {
        return new StandaloneHeuristicNode(this);
    }

    protected CodeModel getCodeModel() {
        return codeModel;
    }

    public NodeResult calculateConfidences(ArchitectureModel archModel, CodeModel codeModel) {
        this.codeModel = codeModel;
        return getNodeResult(archModel, codeModel);
    }
}
