/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.model.pcm;


import edu.kit.kastel.informalin.ontology.OntologyConnector;
import edu.kit.kastel.informalin.ontology.OntologyInterface;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.model.IModelRelation;
import edu.kit.kastel.mcse.ardoco.core.model.Instance;
import java.util.List;
import java.util.Optional;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

/**
 * The Class PcmOntologyModelConnector defines a {@link IModelConnector} that can read PCM Models from Ontologies.
 */
public class PcmOntologyModelConnector implements IModelConnector {
    private static final String ENTITY_NAME_PROPERTY = "entityName_-_NamedElement";

    private static final String ID_PROPERTY = "id_-_Identifier";

    private static Logger logger = LogManager.getLogger(PcmOntologyModelConnector.class);

    private static final String[] TYPES = { "BasicComponent", "CompositeComponent" };
    private OntologyInterface ontologyConnector;

    /**
     * Instantiates a new pcm ontology model connector.
     *
     * @param ontologyUrl Can be a local URL (path to the ontology) or a remote URL
     */
    public PcmOntologyModelConnector(String ontologyUrl) {
        ontologyConnector = new OntologyConnector(ontologyUrl);
    }

    public PcmOntologyModelConnector(OntologyInterface ontologyConnector) {
        this.ontologyConnector = ontologyConnector;
    }

    @Override
    public ImmutableList<IModelInstance> getInstances() {
        MutableList<IModelInstance> instances = Lists.mutable.empty();

        for (String type : TYPES) {
            instances.addAll(getInstancesOfType(type));
        }

        return instances.toImmutable();
    }

    private List<IModelInstance> getInstancesOfType(String type) {
        List<IModelInstance> instances = Lists.mutable.empty();
        Optional<OntClass> optionalClass = ontologyConnector.getClass(type);
        if (optionalClass.isEmpty()) {
            return instances;
        }
        OntClass clazz = optionalClass.get();
        var entityNameProperty = getEntityNameProperty();
        var idProperty = getIdProperty();
        for (Individual individual : ontologyConnector.getIndividualsOfClass(clazz)) {
            var name = individual.getProperty(entityNameProperty).getString();
            var identifier = individual.getProperty(idProperty).getString();
            var instance = new Instance(name, type, identifier);
            instances.add(instance);
        }
        return instances;

    }

    private OntProperty getIdProperty() {
        Optional<OntProperty> optionalProperty = ontologyConnector.getProperty(ID_PROPERTY);
        if (optionalProperty.isEmpty()) {
            throw new IllegalStateException("Cannot find the \"id\" property!");
        }
        return optionalProperty.get();
    }

    private OntProperty getEntityNameProperty() {
        Optional<OntProperty> optionalProperty = ontologyConnector.getProperty(ENTITY_NAME_PROPERTY);
        if (optionalProperty.isEmpty()) {
            throw new IllegalStateException("Cannot find the \"entityName\" property!");
        }
        return optionalProperty.get();
    }

    @Override
    public ImmutableList<IModelRelation> getRelations() {
        logger.warn("This method is not yet implemented and will return an empty list!");
        return Lists.immutable.empty();
    }

}
