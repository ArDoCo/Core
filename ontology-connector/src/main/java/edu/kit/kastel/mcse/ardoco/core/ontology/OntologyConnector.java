package edu.kit.kastel.mcse.ardoco.core.ontology;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.jena.graph.Node.NotLiteral;
import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.reasoner.ValidityReport.Report;
import org.apache.jena.riot.Lang;
import org.apache.jena.shared.Lock;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

/**
 * Adapter that connects your code to an ontology. Provides various methods to decorate the usage of Apache Jena.
 * Although many methods in this class return the objects that are of some type from Apache Jena, you should not
 * directly operate on these classes. The {@link OntologyConnector} also acts as a access controller to make sure that
 * concurrent access does not create invalid states.
 *
 *
 * @author Jan Keim
 *
 */
public class OntologyConnector implements OntologyInterface {
    private static Logger logger = LogManager.getLogger(OntologyConnector.class);

    // Needs to be DL! Otherwise, classes are seen as individual as well, which might have negative affects
    private static OntModelSpec modelSpec = OntModelSpec.OWL_DL_MEM;

    private static final String DEFAULT_PREFIX = "";

    private final OntModel ontModel;
    private OrderedOntologyList.Factory listFactory;

    private String pathToOntology;
    private Ontology ontology;
    private InfModel infModel;

    public OntologyConnector(String ontologyUrl) {
        pathToOntology = ontologyUrl;
        ontModel = loadOntology(pathToOntology);
        ontology = getBaseOntology().orElseThrow(() -> new IllegalArgumentException("Could not load ontology: No base ontology found"));
        listFactory = OrderedOntologyList.Factory.get(this);
    }

    private OntologyConnector() {
        pathToOntology = null;
        ontModel = ModelFactory.createOntologyModel(modelSpec);
        listFactory = OrderedOntologyList.Factory.get(this);
    }

    /**
     * Creates an OntologyConnector based on no existing ontology, so creates an empty ontology.
     *
     * @param defaultNameSpaceUri The default namespace URI
     * @return An OntologyConnector based on no existing ontology
     */
    public static OntologyInterface createWithEmptyOntology(String defaultNameSpaceUri) {
        var ontologyConnector = new OntologyConnector();
        ontologyConnector.ontology = ontologyConnector.ontModel.createOntology(defaultNameSpaceUri);
        ontologyConnector.ontModel.setNsPrefix("", defaultNameSpaceUri);
        ontologyConnector.ontModel.setNsPrefix("xsd", XSD.NS);
        return ontologyConnector;
    }

    /**
     * Validates the ontology. A logger will put out warnings iff there are conflicts.
     *
     * @return <code>true</code> if the ontology is valid and has no conflicts, <code>false</code> if there are
     *         conflicts
     */
    @Override
    public boolean validateOntology() {
        var validationInfModel = ModelFactory.createRDFSModel(ontModel);
        ValidityReport validity = validationInfModel.validate();
        if (validity.isValid()) {
            return true;
        }
        Iterator<Report> i = validity.getReports();
        while (i.hasNext()) {
            logger.warn("Conflict in ontology: {}", i.next());
        }
        return false;
    }

    /**
     * Adds/Sets a namespace prefix
     *
     * @param prefix the new prefix that should be able to use
     * @param uri    the URI that the prefix should be resolved to
     */
    @Override
    public void setNsPrefix(String prefix, String uri) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            ontModel.setNsPrefix(prefix, uri);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Save the ontology to a given file (path). This method uses the RDF/XML language.
     *
     * @param file String containing the path of the file the ontology should be saved to
     * @return true if saving was successful, otherwise false is returned
     */
    @Override
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
    @Override
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

