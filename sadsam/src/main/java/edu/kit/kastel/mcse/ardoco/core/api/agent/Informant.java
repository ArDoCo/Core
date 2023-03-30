/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.agent;

import edu.kit.kastel.informalin.framework.data.DataRepository;
import edu.kit.kastel.informalin.framework.pipeline.AbstractPipelineStep;

public abstract class Informant extends AbstractPipelineStep implements Claimant {
    protected Informant(String id, DataRepository dataRepository) {
        super(id, dataRepository);
    }
}
