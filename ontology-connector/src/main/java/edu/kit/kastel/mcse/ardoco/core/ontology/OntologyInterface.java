package edu.kit.kastel.mcse.ardoco.core.ontology;

import java.util.List;
import java.util.Optional;

import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.eclipse.collections.api.list.ImmutableList;

public interface OntologyInterface {

    /**
     * Validates the ontology. A logger will put out warnings iff there are conflicts.
     *
     * @return <code>true</code> if the ontology is valid and has no conflicts, <code>false</code> if there are
     *         conflicts
     */
    boolean validateOntology();

    /**
     * Adds/Sets a namespace prefix
     *
     * @param prefix the new prefix that should be able to use
     * @param uri    the URI that the prefix should be resolved to
     */
    void setNsPrefix(String prefix, String uri);

    /**
     * Save the ontology to a given file (path). This method uses the RDF/XML language.
     *
     * @param file String containing the path of the file the ontology should be saved to
     * @return true if saving was successful, otherwise false is returned
     */
    boolean save(String file);

    /**
     * Save the ontology to a given file (path). This method uses the N3 language to save.
     *
     * @param file     String containing the path of the file the ontology should be saved to
     * @param language The language the file should be written in
     * @return true if saving was successful, otherwise false is returned
     */
    boolean save(String file, Lang language);

    /**
     * Add an Ontology based on its IRI
     *
     * @param importIRI the IRI of the ontology that should be imported
     */
    void addOntologyImport(String importIRI);

    /**
     * Check if the given iri is imported into the ontology model.
     *
     * @param importIri Iri that should be checked
     * @return True if imported, else False
     */
    boolean hasImport(String importIri);

    /**
     * Creates the uri out of a given prefix and suffix by concatenating them and expanding the prefix.
     *
     * @param prefix prefix that should be used
     * @param suffix suffix that should be used
     * @return uri after expansion.
     */
    String createUri(String prefix, String suffix);

    /**
     * Looks for an {@link OntClass} that contains the given name as either label or uses the name as (local) Iri.
     *
     * @param className Label or Iri of the wanted class
     * @return Optional containing the given class. Returns an empty optional, if the class could not be found
     */
    Optional<OntClass> getClass(String className);

    /**
     * Looks for a class with the given name and the given prefix. The name can be either a local iri or a given label.
     * If it is a label, this method will only return classes that can be found within the namespace of the given prefix
     *
     * @param className label or iri of the class that should be returned
     * @param prefix    Prefix of the namespace the class should be contained in.
     * @return {@link Optional} containing the wanted class if it was found. Else, returns an empty {@link Optional}
     */
    Optional<OntClass> getClass(String className, String prefix);

    /**
     * Returns an {@link Optional} containing the {@link OntClass} that corresponds to the given uri. The uri might be
     * in prefix-notation (e.g., "owl:Thing"). If no class is found, the returned {@link Optional} will be empty
     *
     * @param iri the iri of the class (can be prefix notation or simple uri notation)
     * @return {@link Optional} containing the {@link OntClass} that corresponds to the given iri. Empty Optional if no
     *         class exists.
     */
    Optional<OntClass> getClassByIri(String iri);

    /**
     * Adds a class with the given name to the ontology. If the class exists already, returns the existing class.
     *
     * @param className Name of the class that should be created
     * @return created class
     */
    OntClass addClass(String className);

    /**
     * Adds a class with the given Iri and returns this class. If a class with that Iri already exists, returns the
     * existing class.
     *
     * @param iri Iri of the class that should be added
     * @return {@link OntClass} with the given Iri
     */
    OntClass addClassByIri(String iri);

    /**
     * Add superclass relation between the given arguments.
     *
     * @param subClass   class that should be subclass of the other given class/resource
     * @param superClass class/resource that should be superclass of the other given class
     */
    void addSuperClass(OntClass subClass, Resource superClass);

    /**
     * Sets exclusive superclassing for the given sub-class to the given super-class. Overwrites previous super-classes.
     *
     * @param subClass   class that should be subclass of the other given class/resource
     * @param superClass class/resource that should be superclass of the other given class
     */
    void addSuperClassExclusive(OntClass subClass, Resource superClass);

