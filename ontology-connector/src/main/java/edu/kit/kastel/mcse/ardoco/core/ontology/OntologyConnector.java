package edu.kit.kastel.mcse.ardoco.core.ontology;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
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
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.riot.Lang;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.datastructures.Instance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstance;

public class OntologyConnector {
    private static Logger logger = LogManager.getLogger(OntologyConnector.class);
    private static final String DEFAULT_PREFIX = "";

    private static OntModelSpec modelSpec = OntModelSpec.OWL_DL_MEM;
    private final OntModel ontModel;

    private String pathToOntology;
    private Ontology ontology;

    public OntologyConnector(String ontologyUrl) {
        pathToOntology = ontologyUrl;
        ontModel = loadOntology(pathToOntology);
        ontology = getBaseOntology().orElseThrow(() -> new IllegalArgumentException("Could not load ontology: No base ontology found"));
    }

    /**
     * Save the ontology to a given file (path). This method uses the RDF/XML language.
     *
     * @param file String containing the path of the file the ontology should be saved to
     * @return true if saving was successful, otherwise false is returned
     */
    public boolean save(String file) {
        return save(file, Lang.RDFXML);
    }

    /**
     * Save the ontology to a given file (path). This method uses the N3 language to save.
     *
     * @param file     String containing the path of the file the ontology should be saved to
     * @param language The language the file should be written in
     * @return true if saving was successful, otherwise false is returned
     */
    public boolean save(String file, Lang language) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
            return false;
        }

        ontModel.write(out, language.getName());
        return true;
    }

    private static OntModel loadOntology(String ontologyUrl) {
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

    /**
     * Add an Ontology based on its IRI
     *
     * @param importIRI the IRI of the ontology that should be imported
     */
    public void addOntologyImport(String importIRI) {
        var importResource = ontModel.createResource(importIRI);
        ontology.addImport(importResource);
        ontModel.loadImports();
    }

    /**
     * Returns the first {@link Ontology} that is not an imported ontology in the {@link OntModel}. This is assumed to
     * be the base ontology.
     *
     * @return the first {@link Ontology} that is not an imported ontology in the {@link OntModel}
     */
    protected Optional<Ontology> getBaseOntology() {
        var importedOntologies = ontModel.listImportedOntologyURIs();
        for (var onto : ontModel.listOntologies().toSet()) {
            var ontologyUri = onto.getURI();
            if (!importedOntologies.contains(ontologyUri)) {
                return Optional.of(onto);
            }
        }
        return Optional.empty();
    }

    protected OntModel getOntModel() {
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

    public List<IInstance> getInstancesOfType(String type) {
        List<IInstance> instances = Lists.mutable.empty();
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

    public Optional<OntClass> getClass(String className) {
        var prefixes = ontModel.getNsPrefixMap().keySet();
        for (var prefix : prefixes) {
            var optClass = getClass(className, prefix);
            if (optClass.isPresent()) {
                return optClass;
            }
        }
        return Optional.empty();
    }

    public Optional<OntClass> getClass(String className, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            prefix = DEFAULT_PREFIX;
        }
        var uri = createUri(prefix, className);
        return getClassByIri(uri);
    }

    public Optional<OntClass> getClassByIri(String iri) {
        String uri = ontModel.expandPrefix(iri);
        var clazz = ontModel.getOntClass(uri);

        return Optional.ofNullable(clazz);
    }

    private MutableList<Individual> getInstancesOfClass(OntClass clazz) {
        return createMutableListFromIterator(ontModel.listIndividuals(clazz));
    }

    public Optional<Property> getProperty(String dataPropertyLocalName) {
        var prefixes = ontModel.getNsPrefixMap().keySet();
        for (var prefix : prefixes) {
            var optDP = getProperty(dataPropertyLocalName, prefix);
            if (optDP.isPresent()) {
                return optDP;
            }
        }
        return Optional.empty();
    }

    public Optional<Property> getProperty(String dataPropertyLocalName, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            prefix = DEFAULT_PREFIX;
        }
        var uri = createUri(prefix, dataPropertyLocalName);
        return getPropertyByUri(uri);
    }

    public Optional<Property> getPropertyByUri(String dataPropertyUri) {
        var datatypeProperty = ontModel.getDatatypeProperty(dataPropertyUri);
        return Optional.ofNullable(datatypeProperty);
    }

    private static <T> MutableList<T> createMutableListFromIterator(Iterator<T> iterator) {
        return Lists.mutable.ofAll(() -> iterator);
    }

}
