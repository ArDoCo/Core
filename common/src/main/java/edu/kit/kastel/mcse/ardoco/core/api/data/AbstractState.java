/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.data;

import java.util.Map;

import edu.kit.kastel.informalin.framework.configuration.AbstractConfigurable;
import edu.kit.kastel.informalin.framework.data.PipelineStepData;

public abstract class AbstractState extends AbstractConfigurable implements PipelineStepData {

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> map) {
        // empty
    }
}