    /**
     * Add superclass relation between the given arguments.
     *
     * @param className      name of the class that should be subclass of the other given class/resource
     * @param superClassName name of the class that should be superclass of the other given class
     */
    OntClass addSuperClass(String className, String superClassName);

    /**
     * Add superclass relation between the given arguments.
     *
     * @param className  name of the class that should be subclass of the other given class/resource
     * @param superClass class that should be superclass of the other given class
     */
    OntClass addSubClass(String className, OntClass superClass);

    /**
     * Add superclass relation between the given arguments.
     *
     * @param subClass   class that should be subclass of the other given class/resource
     * @param superClass class that should be superclass of the other given class
     */
    void addSubClass(OntClass subClass, OntClass superClass);

    /**
     * Checks whether there is a sub/super-class relation between the given classes (order is important!)
     *
     * @param clazz      sub-class
     * @param superClass super-class
     * @return True if sub-class and super-class are related correspondingly
     */
    boolean classIsSubClassOf(OntClass clazz, OntClass superClass);

    /**
     * Removes sub/super-classing relation between given classes.
     *
     * @param clazz      previous sub-class
     * @param superClass previous super-class
     */
    void removeSubClassing(OntClass clazz, OntClass superClass);

    /**
     * Returns whether there is a class in the ontology with the given name. See also: {@link #getClass(String)}.
     *
     * @param className class to look for
     * @return True if there is a class with the given name. Else, False
     */
    boolean containsClass(String className);

    /**
     * Returns whether there is a class in the ontology with the given name and the given prefix. See also:
     * {@link #getClass(String, String)}.
     *
     * @param className class to look for
     * @param prefix    prefix of the class
     * @return True if there is a class with the given name and prefix. Else, False
     */
    boolean containsClass(String className, String prefix);

    /**
     * See {@link Individual#hasOntClass(String)}.
     *
     * @param individual Individual to check
     * @param uri        Uri to check
     * @return True if this individual has the given class as one of its types.
     */
    boolean hasOntClass(Individual individual, String uri);

    boolean hasOntClass(Individual individual, String prefix, String localname);

    /**
     * Returns an {@link Optional} that contains a named individual with the given name. If no individual with that name
     * exists, returns empty {@link Optional}.
     *
     * @param name name of the individual
     * @return Optional with the individual if it exists. Otherwise, empty Optional.
     */
    Optional<Individual> getIndividual(String name);

    /**
     * Returns an {@link Optional} that contains a named individual with the given uri. If no individual with that uri
     * exists, returns empty {@link Optional}.
     *
     * @param iri iri of the individual
     * @return Optional with the individual if it exists. Otherwise, empty Optional.
     */
    Optional<Individual> getIndividualByIri(String iri);

    /**
     * Returns the Individuals that have a class that corresponds to the given class name
     *
     * @param className Name of the class
     * @return List of Individuals for the given class (name)
     */
    List<Individual> getIndividualsOfClass(String className);

    /**
     * Returns List of individuals of the given class.
     *
     * @param clazz Class of the individuals that should be returned
     * @return List of individuals with the given class.
     */
    List<Individual> getIndividualsOfClass(OntClass clazz);

    /**
     * Similar to {@link #getIndividualsOfClass(String)}, but also checks for inferred instances.
     *
     * @param className name of the class to retrieve individuals from
     * @return List of Individuals for the given class (name), including inferred ones
     */
    ImmutableList<Individual> getInferredIndividualsOfClass(String className);

    /**
     * Adds an individual with the given name to the default (prefix) namespace.
     *
     * @param name Name of the individual
     * @return the Individual with the given name
     */
    Individual addIndividual(String name);

    /**
     * Removes an individual with the given name in the default (prefix) namespace from the ontology.
     *
     * @param name Name of the individual
     */
    void removeIndividual(String name);

    /**
     * Removes an individual with the given uri from the ontology.
     *
     * @param name Name of the individual
     */
    void removeIndividualByUri(String uri);

    /**
     * Removes a given individual from the ontology.
     *
     * @param individual the individual
     */
    void removeIndividual(Individual individual);

    /**
     * Adds an Individual to the given class. If the Individual does not exist, creates the individual as well.
     *
     * @param name  name of the individual that should be added
     * @param clazz Class the individual should be added to
     * @return the individual corresponding to the name. If it did not exist before, it is the newly created individual
     */
    Individual addIndividualToClass(String name, OntClass clazz);

