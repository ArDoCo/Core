package edu.kit.kastel.mcse.ardoco.core.common.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.compress.utils.Lists;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DBPediaHelper extends FileBasedCache<DBPediaHelper.Record> {
    private static Logger logger = LoggerFactory.getLogger(DBPediaHelper.class);
    private static DBPediaHelper instance;
    private Record record;

    public static synchronized @NotNull DBPediaHelper getInstance() {
        if (instance == null) {
            instance = new DBPediaHelper();
        }
        return instance;
    }

    private DBPediaHelper() {
        super("dbpedia", ".json", "");
    }

    private List<String> loadProgrammingLanguages() {
        ParameterizedSparqlString qs = new ParameterizedSparqlString("""
                prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#>
                PREFIX dbo:     <http://dbpedia.org/ontology/>
                PREFIX yago: <http://dbpedia.org/class/yago/>
                                
                SELECT ?label 
                WHERE { 
                        {
                        ?pl dbo:abstract ?abstract .
                        ?pl rdfs:label ?label .
                        ?pl rdf:type yago:ProgrammingLanguage106898352 .
                        FILTER (LANG(?abstract) = 'en') .
                        FILTER (LANG(?label)='en')
                        }
                        UNION
                        {
                        ?pl dbo:abstract ?abstract .
                        ?pl rdfs:label ?label .
                        ?pl dbo:influenced ?influenced .
                        ?pl dbo:influencedBy ?influencedBy .
                        ?pl rdf:type dbo:ProgrammingLanguage .
                        FILTER (LANG(?abstract) = 'en') .
                        FILTER (LANG(?label)='en')
                        }
                }
                GROUP BY ?label""");

        var languages = runQuery(qs);
        logger.info("Retrieved {} programming languages from DBPedia", languages.size());
        return languages;
    }

    private List<String> loadMarkupLanguages() {
        ParameterizedSparqlString qs = new ParameterizedSparqlString("""
                prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#>
                PREFIX dbo:     <http://dbpedia.org/ontology/>
                PREFIX yago: <http://dbpedia.org/class/yago/>
                                
                SELECT ?label 
                WHERE { 
                        ?pl dbo:abstract ?abstract .
                        ?pl rdfs:label ?label .
                        ?pl rdf:type yago:MarkupLanguage106787835 .
                        FILTER (LANG(?abstract) = 'en') .
                        FILTER (LANG(?label)='en')
                }
                GROUP BY ?label""");

        var languages = runQuery(qs);
        logger.info("Retrieved {} markup languages from DBPedia", languages.size());
        return languages;
    }

    private List<String> loadSoftware() {
        ParameterizedSparqlString qs = new ParameterizedSparqlString("""
                prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#>
                PREFIX dbo:     <http://dbpedia.org/ontology/>
                PREFIX yago: <http://dbpedia.org/class/yago/>
                                
                SELECT ?label 
                WHERE { 
                        ?p rdf:type dbo:Software .
                        ?p dbo:programmingLanguage ?pl .
                        ?pl dbo:abstract ?abstract .
                        ?pl rdfs:label ?label .
                        FILTER (LANG(?abstract) = 'en') .
                        FILTER (LANG(?label)='en')
                }
                GROUP BY ?label""");

        var software = runQuery(qs);
        logger.info("Retrieved {} software from DBPedia", software.size());
        return software;
    }

    private List<String> runQuery(ParameterizedSparqlString query) {
        var list = List.<String>of();
        ResultSet results;
        try (QueryExecution exec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query.asQuery())) {
            results = exec.execSelect();
            var asList = Lists.newArrayList(results);
            list = asList.stream().map(l -> l.getLiteral("label").getLexicalForm().replaceAll("\\((.*?)\\)", "").trim()).sorted().toList();
        }
        return list;
    }

    @Override
    public void save(Record r) {
        try (PrintWriter out = new PrintWriter(getFile())) {
            //Parse before writing to the file, so we don't mess up the entire file due to a parsing error
            String json = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(r);
            out.print(json);
            logger.info("Saved {} file", getIdentifier());
        } catch (IOException e) {
            logger.error(e.getCause().getMessage());
        }
    }

    @Override
    public Record getDefault() {
        return new Record(loadProgrammingLanguages(), loadMarkupLanguages(), loadSoftware());
    }

    @Override
    public Record load(boolean allowReload) {
        if (record != null)
            return record;
        try {
            logger.info("Reading {} file", getIdentifier());
            record = new ObjectMapper().readValue(getFile(), new TypeReference<Record>() {
            });
            return record;
        } catch (IOException e) {
            logger.error("Error reading {} file", getIdentifier());
            throw new RuntimeException(e);
        }
    }

    public record Record(List<String> programmingLanguages, List<String> markupLanguages, List<String> software) {
    }

    public static boolean isWordProgrammingLanguage(String word) {
        return getInstance().load().programmingLanguages().stream().anyMatch(s -> s.replaceAll("\\s+", "").equalsIgnoreCase(word.replaceAll("\\s+", "")));
    }

    public static boolean isWordMarkupLanguage(String word) {
        return getInstance().load().markupLanguages().stream().anyMatch(s -> s.replaceAll("\\s+", "").equalsIgnoreCase(word.replaceAll("\\s+", "")));
    }

    public static boolean isWordSoftware(String word) {
        return getInstance().load().software().stream().anyMatch(s -> s.replaceAll("\\s+", "").equalsIgnoreCase(word.replaceAll("\\s+", "")));
    }
}
