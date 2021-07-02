package edu.kit.kastel.mcse.ardoco.core.model.pcm;

import java.util.List;
import java.util.Optional;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.datastructures.Instance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRelation;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.exception.InconsistentModelException;
import edu.kit.kastel.mcse.ardoco.core.ontology.OntologyConnector;

public class PcmOntologyModelConnector implements IModelConnector {
    private static final String ENTITY_NAME_PROPERTY = "entityName_-_NamedElement";

    private static final String ID_PROPERTY = "id_-_Identifier";

    private static Logger logger = LogManager.getLogger(PcmOntologyModelConnector.class);

    private static final String[] TYPES = { "BasicComponent", "CompositeComponent" };
    private OntologyConnector ontologyConnector;

    private String pathToOntology;

    /**
     * @param ontologyUrl Can be a local URL (path to the ontology) or a remote URL
     */
    public PcmOntologyModelConnector(String ontologyUrl) {
        pathToOntology = ontologyUrl;
        ontologyConnector = new OntologyConnector(pathToOntology);
    }

    @Override
    public List<IInstance> getInstances() {
        MutableList<IInstance> instances = Lists.mutable.empty();

        for (String type : TYPES) {
            instances.addAll(getInstancesOfType(type));
        }

        return instances;
    }

    private List<IInstance> getInstancesOfType(String type) {
        List<IInstance> instances = Lists.mutable.empty();
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
    public List<IRelation> getRelations(List<IInstance> instances) throws InconsistentModelException {
        logger.warn("This method is not yet implemented and will return an empty list!");
        return Lists.mutable.empty();
    }

}
