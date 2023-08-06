/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.pipeline.agent;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.pipeline.Pipeline;

/**
 * This class represents a pipeline agent that calculates some results for an {@link AbstractExecutionStage} execution stage}.
 *
 * Implementing classes need to override {@link #getEnabledPipelineSteps()}. Additionally, sub-classes are free to override {@link #initializeState()} to
 * execute code at the beginning of the initialization before the main processing.
 */
public abstract class PipelineAgent extends Pipeline implements Agent {
    private final List<? extends Informant> informants;

    protected PipelineAgent(String id, DataRepository dataRepository, List<? extends Informant> informants) {
        super(id, dataRepository);
        this.informants = informants;
    }

    @Override
    protected final void preparePipelineSteps() {
        initialize();
        super.preparePipelineSteps();
    }

    /**
     * Initialize the execution
     */
    protected final void initialize() {
        initializeState();
        for (var informant : getEnabledPipelineSteps()) {
            this.addPipelineStep(informant);
        }
    }

    /**
     * If necessary, override this method to additionally initialize the state before the processing
     */
    protected void initializeState() {
        // do nothing here
    }

    /**
     * Return the enabled pipeline steps (informants)
     *
     * @return the list of Informants
     */
    protected abstract List<Informant> getEnabledPipelineSteps();

    /**
     * {@return the informants including disabled}
     */
    public List<Informant> getInformants() {
        return List.copyOf(informants);
    }

    /**
     * {@return the class names of all informants including disabled}
     */
    public List<String> getInformantClassNames() {
        return informants.stream().map(Informant::getClass).map(Class::getSimpleName).toList();
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        getInformants().forEach(e -> e.applyConfiguration(additionalConfiguration));
    }
}
