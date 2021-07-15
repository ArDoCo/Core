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
import java.util.Random;

import org.apache.jena.graph.Node.NotLiteral;
import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
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
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

public class OntologyConnector {
    private static Logger logger = LogManager.getLogger(OntologyConnector.class);
    private static OntModelSpec modelSpec = OntModelSpec.OWL_DL_MEM;

    private static final String DEFAULT_PREFIX = "";
    private static final String LANG_EN = "en";

    private final OntModel ontModel;
    private OrderedOntologyList.Factory listFactory;

    private String pathToOntology;
    private Ontology ontology;
    private InfModel infModel;

    private static Random random = new Random();

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
    public static OntologyConnector createWithEmptyOntology(String defaultNameSpaceUri) {
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
     * Add a namespace prefix
     *
     * @param prefix the new prefix that should be able to use
     * @param uri    the URI that the prefix should be resolved to
     */
    public void addNsPrefix(String prefix, String uri) {
        ontModel.setNsPrefix(prefix, uri);
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
        if (ontModel.hasLoadedImport(importIRI)) {
            return;
        }
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

    private String createUri(String prefix, String suffix) {
        String encodedSuffix = suffix;
        try {
            encodedSuffix = URLEncoder.encode(suffix, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        return ontModel.expandPrefix(prefix + ":" + encodedSuffix);
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
    public Optional<OntClass> getClass(String className) {
        // first look for usage of className as label
        var stmts = ontModel.listStatements(null, RDFS.label, className, null);
        if (stmts.hasNext()) {
            var resource = stmts.next().getSubject();
            if (resource.canAs(OntClass.class)) {
                return Optional.of(resource.as(OntClass.class));
            }
        }

        // if the className was not a label, looks for usage of the className in the Iris
        var prefixes = ontModel.getNsPrefixMap().keySet();
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
    public Optional<OntClass> getClass(String className, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            prefix = DEFAULT_PREFIX;
        }
        var prefixUri = ontModel.getNsPrefixURI(prefix);
        if (prefixUri == null) {
            return Optional.empty();
        }

        var uri = createUri(prefix, className);
        var clazz = getClassByIri(uri);
        if (clazz.isPresent()) {
            return clazz;
        }
        var stmts = ontModel.listStatements(null, RDFS.label, className, null);
        while (stmts.hasNext()) {
            var resource = stmts.next().getSubject();
            var namespace = resource.getNameSpace();
            if (prefixUri.equals(namespace)) {
                return Optional.ofNullable(ontModel.createClass(resource.getURI()));
            }
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
    public Optional<OntClass> getClassByIri(String iri) {
        var expandedUri = ontModel.expandPrefix(iri);
        return Optional.ofNullable(ontModel.getOntClass(expandedUri));
    }

    /**
     * Adds a class with the given name to the ontology. If the class exists already, returns the existing class.
     *
     * @param className Name of the class that should be created
     * @return created class
     */
    public OntClass addClass(String className) {
        Optional<OntClass> clazzOpt = getClass(className);
        if (clazzOpt.isPresent()) {
            return clazzOpt.get();
        }

        var uri = generateRandomURI(DEFAULT_PREFIX);
        var clazz = ontModel.createClass(uri);
        clazz.addProperty(RDFS.label, className);
        return clazz;
    }

    /**
     * Adds a class with the given Iri and returns this class. If a class with that Iri already exists, returns the
     * existing class.
     *
     * @param iri Iri of the class that should be added
     * @return {@link OntClass} with the given Iri
     */
    public OntClass addClassByIri(String iri) {
        Optional<OntClass> clazz = getClassByIri(iri);
        if (clazz.isPresent()) {
            return clazz.get();
        }
        String uri = ontModel.expandPrefix(iri);
        return ontModel.createClass(uri);
    }

    /**
     * Add superclass relation between the given arguments.
     *
     * @param subClass   class that should be subclass of the other given class/resource
     * @param superClass class/resource that should be superclass of the other given class
     */
    public void addSuperClass(OntClass subClass, Resource superClass) {
        subClass.addSuperClass(superClass);
    }

    /**
     * Sets exclusive superclassing for the given sub-class to the given super-class. Overwrites previous super-classes.
     *
     * @param subClass   class that should be subclass of the other given class/resource
     * @param superClass class/resource that should be superclass of the other given class
     */
    public void addSuperClassExclusive(OntClass subClass, Resource superClass) {
        subClass.setSuperClass(superClass);
    }

    /**
     * Add superclass relation between the given arguments.
     *
     * @param className      name of the class that should be subclass of the other given class/resource
     * @param superClassName name of the class that should be superclass of the other given class
     */
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
    public OntClass addSubClass(String className, OntClass superClass) {
        OntClass clazz = addClass(className);
        superClass.addSubClass(clazz);
        return clazz;
    }

    /**
     * Add superclass relation between the given arguments.
     *
     * @param subClass   class that should be subclass of the other given class/resource
     * @param superClass class that should be superclass of the other given class
     */
    public void addSubClass(OntClass subClass, OntClass superClass) {
        superClass.addSubClass(subClass);
    }

    /**
     * Checks whether there is a sub/super-class relation between the given classes (order is important!)
     *
     * @param clazz      sub-class
     * @param superClass super-class
     * @return True if sub-class and super-class are related correspondingly
     */
    public boolean classIsSubClassOf(OntClass clazz, OntClass superClass) {
        return clazz.hasSuperClass(superClass) && superClass.hasSubClass(clazz);
    }

    /**
     * Removes sub/super-classing relation between given classes.
     *
     * @param clazz      previous sub-class
     * @param superClass previous super-class
     */
    public void removeSubClassing(OntClass clazz, OntClass superClass) {
        clazz.removeSuperClass(superClass);
        superClass.removeSubClass(clazz);
    }

    /**
     * Returns whether there is a class in the ontology with the given name. See also: {@link #getClass(String)}.
     *
     * @param className class to look for
     * @return True if there is a class with the given name. Else, False
     */
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
    public boolean containsClass(String className, String prefix) {
        return getClass(className, prefix).isPresent();
    }

    /***************/
    /* INDIVIDUALS */
    /***************/

    /**
     * Returns an {@link Optional} that contains a named individual with the given name. If no individual with that name
     * exists, returns empty {@link Optional}.
     *
     * @param name name of the individual
     * @return Optional with the individual if it exists. Otherwise, empty Optional.
     */
    public Optional<Individual> getIndividual(String name) {
        // first look for usage of name as label
        var stmts = ontModel.listStatements(null, RDFS.label, name, null);
        if (stmts.hasNext()) {
            var resource = stmts.next().getSubject();
            if (resource.canAs(Individual.class)) {
                return Optional.of(resource.as(Individual.class));
            }
        }

        // if the name was not in a label, looks for usage of the name in the Iris
        var prefixes = ontModel.getNsPrefixMap().keySet();
        for (var prefix : prefixes) {
            var uri = createUri(prefix, name);
            var optClass = getIndividualByIri(uri);
            if (optClass.isPresent()) {
                return optClass;
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
    public Optional<Individual> getIndividualByIri(String iri) {
        var uri = ontModel.expandPrefix(iri);
        return Optional.ofNullable(ontModel.getIndividual(uri));
    }

    /**
     * Returns the Individuals that have a class that corresponds to the given class name
     *
     * @param className Name of the class
     * @return List of Individuals for the given class (name)
     */
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
    public List<Individual> getIndividualsOfClass(OntClass clazz) {
        return ontModel.listIndividuals(clazz).toList();
    }

    /**
     * Similar to {@link #getIndividualsOfClass(String)}, but also checks for inferred instances.
     *
     * @param className name of the class to retrieve individuals from
     * @return List of Individuals for the given class (name), including inferred ones
     */
    public List<Individual> getInferredIndividualsOfClass(String className) {
        Optional<OntClass> optClass = getClass(className);
        if (!optClass.isPresent()) {
            return Lists.mutable.empty();
        }
        OntClass clazz = optClass.get();

        StmtIterator stmts = getInfModel().listStatements(null, RDF.type, clazz);
        return createMutableIndividualListFromStatementIterator(stmts);
    }

    private MutableList<Individual> createMutableIndividualListFromStatementIterator(StmtIterator stmts) {
        MutableList<Individual> individuals = Lists.mutable.empty();
        while (stmts.hasNext()) {
            var stmt = stmts.nextStatement();
            var res = stmt.getSubject();
            if (res.canAs(Individual.class)) {
                individuals.add(res.as(Individual.class));
            }
        }
        return individuals;
    }

    /**
     * Adds an individual with the given name to the default (prefix) namespace.
     *
     * @param name
     * @return
     */
    public Individual addIndividual(String name) {
        var uri = generateRandomURI(DEFAULT_PREFIX);
        var individual = ontModel.getIndividual(uri);
        if (individual == null) {
            individual = ontModel.createIndividual(uri, OWL.Thing);
            individual.addLabel(name, LANG_EN);
        }

        return individual;
    }

    /**
     * Adds an Individual to the given class. If the Individual does not exist, creates the individual as well.
     *
     * @param name  name of the individual that should be added
     * @param clazz Class the individual should be added to
     * @return the individual corresponding to the name. If it did not exist before, it is the newly created individual
     */
    public Individual addIndividualToClass(String name, OntClass clazz) {
        var individual = addIndividual(name);
        individual.addOntClass(clazz);
        individual.removeOntClass(OWL.Thing);
        return individual;
    }

    /**
     * Sets the class of an Individual. If the Individual does not exist, creates the individual as well.
     *
     * @param name  name of the individual that should be added
     * @param clazz Class the individual should be exclusively added to
     * @return the individual corresponding to the name. If it did not exist before, it is the newly created individual
     */
    public Individual setIndividualClass(String name, OntClass clazz) {
        var individual = addIndividual(name);
        individual.setOntClass(clazz);
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
    public Optional<OrderedOntologyList> getListByIri(String uri) {
        listFactory.checkListImport();
        var listIndividualOpt = getIndividualByIri(uri);
        if (listIndividualOpt.isPresent()) {
            return listFactory.getOrderedListOntologyFromIndividual(listIndividualOpt.get());
        }
        return Optional.empty();
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
    public Optional<OntProperty> getProperty(String propertyName) {
        var stmts = ontModel.listStatements(null, RDFS.label, propertyName, null);
        if (stmts.hasNext()) {
            var resource = stmts.next().getSubject();
            if (resource.canAs(OntProperty.class)) {
                return Optional.of(resource.as(OntProperty.class));
            }
        }

        var prefixes = ontModel.getNsPrefixMap().keySet();
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
    public Optional<OntProperty> getPropertyByIri(String propertyIri) {
        var expandedUri = ontModel.expandPrefix(propertyIri);
        var property = ontModel.getOntProperty(expandedUri);
        return Optional.ofNullable(property);
    }

    /**
     * Same as {@link #getProperty(String)} but returns a (typed) {@link DatatypeProperty}.
     *
     * @param dataPropertyName name of the property to be returned
     * @return {@link Optional} containing a {@link DatatypeProperty} with the given name. If no such property is found,
     *         the {@link Optional} is empty.
     */
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
    public OntProperty addProperty(String name) {
        String uri = createUri(DEFAULT_PREFIX, name);
        return ontModel.createOntProperty(uri);
    }

    /**
     * Adds a {@link DatatypeProperty} and returns the created {@link DatatypeProperty}. If a {@link DatatypeProperty}
     * with that URI already existed, returns that one
     *
     * @param name Name of the property
     * @return the created or pre-existing DatatypeProperty
     */
    public DatatypeProperty addDataProperty(String name) {
        String uri = createUri(DEFAULT_PREFIX, name);
        return ontModel.createDatatypeProperty(uri);
    }

    /**
     * Adds a {@link ObjectProperty} and returns the created {@link ObjectProperty}. If a {@link ObjectProperty} with
     * that URI already existed, returns that one
     *
     * @param name Name of the property
     * @return the created or pre-existing ObjectProperty
     */
    public ObjectProperty addObjectProperty(String name) {
        String uri = createUri(DEFAULT_PREFIX, name);
        return ontModel.createObjectProperty(uri);
    }

    /**
     * Adds a Property with a value to a given Individual.
     *
     * @param individual Individual the property should be added to
     * @param property   Property that should be added
     * @param value      Value that should be set for that property
     */
    public void addPropertyToIndividual(Individual individual, OntProperty property, String value) {
        individual.addProperty(property, value);
    }

    /**
     * Adds a Property with a value to a given Individual.
     *
     * @param individual Individual the property should be added to
     * @param property   Property that should be added
     * @param value      Value that should be set for that property
     * @param language   language of the property value
     */
    public void addPropertyToIndividual(Individual individual, OntProperty property, String value, String language) {
        individual.addProperty(property, value, language);
    }

    /**
     * Adds a Property with a value to a given Individual.
     *
     * @param individual Individual the property should be added to
     * @param property   Property that should be added
     * @param value      Value that should be set for that property
     */
    public void addPropertyToIndividual(Individual individual, OntProperty property, RDFNode value) {
        individual.addProperty(property, value);
    }

    /**
     * Adds a Property with a value to a given Individual.
     *
     * @param individual Individual the property should be added to
     * @param property   Property that should be added
     * @param value      Value that should be set for that property
     */
    public void addPropertyToIndividual(Individual individual, OntProperty property, int value) {
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
    public void addPropertyToIndividual(Individual individual, OntProperty property, Object value, String type) {
        var valueLiteral = ontModel.createTypedLiteral(value, type);
        individual.addProperty(property, valueLiteral);
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
    public RDFNode getPropertyValue(Individual individual, OntProperty property) {
        return individual.getPropertyValue(property);
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
    public Optional<String> getPropertyStringValue(Individual individual, OntProperty property) {
        var node = getPropertyValue(individual, property);
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
    public Optional<Integer> getPropertyIntValue(Individual individual, OntProperty property) {
        var node = getPropertyValue(individual, property);
        if (node.canAs(Literal.class)) {
            var literal = node.asLiteral();
            try {
                return Optional.of(literal.getInt());
            } catch (NotLiteral e) {
                return Optional.empty();
            }

        }
        return Optional.empty();
    }

    /***********************/
    /* Convenience Methods */
    /***********************/

    /**
     * Transforms a given Resource (that is a subtype of Resource) into the given target type. If it cannot be
     * transformed, returns en empty Optional.
     *
     * @param <S>        source type, must extend Resource
     * @param <T>        target type, must extend Resource
     * @param from       resource that should be transformed
     * @param targetType class of the target type
     * @return Optional containing the transformed resource. If transformation was unsuccessful, the Optional is empty.
     */
    public <S extends Resource, T extends Resource> Optional<T> transformType(S from, Class<T> targetType) {
        if (from.canAs(targetType)) {
            return Optional.of(from.as(targetType));
        } else {
            return Optional.empty();
        }
    }

    private <T extends Resource, S extends Resource> Optional<S> checkOptionalAndTransformIntoType(Optional<T> optional, Class<S> classType) {
        if (optional.isPresent()) {
            var resource = optional.get();
            return transformType(resource, classType);
        }
        return Optional.empty();
    }

    public String generateRandomURI() {
        return generateRandomURI(DEFAULT_PREFIX);
    }

    public String generateRandomURI(String prefix) {
        return createUri(prefix, generateRandomUUID());
    }

    public static String generateRandomUUID() {
        var leftLimit = 48; // numeral '0'
        var rightLimit = 122; // letter 'z'
        var targetStringLength = 10;

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

}
