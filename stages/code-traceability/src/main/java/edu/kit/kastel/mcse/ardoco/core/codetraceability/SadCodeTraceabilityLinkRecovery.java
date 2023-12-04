/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.codetraceability;

import java.util.List;
import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.codetraceability.CodeTraceabilityState;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.agents.ArchitectureLinkToCodeLinkTransformerAgent;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;

public class SadCodeTraceabilityLinkRecovery extends AbstractExecutionStage {

    public SadCodeTraceabilityLinkRecovery(DataRepository dataRepository) {
        super(List.of(new ArchitectureLinkToCodeLinkTransformerAgent(dataRepository)), SadCodeTraceabilityLinkRecovery.class.getSimpleName(), dataRepository);
    }

    public static SadCodeTraceabilityLinkRecovery get(SortedMap<String, String> additionalConfigs, DataRepository dataRepository) {
        var sadSamCodeTraceabilityLinkRecovery = new SadCodeTraceabilityLinkRecovery(dataRepository);
        sadSamCodeTraceabilityLinkRecovery.applyConfiguration(additionalConfigs);
        return sadSamCodeTraceabilityLinkRecovery;
    }

    @Override
    protected void initializeState() {
        DataRepository dataRepository = getDataRepository();
        if (!DataRepositoryHelper.hasCodeTraceabilityState(dataRepository)) {
            var codeTraceabilityState = new CodeTraceabilityStateImpl(dataRepository);
            dataRepository.addData(CodeTraceabilityState.ID, codeTraceabilityState);
        }
    }
}
