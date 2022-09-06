/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.informalin.pipeline.AbstractPipelineStep;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.extractors.ComputerScienceWordsInformant;

/**
 * This agent uses data from DBPedia to mark default words in computer science.
 */
public class ComputerScienceWordsAgent extends PipelineAgent {

    private final List<Informant> extractors;

    @Configurable
    private List<String> enabledExtractors;

    public ComputerScienceWordsAgent(DataRepository dataRepository) {
        super(ComputerScienceWordsAgent.class.getSimpleName(), dataRepository);
        extractors = List.of(new ComputerScienceWordsInformant(dataRepository));
        enabledExtractors = extractors.stream().map(AbstractPipelineStep::getId).toList();
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledExtractors, extractors);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        extractors.forEach(e -> e.applyConfiguration(additionalConfiguration));
    }

}
