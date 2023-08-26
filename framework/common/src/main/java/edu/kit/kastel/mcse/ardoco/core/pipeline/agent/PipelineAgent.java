/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.pipeline.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.configuration.ChildClassConfigurable;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.pipeline.Pipeline;

/**
 * This class represents a pipeline agent that calculates some results for an {@link AbstractExecutionStage} execution stage}.
 *
 * Implementing classes need to override. Additionally, sub-classes are free to override {@link #initializeState()} to execute code at the beginning of the
 * initialization before the main processing.
 */
public abstract class PipelineAgent extends Pipeline implements Agent {
    private final List<? extends Informant> informants;

    @Configurable
    @ChildClassConfigurable
    private List<String> enabledInformants;

    protected PipelineAgent(List<? extends Informant> informants, String id, DataRepository dataRepository) {
        super(id, dataRepository);
        this.informants = new ArrayList<>(informants);
        this.enabledInformants = informants.stream().map(Informant::getId).toList();
    }

    @Override
    protected final void preparePipelineSteps() {
        super.preparePipelineSteps();
        initialize();
    }

    /**
     * Called before all informants
     */
    protected void before() {
        //Nothing by default
    }

    /**
     * Called after all informants
     */
    protected void after() {
        //Nothing by default
    }

    /**
     * Initialize the execution
     */
    protected final void initialize() {
        initializeState();
        for (var informant : informants) {
            if (enabledInformants.contains(informant.getId())) {
                this.addPipelineStep(informant);
            }
        }
    }

    /**
     * If necessary, override this method to additionally initialize the state before the processing
     */
    protected void initializeState() {
        // do nothing here
    }

    /**
     * {@return the informants including disabled}
     */
    public List<Informant> getInformants() {
        return List.copyOf(informants);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        super.delegateApplyConfigurationToInternalObjects(additionalConfiguration);
        informants.forEach(filter -> filter.applyConfiguration(additionalConfiguration));
    }

}
