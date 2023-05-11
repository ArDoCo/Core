/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.codetraceability.informants;

import java.util.Map;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.codetraceability.CodeTraceabilityState;
import edu.kit.kastel.mcse.ardoco.core.api.connectiongenerator.ConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadSamTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SamCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.TransitiveTraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

public class TraceLinkCombiner extends Informant {
    private static final Logger logger = LoggerFactory.getLogger(TraceLinkCombiner.class);

    public TraceLinkCombiner(DataRepository dataRepository) {
        super(TraceLinkCombiner.class.getSimpleName(), dataRepository);
    }

    @Override
    public void run() {
        MutableSet<TransitiveTraceLink> transitiveTraceLinks = Sets.mutable.empty();
        CodeTraceabilityState codeTraceabilityState = DataRepositoryHelper.getCodeTraceabilityState(getDataRepository());
        ModelStates modelStatesData = DataRepositoryHelper.getModelStatesData(getDataRepository());
        ConnectionStates connectionStates = DataRepositoryHelper.getConnectionStates(getDataRepository());

        var samCodeTraceLinks = codeTraceabilityState.getSamCodeTraceLinks();
        for (var modelId : modelStatesData.extractionModelIds()) {
            var metamodel = modelStatesData.getModelExtractionState(modelId).getMetamodel();
            var connectionState = connectionStates.getConnectionState(metamodel);
            var sadSamTraceLinks = connectionState.getTraceLinks();

            var combinedLinks = combineToTransitiveTraceLinks(sadSamTraceLinks, samCodeTraceLinks);
            transitiveTraceLinks.addAll(combinedLinks.toList());
        }

        System.out.println(transitiveTraceLinks.size());
        codeTraceabilityState.addTransitiveTraceLinks(transitiveTraceLinks); // TODO
    }

    private ImmutableSet<TransitiveTraceLink> combineToTransitiveTraceLinks(ImmutableSet<SadSamTraceLink> sadSamTraceLinks,
            ImmutableSet<SamCodeTraceLink> samCodeTraceLinks) {
        MutableSet<TransitiveTraceLink> transitiveTraceLinks = Sets.mutable.empty();
        for (var sadSamTraceLink : sadSamTraceLinks) {
            String modelElementUid = sadSamTraceLink.getModelElementUid();
            for (var samCodeTraceLink : samCodeTraceLinks) {
                String samCodeTraceLinkModelElementId = samCodeTraceLink.getEndpointTuple().firstEndpoint().getId();
                if (modelElementUid.equals(samCodeTraceLinkModelElementId)) {
                    var transitiveTraceLinkOptional = TransitiveTraceLink.createTransitiveTraceLink(sadSamTraceLink, samCodeTraceLink);
                    if (transitiveTraceLinkOptional.isPresent()) {
                        transitiveTraceLinks.add(transitiveTraceLinkOptional.get());
                    }
                }
            }
        }
        return transitiveTraceLinks.toImmutable();
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // empty
    }
}
