package docker;

import java.util.List;

/**
 * A helper class that executed cmd line commands.
 *
 * @author Dominik Fuchss
 */

public record DockerImage(String repository, String tag, List<Integer> exposedPorts) {
    public boolean isNone() {
        return this.repository.equals("<none>") && this.tag.equals("<none>");
    }

    public String repositoryWithTag() {
        return repository + ":" + tag;
    }
}