    /**
     * Sets the class of an Individual. If the Individual does not exist, creates the individual as well.
     *
     * @param name  name of the individual that should be added
     * @param clazz Class the individual should be exclusively added to
     * @return the individual corresponding to the name. If it did not exist before, it is the newly created individual
     */
    Individual setIndividualClass(String name, OntClass clazz);

    /**
     * Adds an empty list. If a list with the provided label exists, then clears the list and returns it.
     *
     * @param label Label of the list that should be added
     * @return List that is empty having the specified label. Overwrites/deletes preexisting lists with same label.
     */
    OrderedOntologyList addEmptyList(String label);

    /**
     * Similar to {@link #addEmptyList(String)} but also adds the provided members. If a list with the provided label
     * exists, then clears the list and adds the provided members.
     *
     * @param label   Label of the list that should be added
     * @param members Individuals that should be added
     * @return List that contains the provided members and that has the specified label. Overwrites/deletes preexisting
     *         lists with same label.
     */
    OrderedOntologyList addList(String label, List<Individual> members);

    /**
     * Returns an {@link Optional} that contains a {@link OrderedOntologyList} if a list with the specified name/label
     * exists
     *
     * @param name Name/label of the list
     * @return Optional containing the list. Empty Optional, if no list with that name was found.
     */
    Optional<OrderedOntologyList> getList(String name);

    /**
     * Returns an {@link Optional} that contains a {@link OrderedOntologyList} if a list with the specified uri exists.
     *
     * @param uri Uri/Iri of the List
     * @return Optional containing the list with that uri. Empty Optional, if no list with that uri was found.
     */
    Optional<OrderedOntologyList> getListByIri(String uri);

    /**
     * Transforms the given individual into an {@link OrderedOntologyList}. If the given individual has an invalid class
     * or the list cannot be created for any other reason, returns an empty {@link Optional}.
     *
     * @param individual Individual that should be transformed into an {@link OrderedOntologyList}
     * @return Optional containing the list; empty Optional in case anything went wrong.
     */
    Optional<OrderedOntologyList> transformIntoOrderedOntologyList(Individual individual);

    /**
     * Returns an {@link Optional} containing a property with the given name. If no property is found, the
     * {@link Optional} is empty.
     *
     * @param propertyName name of the property to be returned
     * @return {@link Optional} containing a property with the given name. If no such property is found, the
     *         {@link Optional} is empty.
     */
    Optional<OntProperty> getProperty(String propertyName);

    /**
     * Same as {@link #getProperty(String)}, but the property has to be within the given prefix-namespace.
     *
     * @param propertyName name of the property to be returned
     * @param prefix       Prefix (namespace) the property should be in
     * @return {@link Optional} containing a property with the given name and prefix. If no such property is found, the
     *         {@link Optional} is empty.
     */
    Optional<OntProperty> getProperty(String propertyName, String prefix);

    /**
     * Returns an {@link Optional} containing a property with the given uri. If no such property is found, the
     * {@link Optional} is empty.
     *
     * @param propertyIri Iri of the property
     * @return {@link Optional} containing a property with the given iri. If no such property is found, the
     *         {@link Optional} is empty.
     */
    Optional<OntProperty> getPropertyByIri(String propertyIri);

    /**
     * Same as {@link #getProperty(String)} but returns a (typed) {@link DatatypeProperty}.
     *
     * @param dataPropertyName name of the property to be returned
     * @return {@link Optional} containing a {@link DatatypeProperty} with the given name. If no such property is found,
     *         the {@link Optional} is empty.
     */
    Optional<DatatypeProperty> getDataProperty(String dataPropertyName);

    /**
     * Same as {@link #getProperty(String)} but returns a (typed) {@link ObjectProperty}.
     *
     * @param dataPropertyName name of the property to be returned
     * @return {@link Optional} containing a {@link ObjectProperty} with the given name. If no such property is found,
     *         the {@link Optional} is empty.
     */
    Optional<ObjectProperty> getObjectProperty(String objectPropertyName);

