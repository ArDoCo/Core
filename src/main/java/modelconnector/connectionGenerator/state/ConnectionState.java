package modelconnector.connectionGenerator.state;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import modelconnector.modelExtractor.state.Instance;
import modelconnector.modelExtractor.state.Relation;
import modelconnector.recommendationGenerator.state.RecommendedInstance;
import modelconnector.recommendationGenerator.state.RecommendedRelation;
import modelconnector.textExtractor.state.NounMapping;

/**
 * The connection state encapsulates all connections between the model extraction state and the recommendation state.
 * These connections are stored in instance and relation links.
 *
 * @author Sophie
 *
 */
public class ConnectionState {

    private Set<InstanceLink> instanceLinks;
    private Set<RelationLink> relationLinks;

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
    public List<InstanceLink> getInstanceLinks() {
        return new ArrayList<>(instanceLinks);
    }

    /**
     * Returns all instance links with a model instance containing the given name.
     *
     * @param name
     *            the name of a model instance
     * @return all instance links with a model instance containing the given name as list
     */
    public List<InstanceLink> getInstanceLinksByName(String name) {
        return instanceLinks.stream()
                            .filter(imapping -> imapping.getModelInstance()
                                                        .getNames()
                                                        .contains(name))
                            .collect(Collectors.toList());
    }

    /**
     * Returns all instance links with a model instance containing the given type.
     *
     * @param type
     *            the type of a model instance
     * @return all instance links with a model instance containing the given type as list
     */
    public List<InstanceLink> getInstanceLinksByType(String type) {
        return instanceLinks.stream()
                            .filter(ilink -> ilink.getModelInstance()
                                                  .getTypes()
                                                  .contains(type))
                            .collect(Collectors.toList());
    }

    public List<InstanceLink> getInstanceLinksByRecommendedInstance(RecommendedInstance recommendedInstance) {
        return instanceLinks.stream()
                            .filter(il -> il.getTextualInstance()
                                            .equals(recommendedInstance))
                            .collect(Collectors.toList());
    }

    /**
     * Returns all instance links with a model instance containing the given name and type.
     *
     * @param type
     *            the type of a model instance
     * @param name
     *            the name of a model instance
     * @return all instance links with a model instance containing the given name and type as list
     */
    public List<InstanceLink> getInstanceLinks(String name, String type) {
        return instanceLinks.stream()
                            .filter(imapping -> imapping.getModelInstance()
                                                        .getNames()
                                                        .contains(name))//
                            .filter(imapping -> imapping.getModelInstance()
                                                        .getTypes()
                                                        .contains(type))
                            .collect(Collectors.toList());
    }

    /**
     * Adds the connection of a recommended instance and a model instance to the state. If the model instance is already
     * contained by the state it is extended. Elsewhere a new instance link is created
     *
     * @param recommendedModelInstance
     *            the recommended instance
     * @param instance
     *            the model instance
     * @param probability
     *            the probability of the link
     */
    public void addToLinks(RecommendedInstance recommendedModelInstance, Instance instance, double probability) {

        InstanceLink instancelink = new InstanceLink(recommendedModelInstance, instance, probability);
        if (!isContainedByInstanceLinks(instancelink)) {
            instanceLinks.add(instancelink);
        } else {
            Optional<InstanceLink> optionalInstanceLink = instanceLinks.stream()
                                                                       .filter(il -> il.equals(instancelink))
                                                                       .findFirst();
            if (optionalInstanceLink.isPresent()) {
                InstanceLink instanceLink = optionalInstanceLink.get();
                List<NounMapping> nameMappings = instancelink.getTextualInstance()
                                                             .getNameMappings();
                List<NounMapping> typeMappings = instancelink.getTextualInstance()
                                                             .getTypeMappings();
                instanceLink.getTextualInstance()
                            .addMappings(nameMappings, typeMappings);
            }
        }
    }

