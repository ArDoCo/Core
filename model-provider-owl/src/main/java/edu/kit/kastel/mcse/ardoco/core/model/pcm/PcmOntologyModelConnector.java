package edu.kit.kastel.mcse.ardoco.core.model.pcm;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRelation;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.exception.InconsistentModelException;
import edu.kit.kastel.mcse.ardoco.core.ontology.OntologyConnector;

public class PcmOntologyModelConnector implements IModelConnector {
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
            instances.addAll(ontologyConnector.getInstancesOfType(type));
        }

        return instances;
    }

    @Override
    public List<IRelation> getRelations(List<IInstance> instances) throws InconsistentModelException {
        logger.warn("This method is not yet implemented and will return an empty list!");
        return Lists.mutable.empty();
    }

}