    /**
     * Same as {@link #getProperty(String)} but returns a (typed) {@link AnnotationProperty}.
     *
     * @param dataPropertyName name of the property to be returned
     * @return {@link Optional} containing a {@link AnnotationProperty} with the given name. If no such property is
     *         found, the {@link Optional} is empty.
     */
    Optional<AnnotationProperty> getAnnotationProperty(String annotationPropertyName);

    /**
     * Adds a {@link OntProperty} and returns the created {@link OntProperty}. If a {@link OntProperty} with that URI
     * already existed, returns that one
     *
     * @param name Name of the property
     * @return the created or pre-existing OntProperty
     */
    OntProperty addProperty(String name);

    /**
     * Adds a {@link DatatypeProperty} and returns the created {@link DatatypeProperty}. If a {@link DatatypeProperty}
     * with that URI already existed, returns that one
     *
     * @param name Name of the property
     * @return the created or pre-existing DatatypeProperty
     */
    DatatypeProperty addDataProperty(String name);

    /**
     * Adds a {@link ObjectProperty} and returns the created {@link ObjectProperty}. If a {@link ObjectProperty} with
     * that URI already existed, returns that one
     *
     * @param name Name of the property
     * @return the created or pre-existing ObjectProperty
     */
    ObjectProperty addObjectProperty(String name);

    /**
     * Adds a Property with a value to a given Individual.
     *
     * @param individual Individual the property should be added to
     * @param property   Property that should be added
     * @param value      Value that should be set for that property
     */
    Resource addPropertyToIndividual(Individual individual, OntProperty property, String value);

    /**
     * Sets a Property with a value to a given Individual.
     *
     * @param individual Individual the property should be set
     * @param property   Property that should be added
     * @param value      Value that should be set for that property
     */
    void setPropertyToIndividual(Individual individual, OntProperty property, String value);

    /**
     * Adds a Property with a value to a given Individual.
     *
     * @param individual Individual the property should be added to
     * @param property   Property that should be added
     * @param value      Value that should be set for that property
     * @param language   language of the property value
     */
    Resource addPropertyToIndividual(Individual individual, OntProperty property, String value, String language);

    /**
     * Sets a Property with a value to a given Individual.
     *
     * @param individual Individual the property should be set
     * @param property   Property that should be added
     * @param value      Value that should be set for that property
     * @param language   language of the property value
     */
    void setPropertyToIndividual(Individual individual, OntProperty property, String value, String language);

    /**
     * Adds a Property with a value to a given Individual.
     *
     * @param individual Individual the property should be added to
     * @param property   Property that should be added
     * @param value      Value that should be set for that property
     */
    Resource addPropertyToIndividual(Individual individual, OntProperty property, RDFNode value);

    /**
     * Sets a Property with a value to a given Individual.
     *
     * @param individual Individual the property should be set
     * @param property   Property that should be added
     * @param value      Value that should be set for that property
     */
    void setPropertyToIndividual(Individual individual, OntProperty property, RDFNode value);

    /**
     * Adds a Property with a value to a given Individual.
     *
     * @param individual Individual the property should be added to
     * @param property   Property that should be added
     * @param value      Value that should be set for that property
     */
    Resource addPropertyToIndividual(Individual individual, OntProperty property, int value);

    /**
     * Sets a Property with a value to a given Individual.
     *
     * @param individual Individual the property should be set
     * @param property   Property that should be added
     * @param value      Value that should be set for that property
     */
    void setPropertyToIndividual(Individual individual, OntProperty property, int value);

    /**
     * Adds a Property with a value to a given Individual.
     *
     * @param individual Individual the property should be added to
     * @param property   Property that should be added
     * @param value      Value that should be set for that property
     * @param type       Type of the value
     */
    Resource addPropertyToIndividual(Individual individual, OntProperty property, Object value, String type);

    /**
     * Sets a Property with a value to a given Individual.
     *
     * @param individual Individual the property should be set
     * @param property   Property that should be added
     * @param value      Value that should be set for that property
     * @param type       Type of the value
     */
    void setPropertyToIndividual(Individual individual, OntProperty property, Object value, String type);

    /**
     * Returns a {@link RDFNode} that represents the value of the given {@link OntProperty} for the given
     * {@link Individual}. The {@link RDFNode} can be used to probe and transform into a corresponding (expected)
     * value-type.
     *
     * @param individual Individual that has the property
     * @param property   Property of which the value should be retrieved
     * @return Value of the given Property for the given Individual
     */
    RDFNode getPropertyValue(Individual individual, OntProperty property);