    /**
     * Checks if an instance link is already contained by the state.
     *
     * @param instanceLink
     *            the given instance link
     * @return true if it is already contained
     */
    public boolean isContainedByInstanceLinks(InstanceLink instanceLink) {
        return instanceLinks.contains(instanceLink);
    }

    /**
     * Removes an instance link from the state
     *
     * @param instanceMapping
     *            the instance link to remove
     */
    public void removeFromMappings(InstanceLink instanceMapping) {
        instanceLinks.remove(instanceMapping);
    }

    /**
     * Removes all instance links containing the given instance
     *
     * @param instance
     *            the given instance
     */
    public void removeAllInstanceLinksWith(Instance instance) {
        instanceLinks.removeIf(mapping -> mapping.getModelInstance()
                                                 .equals(instance));
    }

    /**
     * Removes all instance links containing the given recommended instance
     *
     * @param instance
     *            the given recommended instance
     */
    public void removeAllInstanceLinksWith(RecommendedInstance instance) {
        instanceLinks.removeIf(mapping -> mapping.getTextualInstance()
                                                 .equals(instance));
    }

    /**
     * Adds an instance link to the state.
     *
     * @param instanceMapping
     *            the instance link to add
     */
    public void addToLinks(InstanceLink instanceMapping) {
        this.addToLinks(instanceMapping.getTextualInstance(), instanceMapping.getModelInstance(),
                instanceMapping.getProbability());
    }

    /**
     * Returns all relation links.
     *
     * @return all relation links of this state as list
     */
    public List<RelationLink> getRelationLinks() {
        return new ArrayList<>(relationLinks);
    }

    /**
     * Adds the connection of a recommended relation and a model relation to the state if it is not already contained.
     *
     * @param recommendedModelRelation
     *            the recommended relation
     * @param relation
     *            the model relation
     * @param probability
     *            the probability of the link
     */
    public void addToLinks(RecommendedRelation recommendedModelRelation, Relation relation, double probability) {
        RelationLink rel = new RelationLink(recommendedModelRelation, relation, probability);
        if (!isContainedByRelationLinks(rel)) {
            relationLinks.add(rel);
        } else {
            Optional<RelationLink> optionalRelationLink = relationLinks.stream()
                                                                       .filter(rela -> rela.equals(rel))
                                                                       .findFirst();
            if (optionalRelationLink.isPresent()) {
                RelationLink relationLink = optionalRelationLink.get();
                relationLink.getTextualRelation()
                            .addOccurrences(recommendedModelRelation.getNodes());
            }

        }
    }

    /**
     * Adds a relation link to the state.
     *
     * @param relationMapping
     *            the relation link to add
     */
    public void addToLinks(RelationLink relationMapping) {
        this.addToLinks(relationMapping.getTextualRelation(), relationMapping.getModelRelation(),
                relationMapping.getProbability());
    }

    /**
     * Checks if a relation link is already contained by the state.
     *
     * @param relationMapping
     * @return true, if the relation link is already contained. False if not.
     */
    public boolean isContainedByRelationLinks(RelationLink relationMapping) {
        return relationLinks.contains(relationMapping);
    }

    /**
     * Removes a given relation link from the state.
     *
     * @param relationMapping
     *            the given relation link
     */
    public void removeFromMappings(RelationLink relationMapping) {
        relationLinks.remove(relationMapping);
    }

    /**
     * Removes all relation links with a given model relation.
     *
     * @param relation
     *            the relation to search for
     */
    public void removeAllMappingsWith(Relation relation) {
        relationLinks.removeIf(mapping -> mapping.getModelRelation()
                                                 .equals(relation));
    }

    /**
     * Removes all relation links with a given recommended relation.
     *
     * @param relation
     *            the recommended relation to search for
     */
    public void removeAllMappingsWith(RecommendedRelation relation) {
        relationLinks.removeIf(mapping -> mapping.getTextualRelation()
                                                 .equals(relation));
    }
}
