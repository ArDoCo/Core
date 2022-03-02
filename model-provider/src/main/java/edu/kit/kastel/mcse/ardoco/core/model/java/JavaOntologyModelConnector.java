/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.model.java;

import org.apache.jena.ontology.Individual;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.informalin.ontology.OntologyConnector;
import edu.kit.kastel.informalin.ontology.OntologyInterface;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.model.IModelRelation;
import edu.kit.kastel.mcse.ardoco.core.model.Instance;

/**
 * @author Jan Keim
 *
 */
public class JavaOntologyModelConnector implements IModelConnector {
    private static final String CLASS_OR_INTERFACE_URI = "https://informalin.github.io/knowledgebases/informalin_base_java.owl#OWLClass_5c834f48_ae0d_40d8_8ea1_c193dc511593";
    private OntologyInterface ontologyConnector;

    /**
     * Instantiates a new pcm ontology model connector.
     *
     * @param ontologyUrl Can be a local URL (path to the ontology) or a remote URL
     */
    public JavaOntologyModelConnector(String ontologyUrl) {
        ontologyConnector = new OntologyConnector(ontologyUrl);
    }

    public JavaOntologyModelConnector(OntologyInterface ontologyConnector) {
        this.ontologyConnector = ontologyConnector;
    }

    @Override
    public ImmutableList<IModelInstance> getInstances() {
        MutableList<IModelInstance> instances = Lists.mutable.empty();

        var optionalClass = ontologyConnector.getClassByIri(CLASS_OR_INTERFACE_URI);
        if (optionalClass.isEmpty()) {
            return instances.toImmutable();
        }
        var clazz = optionalClass.get();
        var nameProperty = ontologyConnector.getPropertyByIri("https://informalin.github.io/knowledgebases/informalin_base_java.owl#name").orElseThrow();
        var fqnProperty = ontologyConnector.getPropertyByIri("https://informalin.github.io/knowledgebases/informalin_base_java.owl#fullyQualifiedName")
                .orElseThrow();
        var isInterfaceProperty = ontologyConnector.getPropertyByIri("https://informalin.github.io/knowledgebases/informalin_base_java.owl#isInterface")
                .orElseThrow();

        for (Individual individual : ontologyConnector.getIndividualsOfClass(clazz)) {
            var name = individual.getProperty(nameProperty).getString();
            var identifier = individual.getProperty(fqnProperty).getString();
            var isInterface = individual.getProperty(isInterfaceProperty).getBoolean();
            var type = isInterface ? "Interface" : "Class";
            var instance = new Instance(name, type, identifier);
            instances.add(instance);
        }
        return instances.toImmutable();
    }

    @Override
    public ImmutableList<IModelRelation> getRelations() {
        // NOT YET IMPLEMENTED!
        return Lists.immutable.empty();
    }

}
