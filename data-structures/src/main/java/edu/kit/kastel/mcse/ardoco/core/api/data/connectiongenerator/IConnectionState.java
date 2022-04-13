/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.agent.IClaimant;
import edu.kit.kastel.mcse.ardoco.core.api.common.IConfigurable;
import edu.kit.kastel.mcse.ardoco.core.api.common.ICopyable;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendedInstance;

/**
 * The Interface IConnectionState.
 */
public interface IConnectionState extends ICopyable<IConnectionState>, IConfigurable {

    /**
     * Returns all instance links.
     *
     * @return all instance links
     */
    ImmutableList<IInstanceLink> getInstanceLinks();

    /**
     * Returns all instance links with a model instance containing the given name.
     *
     * @param name the name of a model instance
     * @return all instance links with a model instance containing the given name as list
     */
    ImmutableList<IInstanceLink> getInstanceLinksByName(String name);

    /**
     * Returns all instance links with a model instance containing the given type.
     *
     * @param type the type of a model instance
     * @return all instance links with a model instance containing the given type as list
     */
    ImmutableList<IInstanceLink> getInstanceLinksByType(String type);

    /**
     * Returns all instance links with a model instance containing the given recommended instance.
     *
     * @param recommendedInstance the recommended instance to consider
     * @return all instance links found
     */
    ImmutableList<IInstanceLink> getInstanceLinksByRecommendedInstance(IRecommendedInstance recommendedInstance);

    /**
     * Returns all instance links with a model instance containing the given name and type.
     *
     * @param name the name of a model instance
     * @param type the type of a model instance
     * @return all instance links with a model instance containing the given name and type as list
     */
    ImmutableList<IInstanceLink> getInstanceLinks(String name, String type);

    /**
     * Returns a list of tracelinks that are contained within this connection state.
     *
     * @return list of tracelinks within this connection state
     */
    default ImmutableSet<TraceLink> getTraceLinks() {
        MutableSet<TraceLink> tracelinks = Sets.mutable.empty();
        for (var instanceLink : getInstanceLinks()) {
            var textualInstance = instanceLink.getTextualInstance();
            for (var nm : textualInstance.getNameMappings()) {
                for (var word : nm.getWords()) {
                    var tracelink = new TraceLink(instanceLink, instanceLink.getModelInstance(), word);
                    tracelinks.add(tracelink);
                }
                for (var word : nm.getCoreferences()) {
                    var tracelink = new TraceLink(instanceLink, instanceLink.getModelInstance(), word);
                    tracelinks.add(tracelink);
                }
            }
        }
        return tracelinks.toImmutable();
    }

    /**
     * Adds the connection of a recommended instance and a model instance to the state. If the model instance is already
     * contained by the state it is extended. Elsewhere a new instance link is created
     *
     * @param recommendedModelInstance the recommended instance
     * @param instance                 the model instance
     * @param probability              the probability of the link
     */
    void addToLinks(IRecommendedInstance recommendedModelInstance, IModelInstance instance, IClaimant claimant, double probability);

    /**
     * Checks if an instance link is already contained by the state.
     *
     * @param instanceLink the given instance link
     * @return true if it is already contained
     */
    boolean isContainedByInstanceLinks(IInstanceLink instanceLink);

    /**
     * Removes an instance link from the state.
     *
     * @param instanceMapping the instance link to remove
     */
    void removeFromMappings(IInstanceLink instanceMapping);

    /**
     * Removes all instance links containing the given instance.
     *
     * @param instance the given instance
     */
    void removeAllInstanceLinksWith(IModelInstance instance);

    /**
     * Removes all instance links containing the given recommended instance.
     *
     * @param instance the given recommended instance
     */
    void removeAllInstanceLinksWith(IRecommendedInstance instance);

}
