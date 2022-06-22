/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.agent;

public interface Agent extends Claimant {

    default String getId() {
        return this.getClass().getSimpleName();
    }
}
