/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.agent;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.pipeline.AbstractPipelineStep;

public abstract class AbstractExtractor extends AbstractPipelineStep implements IClaimant {
    protected AbstractExtractor(String id, DataRepository dataRepository) {
        super(id, dataRepository);
    }
}
