/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.codetraceability.informants;

import java.util.SortedMap;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.codetraceability.CodeTraceabilityState;
import edu.kit.kastel.mcse.ardoco.core.api.connectiongenerator.ConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadSamTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SamCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.TransitiveTraceLink;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

@Deterministic
public class TraceLinkCombiner extends Informant {

    public TraceLinkCombiner(DataRepository dataRepository) {
        super(TraceLinkCombiner.class.getSimpleName(), dataRepository);
    }

    @Override
    public void run() {
        MutableSet<SadCodeTraceLink> transitiveTraceLinks = Sets.mutable.empty();
        CodeTraceabilityState codeTraceabilityState = DataRepositoryHelper.getCodeTraceabilityState(getDataRepository());
        ModelStates modelStatesData = DataRepositoryHelper.getModelStatesData(getDataRepository());
        ConnectionStates connectionStates = DataRepositoryHelper.getConnectionStates(getDataRepository());

        if (codeTraceabilityState == null || modelStatesData == null || connectionStates == null) {
            return;
        }
        var samCodeTraceLinks = codeTraceabilityState.getSamCodeTraceLinks();
        for (var modelId : modelStatesData.modelIds()) {
            var metamodel = modelStatesData.getModelExtractionState(modelId).getMetamodel();
            var connectionState = connectionStates.getConnectionState(metamodel);
            var sadSamTraceLinks = connectionState.getTraceLinks();

            var combinedLinks = combineToTransitiveTraceLinks(sadSamTraceLinks, samCodeTraceLinks);
            transitiveTraceLinks.addAll(combinedLinks.toList());
        }

        codeTraceabilityState.addSadCodeTraceLinks(transitiveTraceLinks);
    }

    private ImmutableSet<SadCodeTraceLink> combineToTransitiveTraceLinks(ImmutableSet<SadSamTraceLink> sadSamTraceLinks,
            ImmutableSet<SamCodeTraceLink> samCodeTraceLinks) {
        MutableSet<SadCodeTraceLink> transitiveTraceLinks = Sets.mutable.empty();
        for (var sadSamTraceLink : sadSamTraceLinks) {
            String modelElementUid = sadSamTraceLink.getModelElementUid();
            for (var samCodeTraceLink : samCodeTraceLinks) {
                String samCodeTraceLinkModelElementId = samCodeTraceLink.getEndpointTuple().firstEndpoint().getId();
                if (modelElementUid.equals(samCodeTraceLinkModelElementId)) {
                    var transitiveTraceLinkOptional = TransitiveTraceLink.createTransitiveTraceLink(sadSamTraceLink, samCodeTraceLink);
                    transitiveTraceLinkOptional.ifPresent(transitiveTraceLinks::add);
                }
            }
        }
        return transitiveTraceLinks.toImmutable();
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        // empty
    }
}
