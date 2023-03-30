/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.configuration;

import java.util.Map;

public interface IConfigurable {
    void applyConfiguration(Map<String, String> additionalConfiguration);

    Map<String, String> getLastAppliedConfiguration();
}
