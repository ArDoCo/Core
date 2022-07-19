package docker;

/**
 * A helper class that executed cmd line commands.
 *
 * @author Dominik Fuchss
 */

public record DockerContainer(String id, String image, String status, String name) {
    public boolean isRunning() {
        return status().startsWith("Up");
    }
}