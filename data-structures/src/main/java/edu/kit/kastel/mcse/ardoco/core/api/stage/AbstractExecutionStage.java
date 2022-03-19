/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.stage;

import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.common.AbstractConfigurable;

public abstract class AbstractExecutionStage extends AbstractConfigurable implements IExecutionStage {
    @Override
    protected final void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // You should use #execute(DataStructure data, Map<String, String> additionalSettings)
    }
}
