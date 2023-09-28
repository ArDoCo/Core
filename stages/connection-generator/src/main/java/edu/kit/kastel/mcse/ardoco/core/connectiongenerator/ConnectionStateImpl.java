/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.connectiongenerator.ConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.InstanceLink;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * The connection state encapsulates all connections between the model extraction state and the recommendation state.
 * These connections are stored in instance and relation links.
 */
public class ConnectionStateImpl extends AbstractState implements ConnectionState {

    private transient Set<InstanceLink> instanceLinks;

    /**
     * Creates a new connection state.
     */
    public ConnectionStateImpl() {
        instanceLinks = new HashSet<>();
    }

    /**
     * Returns all instance links.
     *
     * @return all instance links
     */
    @Override
    public ImmutableList<InstanceLink> getInstanceLinks() {
        return Lists.immutable.withAll(instanceLinks);
    }

    /**
     * Returns all instance links with a model instance containing the given name.
     *
     * @param name the name of a model instance
     * @return all instance links with a model instance containing the given name as list
     */
    @Override
    public ImmutableList<InstanceLink> getInstanceLinksByName(String name) {
        return Lists.immutable.fromStream(instanceLinks.stream().filter(imapping -> imapping.getEntity().getNameParts().contains(name)));
    }

    /**
     * Returns all instance links with a model instance containing the given type.
     *
     * @param type the type of a model instance
     * @return all instance links with a model instance containing the given type as list
     */
    @Override
    public ImmutableList<InstanceLink> getInstanceLinksByType(String type) {
        return Lists.immutable.fromStream(instanceLinks.stream().filter(ilink -> ilink.getEntity().getClass().getSimpleName().equals(type)));
    }

    @Override
    public ImmutableList<InstanceLink> getInstanceLinksByRecommendedInstance(RecommendedInstance recommendedInstance) {
        return Lists.immutable.fromStream(instanceLinks.stream().filter(il -> il.getTextualInstance().equals(recommendedInstance)));
    }

    /**
     * Returns all instance links with a model instance containing the given name and type.
     *
     * @param type the type of a model instance
     * @param name the name of a model instance
     * @return all instance links with a model instance containing the given name and type as list
     */
    @Override
    public ImmutableList<InstanceLink> getInstanceLinks(String name, String type) {
        return Lists.immutable.fromStream(instanceLinks.stream()
                .filter(imapping -> imapping.getEntity().getNameParts().contains(name))//
                .filter(imapping -> Arrays.stream(imapping.getEntity().getClass().getSimpleName().split(" ")).toList().contains(type)));
    }

    /**
     * Adds the connection of a recommended instance and an entity to the state. If the entity is already
     * contained by the state it is extended. Elsewhere a new instance link is created
     *
     * @param recommendedModelInstance the recommended instance
     * @param entity                   the entity
     * @param probability              the probability of the link
     */
    @Override
    public void addToLinks(RecommendedInstance recommendedModelInstance, Entity entity, Claimant claimant, double probability) {

        var newInstanceLink = new InstanceLink(recommendedModelInstance, entity, claimant, probability);
        if (!isContainedByInstanceLinks(newInstanceLink)) {
            instanceLinks.add(newInstanceLink);
        } else {
            var optionalInstanceLink = instanceLinks.stream().filter(il -> il.equals(newInstanceLink)).findFirst();
            if (optionalInstanceLink.isPresent()) {
                var existingInstanceLink = optionalInstanceLink.get();
                var newNameMappings = newInstanceLink.getTextualInstance().getNameMappings();
                var newTypeMappings = newInstanceLink.getTextualInstance().getTypeMappings();
                existingInstanceLink.getTextualInstance().addMappings(newNameMappings, newTypeMappings);
            }
        }
    }

    /**
     * Checks if an instance link is already contained by the state.
     *
     * @param instanceLink the given instance link
     * @return true if it is already contained
     */
    @Override
    public boolean isContainedByInstanceLinks(InstanceLink instanceLink) {
        return instanceLinks.contains(instanceLink);
    }

    /**
     * Removes an instance link from the state
     *
     * @param instanceMapping the instance link to remove
     */
    @Override
    public void removeFromMappings(InstanceLink instanceMapping) {
        instanceLinks.remove(instanceMapping);
    }

    /**
     * Removes all instance links containing the given entity
     *
     * @param entity  the given entity
     */
    @Override
    public void removeAllInstanceLinksWith(Entity entity) {
        instanceLinks.removeIf(mapping -> mapping.getEntity().equals(entity));
    }

    /**
     * Removes all instance links containing the given recommended instance
     *
     * @param instance the given recommended instance
     */
    @Override
    public void removeAllInstanceLinksWith(RecommendedInstance instance) {
        instanceLinks.removeIf(mapping -> mapping.getTextualInstance().equals(instance));
    }

}