    /**
     * Returns an {@link Optional} that contains the value of the given {@link OntProperty} for the given
     * {@link Individual}. The returned {@link Optional} is empty, if the there could not be returned a string for the
     * given property.
     *
     * @param individual Individual that has the property
     * @param property   Property of which the value should be retrieved
     * @return {@link Optional} containing the String value. Empty, if no String value could be retrieved
     */
    Optional<String> getPropertyStringValue(Individual individual, OntProperty property);

    /**
     * Returns an {@link Optional} that contains the value of the given {@link OntProperty} for the given
     * {@link Individual}. The returned {@link Optional} is empty, if the there could not be returned a string for the
     * given property.
     *
     * @param individual Individual that has the property
     * @param property   Property of which the value should be retrieved
     * @return {@link Optional} containing the Integer value. Empty, if no Integer value could be retrieved
     */
    Optional<Integer> getPropertyIntValue(Individual individual, OntProperty property);

    /**
     * Removes all statements that have the given resource and property.
     *
     * @param resource Resource
     * @param property property
     */
    void removeAllOfProperty(Resource resource, OntProperty property);

    /**
     * List statements that have the given {@link OntProperty} as property and the given {@link RDFNode} as object.
     * Returns the first non-null subject of the found statements.
     *
     * @param property Property used to look for
     * @param object   object that should be contained
     * @return Optional containing the first non-null subject. Empty Optional, if none is found
     */
    Optional<Resource> getFirstSubjectOf(OntProperty property, RDFNode object);

    /**
     * List statements that have the given {@link OntProperty} as property and the given {@link RDFNode} as object.
     * Returns the extracted subjects of the statements.
     *
     * @param property Property used to look for
     * @param object   object that should be contained
     * @return List of extracted subjects
     */
    List<Resource> getSubjectsOf(OntProperty property, OntResource object);

    /**
     * List statements that have the given {@link OntProperty} as property and the given {@link OntResource} as subject.
     * Returns the first non-null subject of the found statements.
     *
     * @param subject  Subject that should be contained
     * @param property Property used to look for
     * @return Optional containing the first non-null object. Empty Optional, if none is found
     */
    Optional<RDFNode> getFirstObjectOf(OntResource subject, OntProperty property);

    /**
     * List statements that have the given {@link OntProperty} as property and the given {@link OntResource} as subject.
     * Returns the extracted objects of the statements.
     *
     * @param subject  Subject that should be contained
     * @param property Property used to look for
     * @return List of extracted objects
     */
    ImmutableList<RDFNode> getObjectsOf(OntResource subject, OntProperty property);

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
    <S extends RDFNode, T extends Resource> Optional<T> transformType(S from, Class<T> targetType);

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
    <S extends RDFNode, T extends Resource> T transformTypeNullable(S from, Class<T> targetType);

    /**
     * Transforms a given Node (that is a subtype of RDFNode) into an {@link Individual}. If it cannot be transformed,
     * returns <code>null</code>.
     *
     * @param node Node that should be transformed
     * @return The transformed Individual. If transformation was unsuccessful, returns null.
     */
    Optional<Individual> transformIntoIndividual(RDFNode node);

    /**
     * See {@link OntResource#getLocalName()}
     *
     * @param resource Resource
     * @return The localname of this property within its namespace.
     */
    String getLocalName(OntResource resource);

    /**
     * Returns a label for the given resource. For more details, see {@link OntResource#getLabel(String)}. The provided
     * language is set to <code>null</code>
     *
     * @param resource the resource
     * @return a label for the given resource or null if none is found
     */
    String getLabel(OntResource resource);

    /**
     * See {@link OntResource#getLabel(String)}
     *
     * @param resource the resource
     * @param lang     the language attribute
     * @return a label for the given resource or null if none is found
     */
    String getLabel(OntResource resource, String lang);

    /**
     * Generates a random URI using the default prefix
     *
     * @return random URI
     */
    String generateRandomURI();

    /**
     * Generates a random URI using the given prefix.
     *
     * @param prefix Prefix that should be used for namespace
     * @return random URI with the given prefix
     */
    String generateRandomURI(String prefix);

}
