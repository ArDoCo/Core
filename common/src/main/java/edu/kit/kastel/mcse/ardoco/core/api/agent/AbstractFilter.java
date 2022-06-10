/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.agent;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.pipeline.AbstractPipelineStep;

/**
 * @author Jan Keim
 *
 */
public abstract class AbstractFilter extends AbstractPipelineStep {

    protected AbstractFilter(String id, DataRepository dataRepository) {
        super(id, dataRepository);
    }
}
