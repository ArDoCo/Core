/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.docker;

import java.util.List;

public record DockerImage(String repository, String tag, List<Integer> exposedPorts) {
    public boolean isNone() {
        return this.repository.equals("<none>") && this.tag.equals("<none>");
    }

    public String repositoryWithTag() {
        return repository + ":" + tag;
    }
}
