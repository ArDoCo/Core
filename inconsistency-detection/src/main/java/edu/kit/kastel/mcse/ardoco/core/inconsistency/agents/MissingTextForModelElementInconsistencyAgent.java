/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.InconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.informants.MissingTextForModelElementInconsistencyExtractor;

public class MissingTextForModelElementInconsistencyAgent extends InconsistencyAgent {

    private final List<Informant> extractors;

    @Configurable
    private List<String> enabledExtractors;

    public MissingTextForModelElementInconsistencyAgent(DataRepository dataRepository) {
        super("MissingTextForModelElementInconsistencyAgent", dataRepository);

        extractors = List.of(new MissingTextForModelElementInconsistencyExtractor(dataRepository));
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
