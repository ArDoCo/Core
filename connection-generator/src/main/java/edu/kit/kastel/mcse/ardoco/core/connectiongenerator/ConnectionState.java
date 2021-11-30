/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator;


import edu.kit.kastel.mcse.ardoco.core.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.model.IModelRelation;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendedRelation;
import edu.kit.kastel.mcse.ardoco.core.textextraction.INounMapping;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

/**
 * The connection state encapsulates all connections between the model extraction state and the recommendation state.
 * These connections are stored in instance and relation links.
 *
 * @author Sophie
 *
 */
public class ConnectionState implements IConnectionState {

    private Set<IInstanceLink> instanceLinks;
    private Set<IRelationLink> relationLinks;

    @Override
    public IConnectionState createCopy() {
        var newState = new ConnectionState();
        newState.instanceLinks = instanceLinks.stream().map(IInstanceLink::createCopy).collect(Collectors.toSet());
        newState.relationLinks = relationLinks.stream().map(IRelationLink::createCopy).collect(Collectors.toSet());
        return newState;
    }

    /**
     * Creates a new connection state.
     */
    public ConnectionState() {
        instanceLinks = new HashSet<>();
        relationLinks = new HashSet<>();
    }

    /**
     * Returns all instance links.
     *
     * @return all instance links
     */
    @Override
    public ImmutableList<IInstanceLink> getInstanceLinks() {
        return Lists.immutable.withAll(instanceLinks);
    }

    /**
     * Returns all instance links with a model instance containing the given name.
     *
     * @param name the name of a model instance
     * @return all instance links with a model instance containing the given name as list
     */
    @Override
    public ImmutableList<IInstanceLink> getInstanceLinksByName(String name) {
        return Lists.immutable.fromStream(instanceLinks.stream().filter(imapping -> imapping.getModelInstance().getNames().contains(name)));
    }

    /**
     * Returns all instance links with a model instance containing the given type.
     *
     * @param type the type of a model instance
     * @return all instance links with a model instance containing the given type as list
     */
    @Override
    public ImmutableList<IInstanceLink> getInstanceLinksByType(String type) {
        return Lists.immutable.fromStream(instanceLinks.stream().filter(ilink -> ilink.getModelInstance().getTypes().contains(type)));
    }

    @Override
    public ImmutableList<IInstanceLink> getInstanceLinksByRecommendedInstance(IRecommendedInstance recommendedInstance) {
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
    public ImmutableList<IInstanceLink> getInstanceLinks(String name, String type) {
        return Lists.immutable.fromStream(instanceLinks.stream()
                .filter(imapping -> imapping.getModelInstance().getNames().contains(name))//
                .filter(imapping -> imapping.getModelInstance().getTypes().contains(type)));
    }

    /**
     * Adds the connection of a recommended instance and a model instance to the state. If the model instance is already
     * contained by the state it is extended. Elsewhere a new instance link is created
     *
     * @param recommendedModelInstance the recommended instance
     * @param instance                 the model instance
     * @param probability              the probability of the link
     */
    @Override
    public void addToLinks(IRecommendedInstance recommendedModelInstance, IModelInstance instance, double probability) {

        IInstanceLink instancelink = new InstanceLink(recommendedModelInstance, instance, probability);
        if (!isContainedByInstanceLinks(instancelink)) {
            instanceLinks.add(instancelink);
        } else {
            Optional<IInstanceLink> optionalInstanceLink = instanceLinks.stream().filter(il -> il.equals(instancelink)).findFirst();
            if (optionalInstanceLink.isPresent()) {
                IInstanceLink instanceLink = optionalInstanceLink.get();
                ImmutableList<INounMapping> nameMappings = instancelink.getTextualInstance().getNameMappings();
                ImmutableList<INounMapping> typeMappings = instancelink.getTextualInstance().getTypeMappings();
                instanceLink.getTextualInstance().addMappings(nameMappings, typeMappings);
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
    public boolean isContainedByInstanceLinks(IInstanceLink instanceLink) {
        return instanceLinks.contains(instanceLink);
    }

    /**
     * Removes an instance link from the state
     *
     * @param instanceMapping the instance link to remove
     */
    @Override
    public void removeFromMappings(IInstanceLink instanceMapping) {
        instanceLinks.remove(instanceMapping);
    }

    /**
     * Removes all instance links containing the given instance
     *
     * @param instance the given instance
     */
    @Override
    public void removeAllInstanceLinksWith(IModelInstance instance) {
        instanceLinks.removeIf(mapping -> mapping.getModelInstance().equals(instance));
    }

    /**
     * Removes all instance links containing the given recommended instance
     *
     * @param instance the given recommended instance
     */
    @Override
    public void removeAllInstanceLinksWith(IRecommendedInstance instance) {
        instanceLinks.removeIf(mapping -> mapping.getTextualInstance().equals(instance));
    }

    /**
     * Adds an instance link to the state.
     *
     * @param instanceMapping the instance link to add
     */
    @Override
    public void addToLinks(IInstanceLink instanceMapping) {
        this.addToLinks(instanceMapping.getTextualInstance(), instanceMapping.getModelInstance(), instanceMapping.getProbability());
    }

    /**
     * Returns all relation links.
     *
     * @return all relation links of this state as list
     */
    @Override
    public ImmutableList<IRelationLink> getRelationLinks() {
        return Lists.immutable.withAll(relationLinks);
    }

    /**
     * Adds the connection of a recommended relation and a model relation to the state if it is not already contained.
     *
     * @param recommendedModelRelation the recommended relation
     * @param relation                 the model relation
     * @param probability              the probability of the link
     */
    @Override
    public void addToLinks(IRecommendedRelation recommendedModelRelation, IModelRelation relation, double probability) {
        IRelationLink rel = new RelationLink(recommendedModelRelation, relation, probability);
        if (!isContainedByRelationLinks(rel)) {
            relationLinks.add(rel);
        } else {
            Optional<IRelationLink> optionalRelationLink = relationLinks.stream().filter(rela -> rela.equals(rel)).findFirst();
            if (optionalRelationLink.isPresent()) {
                IRelationLink relationLink = optionalRelationLink.get();
                relationLink.getTextualRelation().addOccurrences(recommendedModelRelation.getNodes());
            }

        }
    }

    /**
     * Adds a relation link to the state.
     *
     * @param relationMapping the relation link to add
     */
    @Override
    public void addToLinks(IRelationLink relationMapping) {
        this.addToLinks(relationMapping.getTextualRelation(), relationMapping.getModelRelation(), relationMapping.getProbability());
    }

    /**
     * Checks if a relation link is already contained by the state.
     *
     * @param relationMapping
     * @return true, if the relation link is already contained. False if not.
     */
    @Override
    public boolean isContainedByRelationLinks(IRelationLink relationMapping) {
        return relationLinks.contains(relationMapping);
    }

    /**
     * Removes a given relation link from the state.
     *
     * @param relationMapping the given relation link
     */
    @Override
    public void removeFromMappings(IRelationLink relationMapping) {
        relationLinks.remove(relationMapping);
    }

    /**
     * Removes all relation links with a given model relation.
     *
     * @param relation the relation to search for
     */
    @Override
    public void removeAllMappingsWith(IModelRelation relation) {
        relationLinks.removeIf(mapping -> mapping.getModelRelation().equals(relation));
    }

    /**
     * Removes all relation links with a given recommended relation.
     *
     * @param relation the recommended relation to search for
     */
    @Override
    public void removeAllMappingsWith(IRecommendedRelation relation) {
        relationLinks.removeIf(mapping -> mapping.getTextualRelation().equals(relation));
    }

}
