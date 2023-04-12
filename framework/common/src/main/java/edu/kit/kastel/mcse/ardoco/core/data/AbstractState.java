/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.data;

import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.configuration.AbstractConfigurable;

public abstract class AbstractState extends AbstractConfigurable implements PipelineStepData {

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> map) {
        // empty
    }
}
