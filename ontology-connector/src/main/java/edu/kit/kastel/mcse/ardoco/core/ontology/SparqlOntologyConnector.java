/**
 *
 */
package edu.kit.kastel.mcse.ardoco.core.ontology;

import java.util.Optional;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Jan Keim
 *
 */
public class SparqlOntologyConnector extends OntologyConnector {
    private static Logger logger = LogManager.getLogger(SparqlOntologyConnector.class);
    private static final String N = System.lineSeparator();

    private static final String PREFIX = """
            PREFIX owl: <http://www.w3.org/2002/07/owl#>
            PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            """;

    private static final String LABEL_QUERY = "SELECT ?res WHERE { ?res rdfs:label ?label FILTER (str(?label) = \"%s\") } LIMIT 1 ";
    private static final String LOCALNAME_CLASS_QUERY = "SELECT ?res where { ?res a owl:Class . FILTER(STRENDS(STR(?res),\"%s\"))}";
    private static final String LOCALNAME_PROPERTY_QUERY = "SELECT ?res where { ?res rdf:type [] . [] rdf:type rdf:Property . FILTER(STRENDS(STR(?res),\"%s\"))}";

    private Dataset dataset;

    public SparqlOntologyConnector(String ontologyUrl) {
        super(ontologyUrl);
        dataset = DatasetFactory.create();
        dataset.setDefaultModel(ontModel);
    }

    private ResultSet query(String query) {
        try (QueryExecution qexec = QueryExecutionFactory.create(query, dataset)) {
            ResultSet results = qexec.execSelect();
            results = ResultSetFactory.copyResults(results);
            return results;
        }
    }

    private Optional<Resource> getResourceByLabel(String label) {
        String queryString = PREFIX + N + String.format(LABEL_QUERY, label);
        var results = query(queryString);
        if (results.hasNext()) {
            var soln = results.next();
            return Optional.ofNullable(soln.getResource("res"));
        }
        return Optional.empty();
    }

    @Override
    public Optional<OntClass> getClass(String className) {

        var resourceOpt = getResourceByLabel(className);
        if (resourceOpt.isPresent()) {
            var resource = resourceOpt.get();
            var clazz = getClassByIri(resource.getURI());
            if (clazz.isPresent()) {
                return clazz;
            }
        }

        var queryString = PREFIX + N + String.format(LOCALNAME_CLASS_QUERY, className);
        var results = query(queryString);
        while (results.hasNext()) {
            var soln = results.next();
            var resource = soln.getResource("res");
            if (resource.getLocalName().equals(className)) {
                var clazz = getClassByIri(resource.getURI());
                if (clazz.isPresent()) {
                    return clazz;
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<OntProperty> getProperty(String propertyName) {
        var resourceOpt = getResourceByLabel(propertyName);
        if (resourceOpt.isPresent()) {
            var resource = resourceOpt.get();
            var property = getPropertyByIri(resource.getURI());
            if (property.isPresent()) {
                return property;
            }
        }

        var queryString = PREFIX + N + String.format(LOCALNAME_PROPERTY_QUERY, propertyName);
        var results = query(queryString);
        while (results.hasNext()) {
            var soln = results.next();
            var resource = soln.getResource("res");
            if (resource.getLocalName().equals(propertyName)) {
                var property = getPropertyByIri(resource.getURI());
                if (property.isPresent()) {
                    return property;
                }
            }
        }

        return Optional.empty();
    }
}
