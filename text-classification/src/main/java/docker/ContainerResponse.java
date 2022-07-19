/* Licensed under MIT 2022. */
package docker;

/**
 * Defines the response that contains the local API port of a container and the container's id.
 *
 * @param containerId the container id
 * @param apiPort     the local api port
 */
public record ContainerResponse(String containerId, int apiPort) {
}