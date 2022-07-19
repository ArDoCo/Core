package docker;

/**
 * A helper class that executed cmd line commands.
 *
 * @author Dominik Fuchss
 */

public record DockerPortBind(int hostPort, int containerPort, boolean wildcard) {
    public boolean valid() {
        return hostPort > 0 && containerPort > 0;
    }
}