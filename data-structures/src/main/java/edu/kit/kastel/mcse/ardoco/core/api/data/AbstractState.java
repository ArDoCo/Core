package edu.kit.kastel.mcse.ardoco.core.api.data;

import edu.kit.kastel.mcse.ardoco.core.common.AbstractConfigurable;

import java.util.Map;

public abstract class AbstractState extends AbstractConfigurable {
	protected final Map<String, String> configs;

	protected AbstractState(Map<String, String> config) {
		this.configs = Map.copyOf(config);
		this.applyConfiguration(config);
	}

	@Override
	protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
	}
}
