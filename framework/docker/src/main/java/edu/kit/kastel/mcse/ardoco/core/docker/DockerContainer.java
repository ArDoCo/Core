/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.docker;

public record DockerContainer(String id, String image, String status, String name) {
    public boolean isRunning() {
        return status().startsWith("Up");
    }
}
