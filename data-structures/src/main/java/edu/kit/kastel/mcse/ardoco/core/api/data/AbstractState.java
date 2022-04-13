/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data;

import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.common.AbstractConfigurable;

public abstract class AbstractState extends AbstractConfigurable {
    protected final Map<String, String> configs;

    protected AbstractState(Map<String, String> config) {
        this.configs = Map.copyOf(config);
        this.applyConfiguration(config);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // By Default Nothing To Do
    }
}
