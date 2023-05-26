/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.computation;

import java.util.HashSet;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeModel;

/**
 * A repository of endpoint tuples. An endpoint tuple consists of an
 * architecture endpoint and a code endpoint. Contains every possible
 * combination of endpoints of an architecture model and a code model.
 */
public class EndpointTupleRepo {

    private Set<SamCodeEndpointTuple> endpointTuples;

    /**
     * Creates a new repository of endpoint tuples. Contains every possible
     * combination of endpoints of the specified architecture model and the
     * specified code model.
     *
     * @param archModel the architecture model whose endpoints are to be part of the
     *                  repository
     * @param codeModel the code model whose endpoints are to be part of the
     *                  repository
     */
    public EndpointTupleRepo(ArchitectureModel archModel, CodeModel codeModel) {
        endpointTuples = new HashSet<>();
        for (var architectureEndpoint : archModel.getEndpoints()) {
            for (var codeEndpoint : codeModel.getEndpoints()) {
                endpointTuples.add(new SamCodeEndpointTuple(architectureEndpoint, codeEndpoint));
            }
        }
    }

    /**
     * Returns all endpoint tuples.
     *
     * @return all endpoint tuples
     */
    public Set<SamCodeEndpointTuple> getEndpointTuples() {
        return endpointTuples;
    }
}
