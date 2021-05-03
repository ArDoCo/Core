package edu.kit.kastel.mcse.ardoco.core.model.pcm;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.datastructures.Instance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRelation;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.exception.InconsistentModelException;

public class PcmOntologyModelConnector implements IModelConnector {
    private static Logger logger = LogManager.getLogger(PcmOntologyModelConnector.class);

    private static final String DEFAULT_PREFIX = "";
    private static final String ONT_LANG = "TURTLE";
    private static final String[] TYPES = { "BasicComponent", "CompositeComponent" };

    private static OntModelSpec modelSpec = OntModelSpec.OWL_DL_MEM;
    private final OntModel ontModel;

    private String pathToOntology;

    /**
     * @param ontologyUrl Can be a local URL (path to the ontology) or a remote URL
     */
    public PcmOntologyModelConnector(String ontologyUrl) {
        pathToOntology = ontologyUrl;
        ontModel = loadOntology(pathToOntology);
    }

    @Override
    public List<IInstance> getInstances() {
        MutableList<IInstance> instances = Lists.mutable.empty();

        for (String type : TYPES) {
            instances.addAll(getInstancesOfType(type));
        }

        return instances;
    }

    public List<IInstance> getInstancesOfType(String type) {
        List<IInstance> instances = Lists.mutable.empty();
        Optional<OntClass> optionalClass = getClass(type);
        if (optionalClass.isEmpty()) {
            return instances;
        }
        OntClass clazz = optionalClass.get();
        Property entityNameProperty = getEntityNameProperty();
        Property idProperty = getIdProperty();
        for (Individual individual : getInstancesOfClass(clazz)) {
            String name = individual.getProperty(entityNameProperty).getString();
            String identifier = individual.getProperty(idProperty).getString();
            Instance instance = new Instance(name, type, identifier);
            instances.add(instance);
        }
        return instances;

    }

    private Property getIdProperty() {
        Optional<Property> optionalProperty = getProperty("id_-_Identifier");
        if (optionalProperty.isEmpty()) {
            throw new IllegalStateException("Cannot find the \"id\" property!");
        }
        return optionalProperty.get();
    }

    private Property getEntityNameProperty() {
        Optional<Property> optionalProperty = getProperty("entityName_-_NamedElement");
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

    private static OntModel loadOntology(String ontologyUrl) {
        // ontology either conforms to the URI convention at the start, then we can skip
        // preprocessing
        // preprocessing then looks if it is a file and checks if it exists. Then
        // prepends "file:///"
        if (!ontologyUrl.startsWith("file") && !ontologyUrl.startsWith("https")) {
            File file = new File(ontologyUrl);
            if (!file.exists()) {
                logger.warn("Cannot load ontology");
                throw new IllegalArgumentException("Provided Ontology URL cannot be accessed");
            }
            ontologyUrl = "file:///" + file.getAbsolutePath();
        }

        OntModel ontModel = ModelFactory.createOntologyModel(modelSpec);
        ontModel.read(ontologyUrl, ONT_LANG);
        ontModel.setDynamicImports(true);
        return ontModel;
    }

    private String createUri(String prefix, String suffix) {
        String encodedSuffix = suffix;
        try {
            encodedSuffix = URLEncoder.encode(suffix, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        return ontModel.expandPrefix(prefix + ":" + encodedSuffix);
    }

    public Optional<OntClass> getClass(String className) {
        return Optional.ofNullable(ontModel.getOntClass(createUri(DEFAULT_PREFIX, className)));
    }

    private MutableList<Individual> getInstancesOfClass(OntClass clazz) {
        return createMutableListFromIterator(ontModel.listIndividuals(clazz));
    }

    private Optional<Property> getProperty(String propertyName) {
        String propertyUri = createUri(DEFAULT_PREFIX, propertyName);
        return Optional.ofNullable(ontModel.getDatatypeProperty(propertyUri));
    }

    private static <T> MutableList<T> createMutableListFromIterator(Iterator<T> iterator) {
        return Lists.mutable.ofAll(() -> iterator);
    }

}
