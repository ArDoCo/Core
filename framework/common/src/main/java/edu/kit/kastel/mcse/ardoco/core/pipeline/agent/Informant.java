/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.pipeline.agent;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;

public abstract class Informant extends AbstractPipelineStep implements Claimant {
    protected Informant(String id, DataRepository dataRepository) {
        super(id, dataRepository);
    }
}
