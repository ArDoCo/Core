/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.agent;

import java.util.List;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.pipeline.Pipeline;

public abstract class PipelineAgent extends Pipeline implements Agent {

    protected PipelineAgent(String id, DataRepository dataRepository) {
        super(id, dataRepository);
    }

    @Override
    public final void run() {
        setUpPipelineSteps();
        super.run();
    }

    protected void setUpPipelineSteps() {
        for (var extractor : getEnabledPipelineSteps()) {
            this.addPipelineStep(extractor);
        }
    }

    protected abstract List<Informant> getEnabledPipelineSteps();
}
