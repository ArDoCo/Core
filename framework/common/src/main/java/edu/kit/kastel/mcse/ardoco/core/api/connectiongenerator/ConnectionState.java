/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.connectiongenerator;

import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.InstanceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadSamTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.configuration.IConfigurable;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * The Interface IConnectionState.
 */
public interface ConnectionState extends IConfigurable {

    /**
     * Returns all instance links.
     *
     * @return all instance links
     */
    ImmutableList<InstanceLink> getInstanceLinks();

    /**
     * Returns all instance links with a model instance containing the given name.
     *
     * @param name the name of a model instance
     * @return all instance links with a model instance containing the given name as list
     */
    ImmutableList<InstanceLink> getInstanceLinksByName(String name);

    /**
     * Returns all instance links with a model instance containing the given type.
     *
     * @param type the type of a model instance
     * @return all instance links with a model instance containing the given type as list
     */
    ImmutableList<InstanceLink> getInstanceLinksByType(String type);

    /**
     * Returns all instance links with a model instance containing the given recommended instance.
     *
     * @param recommendedInstance the recommended instance to consider
     * @return all instance links found
     */
    ImmutableList<InstanceLink> getInstanceLinksByRecommendedInstance(RecommendedInstance recommendedInstance);

    /**
     * Returns all instance links with a model instance containing the given name and type.
     *
     * @param name the name of a model instance
     * @param type the type of a model instance
     * @return all instance links with a model instance containing the given name and type as list
     */
    ImmutableList<InstanceLink> getInstanceLinks(String name, String type);

    /**
     * Returns a list of tracelinks that are contained within this connection state.
     *
     * @return list of tracelinks within this connection state
     */
    default ImmutableSortedSet<SadSamTraceLink> getTraceLinks() {
        MutableSortedSet<SadSamTraceLink> traceLinks = SortedSets.mutable.empty();
        for (var instanceLink : getInstanceLinks()) {
            var textualInstance = instanceLink.getTextualInstance();
            for (var nm : textualInstance.getNameMappings()) {
                for (var word : nm.getWords()) {
                    var traceLink = new SadSamTraceLink(instanceLink, word);
                    traceLinks.add(traceLink);
                }
            }
        }
        return traceLinks.toImmutable();
    }

    /**
     * Adds the connection of a recommended instance and a model instance to the state. If the model instance is already
     * contained by the state it is extended. Elsewhere a new instance link is created
     *
     * @param recommendedModelInstance the recommended instance
     * @param instance                 the model instance
     * @param claimant                 the claimant
     * @param probability              the probability of the link
     */
    void addToLinks(RecommendedInstance recommendedModelInstance, ModelInstance instance, Claimant claimant, double probability);

    /**
     * Checks if an instance link is already contained by the state.
     *
     * @param instanceLink the given instance link
     * @return true if it is already contained
     */
    boolean isContainedByInstanceLinks(InstanceLink instanceLink);

    /**
     * Removes an instance link from the state.
     *
     * @param instanceMapping the instance link to remove
     */
    void removeFromMappings(InstanceLink instanceMapping);

    /**
     * Removes all instance links containing the given instance.
     *
     * @param instance the given instance
     */
    void removeAllInstanceLinksWith(ModelInstance instance);

    /**
     * Removes all instance links containing the given recommended instance.
     *
     * @param instance the given recommended instance
     */
    void removeAllInstanceLinksWith(RecommendedInstance instance);

}
