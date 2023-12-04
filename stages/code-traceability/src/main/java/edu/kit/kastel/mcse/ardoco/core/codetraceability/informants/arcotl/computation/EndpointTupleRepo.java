/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.computation;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;

/**
 * A repository of endpoint tuples. An endpoint tuple consists of an
 * architecture endpoint and a code endpoint. Contains every possible
 * combination of endpoints of an architecture model and a code model.
 */
public class EndpointTupleRepo {

    private List<SamCodeEndpointTuple> endpointTuples;

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
        endpointTuples = new ArrayList<>();
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
    public List<SamCodeEndpointTuple> getEndpointTuples() {
        return new ArrayList<>(endpointTuples);
    }
}
