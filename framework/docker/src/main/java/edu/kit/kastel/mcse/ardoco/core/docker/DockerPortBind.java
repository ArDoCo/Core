/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.docker;

public record DockerPortBind(int hostPort, int containerPort, boolean wildcard) {
    public boolean valid() {
        return hostPort > 0 && containerPort > 0;
    }
}
