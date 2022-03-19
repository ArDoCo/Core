/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common;

import java.util.Map;

public interface IConfigurable {
    void applyConfiguration(Map<String, String> additionalConfiguration);
}
