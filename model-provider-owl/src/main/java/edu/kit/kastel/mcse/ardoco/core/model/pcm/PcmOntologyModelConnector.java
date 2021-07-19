package edu.kit.kastel.mcse.ardoco.core.model.pcm;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
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
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.datastructures.Instance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelRelation;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;

/**
 * The Class PcmOntologyModelConnector defines a {@link IModelConnector} that can read PCM Models from Ontologies.
 */
public class PcmOntologyModelConnector implements IModelConnector {
    private static final Logger logger = LogManager.getLogger(PcmOntologyModelConnector.class);

    private static final String DEFAULT_PREFIX = "";
    private static final String[] TYPES = { "BasicComponent", "CompositeComponent" };

    private static OntModelSpec modelSpec = OntModelSpec.OWL_DL_MEM;
    private final OntModel ontModel;

    private String pathToOntology;

    /**
     * Instantiates a new pcm ontology model connector.
     *
     * @param ontologyUrl Can be a local URL (path to the ontology) or a remote URL
     */
    public PcmOntologyModelConnector(String ontologyUrl) {
        pathToOntology = ontologyUrl;
        ontModel = loadOntology(pathToOntology);
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
        Optional<OntClass> optionalClass = getClass(type);
        if (optionalClass.isEmpty()) {
            return instances;
        }
        OntClass clazz = optionalClass.get();
        var entityNameProperty = getEntityNameProperty();
        var idProperty = getIdProperty();
        for (Individual individual : getInstancesOfClass(clazz)) {
            var name = individual.getProperty(entityNameProperty).getString();
            var identifier = individual.getProperty(idProperty).getString();
            var instance = new Instance(name, type, identifier);
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
    public ImmutableList<IModelRelation> getRelations() {
        logger.warn("This method is not yet implemented and will return an empty list!");
        return Lists.immutable.empty();
    }

    private static OntModel loadOntology(String ontologyUrl) {
        // ontology either conforms to the URI convention at the start, then we can skip preprocessing
        // looks if it is a file and checks if it exists.
        // Then prepends "file:///"
        if (!ontologyUrl.startsWith("file") && !ontologyUrl.startsWith("https")) {
            var file = new File(ontologyUrl);
            if (!file.exists()) {
                logger.warn("Cannot load ontology");
                throw new IllegalArgumentException("Provided Ontology URL cannot be accessed");
            }
            var uri = file.toURI();
            URL url;
            try {
                url = uri.toURL();
            } catch (MalformedURLException e) {
                logger.warn("Cannot load ontology");
                throw new IllegalArgumentException("Provided Ontology URL cannot be accessed");
            }
            ontologyUrl = url.toString();
        }

        var ontModel = ModelFactory.createOntologyModel(modelSpec);
        ontModel.read(ontologyUrl);
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

    private Optional<OntClass> getClass(String className) {
        var prefixes = ontModel.getNsPrefixMap().keySet();
        for (var prefix : prefixes) {
            var optClass = getClass(className, prefix);
            if (optClass.isPresent()) {
                return optClass;
            }
        }
        return Optional.empty();
    }

    private Optional<OntClass> getClass(String className, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            prefix = DEFAULT_PREFIX;
        }
        var uri = createUri(prefix, className);
        return getClassByIri(uri);
    }

    private Optional<OntClass> getClassByIri(String iri) {
        String uri = ontModel.expandPrefix(iri);
        var clazz = ontModel.getOntClass(uri);

        return Optional.ofNullable(clazz);
    }

    private MutableList<Individual> getInstancesOfClass(OntClass clazz) {
        return createMutableListFromIterator(ontModel.listIndividuals(clazz));
    }

    private Optional<Property> getProperty(String dataPropertyLocalName) {
        var prefixes = ontModel.getNsPrefixMap().keySet();
        for (var prefix : prefixes) {
            var optDP = getProperty(dataPropertyLocalName, prefix);
            if (optDP.isPresent()) {
                return optDP;
            }
        }
        return Optional.empty();
    }

    private Optional<Property> getProperty(String dataPropertyLocalName, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            prefix = DEFAULT_PREFIX;
        }
        var uri = createUri(prefix, dataPropertyLocalName);
        return getPropertyByUri(uri);
    }

    private Optional<Property> getPropertyByUri(String dataPropertyUri) {
        var datatypeProperty = ontModel.getDatatypeProperty(dataPropertyUri);
        return Optional.ofNullable(datatypeProperty);
    }

    private static <T> MutableList<T> createMutableListFromIterator(Iterator<T> iterator) {
        return Lists.mutable.ofAll(() -> iterator);
    }

}
