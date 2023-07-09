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
    private static Logger logger = LoggerFactory.getLogger(AbbreviationDisambiguationHelper.class);
    private static DBPediaHelper instance;
    private Record record;

    public static synchronized @NotNull DBPediaHelper getInstance() {
        if (instance == null) {
            instance = new DBPediaHelper();
        }
        return instance;
    }

    private DBPediaHelper() {
    }

    private List<String> loadProgrammingLanguages() {
        ParameterizedSparqlString qs = new ParameterizedSparqlString("""
                prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n
                prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#>\n
                PREFIX dbo:     <http://dbpedia.org/ontology/>
                PREFIX yago: <http://dbpedia.org/class/yago/>
                \n
                SELECT ?label \n
                WHERE { \n
                        ?pl dbo:abstract ?abstract .\n
                        ?pl rdfs:label ?label .\n
                        ?pl rdf:type yago:ProgrammingLanguage106898352 .\n
                        FILTER (LANG(?abstract) = 'en') .\n
                        FILTER (LANG(?label)='en')
                }
                GROUP BY ?label""");

        var languages = runQuery(qs);
        logger.info("Retrieved {} programming languages from DBPedia", languages.size());
        return languages;
    }

    private List<String> loadMarkupLanguages() {
        ParameterizedSparqlString qs = new ParameterizedSparqlString("""
                prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n
                prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#>\n
                PREFIX dbo:     <http://dbpedia.org/ontology/>
                PREFIX yago: <http://dbpedia.org/class/yago/>
                \n
                SELECT ?label \n
                WHERE { \n
                        ?pl dbo:abstract ?abstract .\n
                        ?pl rdfs:label ?label .\n
                        ?pl rdf:type yago:MarkupLanguage106787835 .\n
                        FILTER (LANG(?abstract) = 'en') .\n
                        FILTER (LANG(?label)='en')
                }
                GROUP BY ?label""");

        var languages = runQuery(qs);
        logger.info("Retrieved {} programming languages from DBPedia", languages.size());
        return languages;
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
        return new Record(loadProgrammingLanguages(), loadMarkupLanguages());
    }

    @Override
    public Record load() {
        if (record != null)
            return record;
        try {
            logger.info("Reading {} file", getIdentifier());
            Record r = new ObjectMapper().readValue(getFile(), new TypeReference<Record>() {
            });
            return r;
        } catch (IOException e) {
            logger.error("Error reading {} file", getIdentifier());
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getIdentifier() {
        return "dbpedia";
    }

    public record Record(List<String> programmingLanguages, List<String> markupLanguages) {
    }
}