        ontModel.enterCriticalSection(Lock.READ);
        try {
            ontModel.write(out, language.getName());
        } finally {
            ontModel.leaveCriticalSection();
        }
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
    @Override
    public void addOntologyImport(String importIRI) {
        var hasOntologyLoaded = false;
        ontModel.enterCriticalSection(Lock.READ);
        try {
            if (ontModel.hasLoadedImport(importIRI)) {
                hasOntologyLoaded = true;
            }
        } finally {
            ontModel.leaveCriticalSection();
        }

        if (hasOntologyLoaded) {
            return;
        }

        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            var importResource = ontModel.createResource(importIRI);
            ontology.addImport(importResource);
            ontModel.loadImports();
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Check if the given iri is imported into the ontology model.
     *
     * @param importIri Iri that should be checked
     * @return True if imported, else False
     */
    @Override
    public boolean hasImport(String importIri) {
        Set<String> importedModels = Sets.mutable.empty();
        ontModel.enterCriticalSection(Lock.READ);
        try {
            importedModels = ontModel.listImportedOntologyURIs();
        } finally {
            ontModel.leaveCriticalSection();
        }

        return importedModels.contains(importIri);
    }

    /**
     * Returns the first {@link Ontology} that is not an imported ontology in the {@link OntModel}. This is assumed to
     * be the base ontology.
     *
     * @return the first {@link Ontology} that is not an imported ontology in the {@link OntModel}
     */
    protected Optional<Ontology> getBaseOntology() {
        Set<String> importedOntologies = Sets.mutable.empty();
        ontModel.enterCriticalSection(Lock.READ);
        try {
            importedOntologies = ontModel.listImportedOntologyURIs();
        } finally {
            ontModel.leaveCriticalSection();
        }
        for (var onto : ontModel.listOntologies().toSet()) {
            var ontologyUri = onto.getURI();
            if (!importedOntologies.contains(ontologyUri)) {
                return Optional.of(onto);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the {@link InfModel} for inference reasons.
     *
     * @return The InfModel for the ontology
     */
    private synchronized InfModel getInfModel() {
        if (infModel == null) {
            var reasoner = ReasonerRegistry.getOWLReasoner();
            infModel = ModelFactory.createInfModel(reasoner, ontModel);
        }
        return infModel;
    }

    /**
     * Creates the uri out of a given prefix and suffix by concatenating them and expanding the prefix.
     *
     * @param prefix prefix that should be used
     * @param suffix suffix that should be used
     * @return uri after expansion.
     */
    @Override
    public String createUri(String prefix, String suffix) {
        String encodedSuffix = suffix;
        try {
            encodedSuffix = URLEncoder.encode(suffix, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }

        ontModel.enterCriticalSection(Lock.READ);
        try {
            return ontModel.expandPrefix(prefix + ":" + encodedSuffix);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /***********/
    /* CLASSES */
    /***********/

    /**
     * Looks for an {@link OntClass} that contains the given name as either label or uses the name as (local) Iri.
     *
     * @param className Label or Iri of the wanted class
     * @return Optional containing the given class. Returns an empty optional, if the class could not be found
     */
    @Override
    public Optional<OntClass> getClass(String className) {
        // first look for usage of className as label
        ontModel.enterCriticalSection(Lock.READ);
        try {
            var stmts = ontModel.listStatements(null, RDFS.label, className, null);
            if (stmts.hasNext()) {
                var resource = stmts.next().getSubject();
                if (resource.canAs(OntClass.class)) {
                    return Optional.of(resource.as(OntClass.class));
                }
            }
        } finally {
            ontModel.leaveCriticalSection();
        }

        // if the className was not a label, looks for usage of the className in the Iris
        Set<String> prefixes = Sets.mutable.empty();
        ontModel.enterCriticalSection(Lock.READ);
        try {
            prefixes = ontModel.getNsPrefixMap().keySet();
        } finally {
            ontModel.leaveCriticalSection();
        }
        for (var prefix : prefixes) {
            var uri = createUri(prefix, className);
            var optClass = getClassByIri(uri);
            if (optClass.isPresent()) {
                return optClass;
            }
        }
        return Optional.empty();
    }

    /**
     * Looks for a class with the given name and the given prefix. The name can be either a local iri or a given label.
     * If it is a label, this method will only return classes that can be found within the namespace of the given prefix
     *
     * @param className label or iri of the class that should be returned
     * @param prefix    Prefix of the namespace the class should be contained in.
     * @return {@link Optional} containing the wanted class if it was found. Else, returns an empty {@link Optional}
     */
    @Override
    public Optional<OntClass> getClass(String className, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            prefix = DEFAULT_PREFIX;
        }
        String prefixUri = null;
        ontModel.enterCriticalSection(Lock.READ);
        try {
            prefixUri = ontModel.getNsPrefixURI(prefix);
        } finally {
            ontModel.leaveCriticalSection();
        }
        if (prefixUri == null) {
            return Optional.empty();
        }

        var uri = createUri(prefix, className);
        var clazz = getClassByIri(uri);
        if (clazz.isPresent()) {
            return clazz;
        }

        ontModel.enterCriticalSection(Lock.READ);
        try {
            var stmts = ontModel.listStatements(null, RDFS.label, className, null);
            while (stmts.hasNext()) {
                var resource = stmts.next().getSubject();
                var namespace = resource.getNameSpace();
                if (prefixUri.equals(namespace)) {
                    return Optional.ofNullable(ontModel.createClass(resource.getURI()));
                }
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
        return Optional.empty();
    }

    /**
     * Returns an {@link Optional} containing the {@link OntClass} that corresponds to the given uri. The uri might be
     * in prefix-notation (e.g., "owl:Thing"). If no class is found, the returned {@link Optional} will be empty
     *
     * @param iri the iri of the class (can be prefix notation or simple uri notation)
     * @return {@link Optional} containing the {@link OntClass} that corresponds to the given iri. Empty Optional if no
     *         class exists.
     */
    @Override
    public Optional<OntClass> getClassByIri(String iri) {
        ontModel.enterCriticalSection(Lock.READ);
        try {
            var expandedUri = ontModel.expandPrefix(iri);
            return Optional.ofNullable(ontModel.getOntClass(expandedUri));
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Adds a class with the given name to the ontology. If the class exists already, returns the existing class.
     *
     * @param className Name of the class that should be created
     * @return created class
     */
    @Override
    public OntClass addClass(String className) {
        Optional<OntClass> clazzOpt = getClass(className);
        if (clazzOpt.isPresent()) {
            return clazzOpt.get();
        }

        var uri = generateRandomURI(DEFAULT_PREFIX);
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            var clazz = ontModel.createClass(uri);
            clazz.addProperty(RDFS.label, className);
            return clazz;
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Adds a class with the given Iri and returns this class. If a class with that Iri already exists, returns the
     * existing class.
     *
     * @param iri Iri of the class that should be added
     * @return {@link OntClass} with the given Iri
     */
    @Override
    public OntClass addClassByIri(String iri) {
        Optional<OntClass> clazz = getClassByIri(iri);
        if (clazz.isPresent()) {
            return clazz.get();
        }
        String uri = null;
        ontModel.enterCriticalSection(Lock.READ);
        try {
            uri = ontModel.expandPrefix(iri);
        } finally {
            ontModel.leaveCriticalSection();
        }

        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            return ontModel.createClass(uri);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Add superclass relation between the given arguments.
     *
     * @param subClass   class that should be subclass of the other given class/resource
     * @param superClass class/resource that should be superclass of the other given class
     */
    @Override
    public void addSuperClass(OntClass subClass, Resource superClass) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            subClass.addSuperClass(superClass);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Sets exclusive superclassing for the given sub-class to the given super-class. Overwrites previous super-classes.
     *
     * @param subClass   class that should be subclass of the other given class/resource
     * @param superClass class/resource that should be superclass of the other given class
     */
    @Override
    public void addSuperClassExclusive(OntClass subClass, Resource superClass) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            subClass.setSuperClass(superClass);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Add superclass relation between the given arguments.
     *
     * @param className      name of the class that should be subclass of the other given class/resource
     * @param superClassName name of the class that should be superclass of the other given class
     */
    @Override
    public OntClass addSuperClass(String className, String superClassName) {
        Optional<OntClass> superClassOpt = getClass(superClassName);
        if (superClassOpt.isPresent()) {
            OntClass superClass = superClassOpt.get();
            return addSubClass(className, superClass);
        } else {
            return addClass(className);
        }
    }

    /**
     * Add superclass relation between the given arguments.
     *
     * @param className  name of the class that should be subclass of the other given class/resource
     * @param superClass class that should be superclass of the other given class
     */
    @Override
    public OntClass addSubClass(String className, OntClass superClass) {
        OntClass clazz = addClass(className);
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            superClass.addSubClass(clazz);
        } finally {
            ontModel.leaveCriticalSection();
        }
        return clazz;
    }

    /**
     * Add superclass relation between the given arguments.
     *
     * @param subClass   class that should be subclass of the other given class/resource
     * @param superClass class that should be superclass of the other given class
     */
    @Override
    public void addSubClass(OntClass subClass, OntClass superClass) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            superClass.addSubClass(subClass);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Checks whether there is a sub/super-class relation between the given classes (order is important!)
     *
     * @param clazz      sub-class
     * @param superClass super-class
     * @return True if sub-class and super-class are related correspondingly
     */
    @Override
    public boolean classIsSubClassOf(OntClass clazz, OntClass superClass) {
        ontModel.enterCriticalSection(Lock.READ);
        try {
            return clazz.hasSuperClass(superClass) && superClass.hasSubClass(clazz);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Removes sub/super-classing relation between given classes.
     *
     * @param clazz      previous sub-class
     * @param superClass previous super-class
     */
    @Override
    public void removeSubClassing(OntClass clazz, OntClass superClass) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            clazz.removeSuperClass(superClass);
            superClass.removeSubClass(clazz);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Returns whether there is a class in the ontology with the given name. See also: {@link #getClass(String)}.
     *
     * @param className class to look for
     * @return True if there is a class with the given name. Else, False
     */
    @Override
    public boolean containsClass(String className) {
        return getClass(className).isPresent();
    }

    /**
     * Returns whether there is a class in the ontology with the given name and the given prefix. See also:
     * {@link #getClass(String, String)}.
     *
     * @param className class to look for
     * @param prefix    prefix of the class
     * @return True if there is a class with the given name and prefix. Else, False
     */
    @Override
    public boolean containsClass(String className, String prefix) {
        return getClass(className, prefix).isPresent();
    }

    /***************/
    /* INDIVIDUALS */
    /***************/

    /**
     * See {@link Individual#hasOntClass(String)}.
     *
     * @param individual Individual to check
     * @param uri        Uri to check
     * @return True if this individual has the given class as one of its types.
     */
    @Override
    public boolean hasOntClass(Individual individual, String uri) {
        ontModel.enterCriticalSection(Lock.READ);
        try {
            return individual.hasOntClass(uri);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    @Override
    public boolean hasOntClass(Individual individual, String prefix, String localname) {
        if (prefix == null || prefix.isBlank()) {
            prefix = DEFAULT_PREFIX;
        }
        var uri = createUri(prefix, localname);
        ontModel.enterCriticalSection(Lock.READ);
        try {
            return individual.hasOntClass(uri);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Returns an {@link Optional} that contains a named individual with the given name. If no individual with that name
     * exists, returns empty {@link Optional}.
     *
     * @param name name of the individual
     * @return Optional with the individual if it exists. Otherwise, empty Optional.
     */
    @Override
    public Optional<Individual> getIndividual(String name) {
        // first look for usage of name as label
        ontModel.enterCriticalSection(Lock.READ);
        try {
            var optIndividual = getIndividualWithStatement(name);
            if (optIndividual.isPresent()) {
                return optIndividual;
            }
        } finally {
            ontModel.leaveCriticalSection();
        }

        // if the name was not in a label, looks for usage of the name in the Iris
        Set<String> prefixes = Sets.mutable.empty();
        ontModel.enterCriticalSection(Lock.READ);
        try {
            prefixes = ontModel.getNsPrefixMap().keySet();
        } finally {
            ontModel.leaveCriticalSection();
        }
        for (var prefix : prefixes) {
            var uri = createUri(prefix, name);
            var optIndividual = getIndividualByIri(uri);
            if (optIndividual.isPresent()) {
                return optIndividual;
            }
        }
        return Optional.empty();
    }

    private Optional<Individual> getIndividualWithStatement(String name) {
        var stmts = ontModel.listStatements(null, RDFS.label, name, null);
        if (stmts.hasNext()) {
            var resource = stmts.next().getSubject();
            if (resource.canAs(Individual.class)) {
                return Optional.of(resource.as(Individual.class));
            }
        }
        return Optional.empty();
    }

    /**
     * Returns an {@link Optional} that contains a named individual with the given uri. If no individual with that uri
     * exists, returns empty {@link Optional}.
     *
     * @param iri iri of the individual
     * @return Optional with the individual if it exists. Otherwise, empty Optional.
     */
    @Override
    public Optional<Individual> getIndividualByIri(String iri) {
        ontModel.enterCriticalSection(Lock.READ);
        try {
            var uri = ontModel.expandPrefix(iri);
            return Optional.ofNullable(ontModel.getIndividual(uri));
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Returns the Individuals that have a class that corresponds to the given class name
     *
     * @param className Name of the class
     * @return List of Individuals for the given class (name)
     */
    @Override
    public List<Individual> getIndividualsOfClass(String className) {
        Optional<OntClass> optClass = getClass(className);
        if (!optClass.isPresent()) {
            return Lists.mutable.empty();
        }
        OntClass clazz = optClass.get();
        return getIndividualsOfClass(clazz);
    }

    /**
     * Returns List of individuals of the given class.
     *
     * @param clazz Class of the individuals that should be returned
     * @return List of individuals with the given class.
     */
    @Override
    public List<Individual> getIndividualsOfClass(OntClass clazz) {
        ontModel.enterCriticalSection(Lock.READ);
        try {
            return ontModel.listIndividuals(clazz).toList();
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Similar to {@link #getIndividualsOfClass(String)}, but also checks for inferred instances.
     *
     * @param className name of the class to retrieve individuals from
     * @return List of Individuals for the given class (name), including inferred ones
     */
    @Override
    public ImmutableList<Individual> getInferredIndividualsOfClass(String className) {
        Optional<OntClass> optClass = getClass(className);
        if (!optClass.isPresent()) {
            return Lists.immutable.empty();
        }
        OntClass clazz = optClass.get();

        StmtIterator stmts = null;
        ontModel.enterCriticalSection(Lock.READ);
        try {
            stmts = getInfModel().listStatements(null, RDF.type, clazz);
        } finally {
            ontModel.leaveCriticalSection();
        }
        return createImmutableIndividualListFromStatementIterator(stmts);
    }

    private ImmutableList<Individual> createImmutableIndividualListFromStatementIterator(StmtIterator stmts) {
        if (stmts == null) {
            return Lists.immutable.empty();
        }

        MutableList<Individual> individuals = Lists.mutable.empty();
        ontModel.enterCriticalSection(Lock.READ);
        try {
            while (stmts.hasNext()) {
                var stmt = stmts.nextStatement();
                var res = stmt.getSubject();
                if (res.canAs(Individual.class)) {
                    individuals.add(res.as(Individual.class));
                }
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
        return individuals.toImmutable();
    }

    /**
     * Adds an individual with the given name to the default (prefix) namespace.
     *
     * @param name Name of the individual
     * @return the Individual with the given name
     */
    @Override
    public Individual addIndividual(String name) {
        var uri = generateRandomURI(DEFAULT_PREFIX);

        Individual individual = null;
        ontModel.enterCriticalSection(Lock.READ);
        try {
            individual = ontModel.getIndividual(uri);
        } finally {
            ontModel.leaveCriticalSection();
        }

        if (individual == null) {
            ontModel.enterCriticalSection(Lock.WRITE);
            try {
                individual = ontModel.createIndividual(uri, OWL.Thing);
                individual.addLabel(name, null);
            } finally {
                ontModel.leaveCriticalSection();
            }
        }

        return individual;
    }

    /**
     * Removes an individual with the given name in the default (prefix) namespace from the ontology.
     *
     * @param name Name of the individual
     */
    @Override
    public void removeIndividual(String name) {
        var optIndividual = getIndividual(name);
        if (optIndividual.isPresent()) {
            ontModel.enterCriticalSection(Lock.WRITE);
            try {
                optIndividual.get().remove();
            } finally {
                ontModel.leaveCriticalSection();
            }
        }
    }

    /**
     * Removes an individual with the given uri from the ontology.
     *
     * @param name Name of the individual
     */
    @Override
    public void removeIndividualByUri(String uri) {
        var optIndividual = getIndividualByIri(uri);
        if (optIndividual.isPresent()) {
            ontModel.enterCriticalSection(Lock.WRITE);
            try {
                optIndividual.get().remove();
            } finally {
                ontModel.leaveCriticalSection();
            }
        }
    }

    /**
     * Removes a given individual from the ontology.
     *
     * @param individual the individual
     */
    @Override
    public void removeIndividual(Individual individual) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            individual.remove();
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Adds an Individual to the given class. If the Individual does not exist, creates the individual as well.
     *
     * @param name  name of the individual that should be added
     * @param clazz Class the individual should be added to
     * @return the individual corresponding to the name. If it did not exist before, it is the newly created individual
     */
    @Override
    public Individual addIndividualToClass(String name, OntClass clazz) {
        var individual = addIndividual(name);

        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            individual.addOntClass(clazz);
            individual.removeOntClass(OWL.Thing);
        } finally {
            ontModel.leaveCriticalSection();
        }

        return individual;
    }

    /**
     * Sets the class of an Individual. If the Individual does not exist, creates the individual as well.
     *
     * @param name  name of the individual that should be added
     * @param clazz Class the individual should be exclusively added to
     * @return the individual corresponding to the name. If it did not exist before, it is the newly created individual
     */
    @Override
    public Individual setIndividualClass(String name, OntClass clazz) {
        var individual = addIndividual(name);

        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            individual.setOntClass(clazz);
        } finally {
            ontModel.leaveCriticalSection();
        }

        return individual;
    }

    /*********/
    /* LISTS */
    /*********/

    /**
     * Adds an empty list. If a list with the provided label exists, then clears the list and returns it.
     *
     * @param label Label of the list that should be added
     * @return List that is empty having the specified label. Overwrites/deletes preexisting lists with same label.
     */
    @Override
    public OrderedOntologyList addEmptyList(String label) {
        listFactory.checkListImport();
        var list = listFactory.createFromLabel(label);
        list.clear();
        return list;
    }

    /**
     * Similar to {@link #addEmptyList(String)} but also adds the provided members. If a list with the provided label
     * exists, then clears the list and adds the provided members.
     *
     * @param label   Label of the list that should be added
     * @param members Individuals that should be added
     * @return List that contains the provided members and that has the specified label. Overwrites/deletes preexisting
     *         lists with same label.
     */
    @Override
    public OrderedOntologyList addList(String label, List<Individual> members) {
        listFactory.checkListImport();
        var list = addEmptyList(label);
        list.addAll(members);
        return list;
    }

    /**
     * Returns an {@link Optional} that contains a {@link OrderedOntologyList} if a list with the specified name/label
     * exists
     *
     * @param name Name/label of the list
     * @return Optional containing the list. Empty Optional, if no list with that name was found.
     */
    @Override
    public Optional<OrderedOntologyList> getList(String name) {
        listFactory.checkListImport();
        var individualOpt = getIndividual(name);
        if (individualOpt.isEmpty()) {
            return Optional.empty();
        }
        return listFactory.getOrderedListOntologyFromIndividual(individualOpt.get());
    }

    /**
     * Returns an {@link Optional} that contains a {@link OrderedOntologyList} if a list with the specified uri exists.
     *
     * @param uri Uri/Iri of the List
     * @return Optional containing the list with that uri. Empty Optional, if no list with that uri was found.
     */
    @Override
    public Optional<OrderedOntologyList> getListByIri(String uri) {
        listFactory.checkListImport();
        var listIndividualOpt = getIndividualByIri(uri);
        if (listIndividualOpt.isPresent()) {
            return listFactory.getOrderedListOntologyFromIndividual(listIndividualOpt.get());
        }
        return Optional.empty();
    }

    /**
     * Transforms the given individual into an {@link OrderedOntologyList}. If the given individual has an invalid class
     * or the list cannot be created for any other reason, returns an empty {@link Optional}.
     *
     * @param individual Individual that should be transformed into an {@link OrderedOntologyList}
     * @return Optional containing the list; empty Optional in case anything went wrong.
     */
    @Override
    public Optional<OrderedOntologyList> transformIntoOrderedOntologyList(Individual individual) {
        return listFactory.getOrderedListOntologyFromIndividual(individual);
    }

    /**************/
    /* PROPERTIES */
    /**************/

    /**
     * Returns an {@link Optional} containing a property with the given name. If no property is found, the
     * {@link Optional} is empty.
     *
     * @param propertyName name of the property to be returned
     * @return {@link Optional} containing a property with the given name. If no such property is found, the
     *         {@link Optional} is empty.
     */
    @Override
    public Optional<OntProperty> getProperty(String propertyName) {
        ontModel.enterCriticalSection(Lock.READ);
        try {
            var stmts = ontModel.listStatements(null, RDFS.label, propertyName, null);
            if (stmts.hasNext()) {
                var resource = stmts.next().getSubject();
                if (resource.canAs(OntProperty.class)) {
                    return Optional.of(resource.as(OntProperty.class));
                }
            }

        } finally {
            ontModel.leaveCriticalSection();
        }

        Set<String> prefixes = Sets.mutable.empty();
        ontModel.enterCriticalSection(Lock.READ);
        try {
            prefixes = ontModel.getNsPrefixMap().keySet();
        } finally {
            ontModel.leaveCriticalSection();
        }

        for (var prefix : prefixes) {
            var optProperty = getProperty(propertyName, prefix);
            if (optProperty.isPresent()) {
                return optProperty;
            }
        }
        return Optional.empty();
    }

    /**
     * Same as {@link #getProperty(String)}, but the property has to be within the given prefix-namespace.
     *
     * @param propertyName name of the property to be returned
     * @param prefix       Prefix (namespace) the property should be in
     * @return {@link Optional} containing a property with the given name and prefix. If no such property is found, the
     *         {@link Optional} is empty.
     */
    @Override
    public Optional<OntProperty> getProperty(String propertyName, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            prefix = DEFAULT_PREFIX;
        }
        var uri = createUri(prefix, propertyName);
        return getPropertyByIri(uri);
    }

    /**
     * Returns an {@link Optional} containing a property with the given uri. If no such property is found, the
     * {@link Optional} is empty.
     *
     * @param propertyIri Iri of the property
     * @return {@link Optional} containing a property with the given iri. If no such property is found, the
     *         {@link Optional} is empty.
     */
    @Override
    public Optional<OntProperty> getPropertyByIri(String propertyIri) {
        ontModel.enterCriticalSection(Lock.READ);
        try {
            var expandedUri = ontModel.expandPrefix(propertyIri);
            var property = ontModel.getOntProperty(expandedUri);
            return Optional.ofNullable(property);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Same as {@link #getProperty(String)} but returns a (typed) {@link DatatypeProperty}.
     *
     * @param dataPropertyName name of the property to be returned
     * @return {@link Optional} containing a {@link DatatypeProperty} with the given name. If no such property is found,
     *         the {@link Optional} is empty.
     */
    @Override
    public Optional<DatatypeProperty> getDataProperty(String dataPropertyName) {
        var propertyOpt = getProperty(dataPropertyName);
        return checkOptionalAndTransformIntoType(propertyOpt, DatatypeProperty.class);
    }

    /**
     * Same as {@link #getProperty(String)} but returns a (typed) {@link ObjectProperty}.
     *
     * @param dataPropertyName name of the property to be returned
     * @return {@link Optional} containing a {@link ObjectProperty} with the given name. If no such property is found,
     *         the {@link Optional} is empty.
     */
    @Override
    public Optional<ObjectProperty> getObjectProperty(String objectPropertyName) {
        var propertyOpt = getProperty(objectPropertyName);
        return checkOptionalAndTransformIntoType(propertyOpt, ObjectProperty.class);
    }

    /**
     * Same as {@link #getProperty(String)} but returns a (typed) {@link AnnotationProperty}.
     *
     * @param dataPropertyName name of the property to be returned
     * @return {@link Optional} containing a {@link AnnotationProperty} with the given name. If no such property is
     *         found, the {@link Optional} is empty.
     */
    @Override
    public Optional<AnnotationProperty> getAnnotationProperty(String annotationPropertyName) {
        var propertyOpt = getProperty(annotationPropertyName);
        return checkOptionalAndTransformIntoType(propertyOpt, AnnotationProperty.class);
    }

    /**
     * Adds a {@link OntProperty} and returns the created {@link OntProperty}. If a {@link OntProperty} with that URI
     * already existed, returns that one
     *
     * @param name Name of the property
     * @return the created or pre-existing OntProperty
     */
    @Override
    public OntProperty addProperty(String name) {
        String uri = createUri(DEFAULT_PREFIX, name);

        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            return ontModel.createOntProperty(uri);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Adds a {@link DatatypeProperty} and returns the created {@link DatatypeProperty}. If a {@link DatatypeProperty}
     * with that URI already existed, returns that one
     *
     * @param name Name of the property
     * @return the created or pre-existing DatatypeProperty
     */
    @Override
    public DatatypeProperty addDataProperty(String name) {
        String uri = createUri(DEFAULT_PREFIX, name);
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            return ontModel.createDatatypeProperty(uri);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Adds a {@link ObjectProperty} and returns the created {@link ObjectProperty}. If a {@link ObjectProperty} with
     * that URI already existed, returns that one
     *
     * @param name Name of the property
     * @return the created or pre-existing ObjectProperty
     */
    @Override
    public ObjectProperty addObjectProperty(String name) {
        String uri = createUri(DEFAULT_PREFIX, name);
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            return ontModel.createObjectProperty(uri);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Adds a Property with a value to a given Individual.
     *
     * @param individual Individual the property should be added to
     * @param property   Property that should be added
     * @param value      Value that should be set for that property
     */
    @Override
    public Resource addPropertyToIndividual(Individual individual, OntProperty property, String value) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            return individual.addProperty(property, value);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Sets a Property with a value to a given Individual.
     *
     * @param individual Individual the property should be set
     * @param property   Property that should be added
     * @param value      Value that should be set for that property
     */
    @Override
    public void setPropertyToIndividual(Individual individual, OntProperty property, String value) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            individual.setPropertyValue(property, ontModel.createLiteral(value));
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Adds a Property with a value to a given Individual.
     *
     * @param individual Individual the property should be added to
     * @param property   Property that should be added
     * @param value      Value that should be set for that property
     * @param language   language of the property value
     */
    @Override
    public Resource addPropertyToIndividual(Individual individual, OntProperty property, String value, String language) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            return individual.addProperty(property, value, language);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Sets a Property with a value to a given Individual.
     *
     * @param individual Individual the property should be set
     * @param property   Property that should be added
     * @param value      Value that should be set for that property
     * @param language   language of the property value
     */
    @Override
    public void setPropertyToIndividual(Individual individual, OntProperty property, String value, String language) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            individual.addProperty(property, value, language);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Adds a Property with a value to a given Individual.
     *
     * @param individual Individual the property should be added to
     * @param property   Property that should be added
     * @param value      Value that should be set for that property
     */
    @Override
    public Resource addPropertyToIndividual(Individual individual, OntProperty property, RDFNode value) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            return individual.addProperty(property, value);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Sets a Property with a value to a given Individual.
     *
     * @param individual Individual the property should be set
     * @param property   Property that should be added
     * @param value      Value that should be set for that property
     */
    @Override
    public void setPropertyToIndividual(Individual individual, OntProperty property, RDFNode value) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            individual.setPropertyValue(property, value);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Adds a Property with a value to a given Individual.
     *
     * @param individual Individual the property should be added to
     * @param property   Property that should be added
     * @param value      Value that should be set for that property
     */
    @Override
    public Resource addPropertyToIndividual(Individual individual, OntProperty property, int value) {
        return addPropertyToIndividual(individual, property, value, XSD.integer.toString());
    }

    /**
     * Sets a Property with a value to a given Individual.
     *
     * @param individual Individual the property should be set
     * @param property   Property that should be added
     * @param value      Value that should be set for that property
     */
    @Override
    public void setPropertyToIndividual(Individual individual, OntProperty property, int value) {
        addPropertyToIndividual(individual, property, value, XSD.integer.toString());
    }

    /**
     * Adds a Property with a value to a given Individual.
     *
     * @param individual Individual the property should be added to
     * @param property   Property that should be added
     * @param value      Value that should be set for that property
     * @param type       Type of the value
     */
    @Override
    public Resource addPropertyToIndividual(Individual individual, OntProperty property, Object value, String type) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            var valueLiteral = ontModel.createTypedLiteral(value, type);
            return individual.addProperty(property, valueLiteral);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Sets a Property with a value to a given Individual.
     *
     * @param individual Individual the property should be set
     * @param property   Property that should be added
     * @param value      Value that should be set for that property
     * @param type       Type of the value
     */
    @Override
    public void setPropertyToIndividual(Individual individual, OntProperty property, Object value, String type) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            var valueLiteral = ontModel.createTypedLiteral(value, type);
            individual.setPropertyValue(property, valueLiteral);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Returns a {@link RDFNode} that represents the value of the given {@link OntProperty} for the given
     * {@link Individual}. The {@link RDFNode} can be used to probe and transform into a corresponding (expected)
     * value-type.
     *
     * @param individual Individual that has the property
     * @param property   Property of which the value should be retrieved
     * @return Value of the given Property for the given Individual
     */
    @Override
    public RDFNode getPropertyValue(Individual individual, OntProperty property) {
        ontModel.enterCriticalSection(Lock.READ);
        try {
            return individual.getPropertyValue(property);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Returns an {@link Optional} that contains the value of the given {@link OntProperty} for the given
     * {@link Individual}. The returned {@link Optional} is empty, if the there could not be returned a string for the
     * given property.
     *
     * @param individual Individual that has the property
     * @param property   Property of which the value should be retrieved
     * @return {@link Optional} containing the String value. Empty, if no String value could be retrieved
     */
    @Override
    public Optional<String> getPropertyStringValue(Individual individual, OntProperty property) {
        var node = getPropertyValue(individual, property);

        ontModel.enterCriticalSection(Lock.READ);
        try {
            if (node.canAs(Literal.class)) {
                var literal = node.asLiteral();
                String literalString;
                try {
                    literalString = literal.getString();
                } catch (NotLiteral e) {
                    literalString = null;
                }
                return Optional.ofNullable(literalString);
            }
        } finally {
            ontModel.leaveCriticalSection();
        }

        return Optional.empty();
    }

    /**
     * Returns an {@link Optional} that contains the value of the given {@link OntProperty} for the given
     * {@link Individual}. The returned {@link Optional} is empty, if the there could not be returned a string for the
     * given property.
     *
     * @param individual Individual that has the property
     * @param property   Property of which the value should be retrieved
     * @return {@link Optional} containing the Integer value. Empty, if no Integer value could be retrieved
     */
    @Override
    public Optional<Integer> getPropertyIntValue(Individual individual, OntProperty property) {
        var node = getPropertyValue(individual, property);

        ontModel.enterCriticalSection(Lock.READ);
        try {
            if (node.canAs(Literal.class)) {
                var literal = node.asLiteral();
                try {
                    return Optional.of(literal.getInt());
                } catch (NotLiteral e) {
                    return Optional.empty();
                }

            }
        } finally {
            ontModel.leaveCriticalSection();
        }

        return Optional.empty();
    }

    /**
     * Removes all statements that have the given resource and property.
     *
     * @param resource Resource
     * @param property property
     */
    @Override
    public void removeAllOfProperty(Resource resource, OntProperty property) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            resource.removeAll(property);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * List statements that have the given {@link OntProperty} as property and the given {@link RDFNode} as object.
     * Returns the first non-null subject of the found statements.
     *
     * @param property Property used to look for
     * @param object   object that should be contained
     * @return Optional containing the first non-null subject. Empty Optional, if none is found
     */
    @Override
    public Optional<Resource> getFirstSubjectOf(OntProperty property, RDFNode object) {
        StmtIterator stmtIterator = null;
        ontModel.enterCriticalSection(Lock.READ);
        try {
            stmtIterator = ontModel.listStatements(null, property, object);
        } finally {
            ontModel.leaveCriticalSection();
        }

        while (stmtIterator != null && stmtIterator.hasNext()) {
            Resource subject = null;

            ontModel.enterCriticalSection(Lock.READ);
            try {
                var stmt = stmtIterator.next();
                subject = stmt.getSubject();
            } finally {
                ontModel.leaveCriticalSection();
            }

            if (subject != null) {
                return Optional.of(subject);
            }
        }
        return Optional.empty();
    }

    /**
     * List statements that have the given {@link OntProperty} as property and the given {@link RDFNode} as object.
     * Returns the extracted subjects of the statements.
     *
     * @param property Property used to look for
     * @param object   object that should be contained
     * @return List of extracted subjects
     */
    @Override
    public List<Resource> getSubjectsOf(OntProperty property, OntResource object) {
        StmtIterator stmtIterator = null;

        ontModel.enterCriticalSection(Lock.READ);
        try {
            stmtIterator = ontModel.listStatements(null, property, object);
        } finally {
            ontModel.leaveCriticalSection();
        }

        var resList = new ArrayList<Resource>();
        while (stmtIterator != null && stmtIterator.hasNext()) {
            Resource subject = null;

            ontModel.enterCriticalSection(Lock.READ);
            try {
                var stmt = stmtIterator.next();
                subject = stmt.getSubject();
            } finally {
                ontModel.leaveCriticalSection();
            }

            if (subject != null) {
                resList.add(subject);
            }
        }
        return resList;
    }

    /**
     * List statements that have the given {@link OntProperty} as property and the given {@link OntResource} as subject.
     * Returns the first non-null subject of the found statements.
     *
     * @param subject  Subject that should be contained
     * @param property Property used to look for
     * @return Optional containing the first non-null object. Empty Optional, if none is found
     */
    @Override
    public Optional<RDFNode> getFirstObjectOf(OntResource subject, OntProperty property) {
        StmtIterator stmtIterator = null;

        ontModel.enterCriticalSection(Lock.READ);
        try {
            stmtIterator = ontModel.listStatements(subject, property, (RDFNode) null);
        } finally {
            ontModel.leaveCriticalSection();
        }

        while (stmtIterator != null && stmtIterator.hasNext()) {
            RDFNode object = null;

            ontModel.enterCriticalSection(Lock.READ);
            try {
                var stmt = stmtIterator.next();
                object = stmt.getObject();
            } finally {
                ontModel.leaveCriticalSection();
            }
            if (object != null) {
                return Optional.of(object);
            }
        }
        return Optional.empty();
    }

    /**
     * List statements that have the given {@link OntProperty} as property and the given {@link OntResource} as subject.
     * Returns the extracted objects of the statements.
     *
     * @param subject  Subject that should be contained
     * @param property Property used to look for
     * @return List of extracted objects
     */
    @Override
    public ImmutableList<RDFNode> getObjectsOf(OntResource subject, OntProperty property) {
        StmtIterator stmtIterator = null;

        ontModel.enterCriticalSection(Lock.READ);
        try {
            stmtIterator = ontModel.listStatements(subject, property, (RDFNode) null);
        } finally {
            ontModel.leaveCriticalSection();
        }

        MutableList<RDFNode> resList = Lists.mutable.empty();
        while (stmtIterator != null && stmtIterator.hasNext()) {
            RDFNode object = null;

            ontModel.enterCriticalSection(Lock.READ);
            try {
                var stmt = stmtIterator.next();
                object = stmt.getObject();
            } finally {
                ontModel.leaveCriticalSection();
            }
            if (object != null) {
                resList.add(object);
            }
        }
        return resList.toImmutable();
    }

    /***********************/
    /* Convenience Methods */
    /***********************/

    /**
     * Transforms a given Node (that is a subtype of RDFNode) into the given target type. If it cannot be transformed,
     * returns an empty Optional.
     *
     * @param <S>        source type, must extend Resource
     * @param <T>        target type, must extend Resource
     * @param from       resource that should be transformed
     * @param targetType class of the target type
     * @return Optional containing the transformed resource. If transformation was unsuccessful, the Optional is empty.
     */
    @Override
    public <S extends RDFNode, T extends Resource> Optional<T> transformType(S from, Class<T> targetType) {
        return Optional.ofNullable(transformTypeNullable(from, targetType));
    }

    /**
     * Transforms a given Node (that is a subtype of RDFNode) into the given target type. If it cannot be transformed,
     * returns <code>null</code>.
     *
     * @param <S>        source type, must extend Resource
     * @param <T>        target type, must extend Resource
     * @param from       resource that should be transformed
     * @param targetType class of the target type
     * @return The transformed resource. If transformation was unsuccessful, returns null.
     */
    @Override
    public <S extends RDFNode, T extends Resource> T transformTypeNullable(S from, Class<T> targetType) {
        ontModel.enterCriticalSection(Lock.READ);
        try {
            if (from != null && from.canAs(targetType)) {
                return from.as(targetType);
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
        return null;
    }

    /**
     * Transforms a given Node (that is a subtype of RDFNode) into an {@link Individual}. If it cannot be transformed,
     * returns <code>null</code>.
     *
     * @param node Node that should be transformed
     * @return The transformed Individual. If transformation was unsuccessful, returns null.
     */
    @Override
    public Optional<Individual> transformIntoIndividual(RDFNode node) {
        return transformType(node, Individual.class);
    }

    private <T extends Resource, S extends Resource> Optional<S> checkOptionalAndTransformIntoType(Optional<T> optional, Class<S> classType) {
        if (optional.isPresent()) {
            var resource = optional.get();
            return transformType(resource, classType);
        }
        return Optional.empty();
    }

    /**
     * See {@link OntResource#getLocalName()}
     *
     * @param resource Resource
     * @return The localname of this property within its namespace.
     */
    @Override
    public String getLocalName(OntResource resource) {
        ontModel.enterCriticalSection(Lock.READ);
        try {
            return resource.getLocalName();
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Returns a label for the given resource. For more details, see {@link OntResource#getLabel(String)}. The provided
     * language is set to <code>null</code>
     *
     * @param resource the resource
     * @return a label for the given resource or null if none is found
     */
    @Override
    public String getLabel(OntResource resource) {
        return getLabel(resource, null);
    }

    /**
     * See {@link OntResource#getLabel(String)}
     *
     * @param resource the resource
     * @param lang     the language attribute
     * @return a label for the given resource or null if none is found
     */
    @Override
    public String getLabel(OntResource resource, String lang) {
        ontModel.enterCriticalSection(Lock.READ);
        try {
            return resource.getLabel(lang);
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    /**
     * Generates a random URI using the default prefix
     *
     * @return random URI
     */
    @Override
    public String generateRandomURI() {
        return generateRandomURI(DEFAULT_PREFIX);
    }

    /**
     * Generates a random URI using the given prefix.
     *
     * @param prefix Prefix that should be used for namespace
     * @return random URI with the given prefix
     */
    @Override
    public String generateRandomURI(String prefix) {
        return createUri(prefix, OntologyUtil.generateRandomID());
    }

}
