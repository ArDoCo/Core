/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractInformant;
import edu.kit.kastel.mcse.ardoco.core.api.agent.InconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.informants.MissingModelElementInconsistencyExtractor;

public class MissingModelElementInconsistencyAgent extends InconsistencyAgent {

    private final List<AbstractInformant> extractors;

    @Configurable
    private List<String> enabledExtractors;

    public MissingModelElementInconsistencyAgent(DataRepository dataRepository) {
        super("MissingModelElementInconsistencyAgent", dataRepository);

        extractors = List.of(new MissingModelElementInconsistencyExtractor(dataRepository));
        enabledExtractors = extractors.stream().map(e -> e.getClass().getSimpleName()).toList();
    }

    @Override
    public void run() {
        for (var extractor : findByClassName(enabledExtractors, extractors)) {
            this.addPipelineStep(extractor);
        }

        super.run();
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        extractors.forEach(e -> e.applyConfiguration(additionalConfiguration));
    }
}
