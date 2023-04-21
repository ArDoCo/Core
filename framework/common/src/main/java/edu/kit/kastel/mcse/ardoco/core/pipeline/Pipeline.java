/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.pipeline;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;

/**
 * Class that represents a pipeline that can consist of multiple {@link AbstractPipelineStep AbstractPipelineSteps}.
 * Steps are executed consecutively one after another in the order they were added to the pipeline. Execution calls the
 * {@link #run()} method of the different {@link AbstractPipelineStep AbstractPipelineSteps}.
 */
public class Pipeline extends AbstractPipelineStep {
    private final List<AbstractPipelineStep> pipelineSteps;

    /**
     * Constructs a Pipeline with the given id and {@link DataRepository}.
     *
     * @param id             id for the pipeline
     * @param dataRepository {@link DataRepository} that should be used for fetching and saving data
     */
    public Pipeline(String id, DataRepository dataRepository) {
        super(id, dataRepository);
        this.pipelineSteps = new ArrayList<>();
    }

    /**
     * Constructs a Pipeline with the given id and {@link DataRepository}.
     *
     * @param id             id for the pipeline
     * @param dataRepository {@link DataRepository} that should be used for fetching and saving data
     * @param pipelineSteps  List of {@link AbstractPipelineStep} that should be added to the constructed pipeline
     */
    public Pipeline(String id, DataRepository dataRepository, List<AbstractPipelineStep> pipelineSteps) {
        super(id, dataRepository);
        this.pipelineSteps = pipelineSteps;
    }

    /**
     * Returns whether there were any pipeline steps added
     *
     * @return whether there were any pipeline steps added
     */
    public boolean hasPipelineSteps() {
        return !pipelineSteps.isEmpty();
    }

    /**
     * Adds a {@link AbstractPipelineStep} to the execution list of this pipeline
     *
     * @param pipelineStep step that should be added
     * @return True, if the step was added successfully. Otherwise, returns false
     */
    public boolean addPipelineStep(AbstractPipelineStep pipelineStep) {
        return this.pipelineSteps.add(pipelineStep);
    }

    @Override
    public final void run() {
        preparePipelineSteps();
        for (var pipelineStep : this.pipelineSteps) {
            logger.info("Starting {} - {}", this.getId(), pipelineStep.getId());
            var start = Instant.now();

            pipelineStep.run();

            if (logger.isInfoEnabled()) {
                var end = Instant.now();
                var duration = Duration.between(start, end);
                String durationString = String.format("%01d.%03d s", duration.toSecondsPart(), duration.toMillisPart());
                logger.info("Finished {} - {} in {}", this.getId(), pipelineStep.getId(), durationString);
            }
        }
    }

    /**
     * This method is called at the start of running the pipeline. Within this method, the added PipelineSteps are prepared.
     * Sub-classes of Pipeline can override it with special cases.
     * It is recommended that you apply the Map from {@link #getLastAppliedConfiguration()} via {@link #applyConfiguration(Map)} to each pipeline step.
     * You can do that on your own if you need special treatment or by default call {@link #delegateApplyConfigurationToInternalObjects(Map)}.
     * The base version does apply the last configuration via the default call.
     */
    protected void preparePipelineSteps() {
        delegateApplyConfigurationToInternalObjects(getLastAppliedConfiguration());
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        this.pipelineSteps.forEach(it -> it.applyConfiguration(additionalConfiguration));
    }
}
