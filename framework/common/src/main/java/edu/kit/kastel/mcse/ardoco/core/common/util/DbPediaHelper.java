/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.common.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.compress.utils.Lists;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class provides lists of computer- and software-related terminology. It retrieves the terminology from the DBPedia ontology using SPARQL queries. The
 * class caches the lists in a {@link FileBasedCache} in the user data directory folder of ArDoCo.
 */
public class DbPediaHelper extends FileBasedCache<DbPediaHelper.DbPediaData> {
    private static final Logger logger = LoggerFactory.getLogger(DbPediaHelper.class);
    private static DbPediaHelper instance;

    /**
     * {@return the singleton instance}
     */
    static synchronized DbPediaHelper getInstance() {
        if (instance == null) {
            instance = new DbPediaHelper();
        }
        return instance;
    }

    private DbPediaHelper() {
        super("dbpedia", ".json", "");
    }

    /**
     * SPARQL query to retrieve programming languages from the Yago programming languages and DBOntology programming languages category.
     *
     * @return a list of programming languages
     */
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

    /**
     * SPARQL query to retrieve markup languages from the Yago markup languages category.
     *
     * @return a list of markup languages
     */
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

    /**
     * SPARQL query to retrieve softwares from the DBOntology software category.
     *
     * @return a list of softwares
     */
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

    /**
     * {@return all labels retrieved by the SPARQL query}
     *
     * @param query the parameterized query
     */
    private List<String> runQuery(ParameterizedSparqlString query) {
        var list = List.<String>of();
        ResultSet results;
        try (QueryExecution exec = QueryExecution.service("http://dbpedia.org/sparql").query(query.asQuery()).build()) {
            results = exec.execSelect();
            var asList = Lists.newArrayList(results);
            list = asList.stream().map(l -> l.getLiteral("label").getLexicalForm().replaceAll("\\((.*?)\\)", "").trim()).sorted().toList();
        }
        return list;
    }

    @Override
    protected void write(DbPediaData r) {
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
    protected DbPediaData getDefault() {
        return new DbPediaData(loadProgrammingLanguages(), loadMarkupLanguages(), loadSoftware());
    }

    @Override
    protected DbPediaData read() throws CacheException {
        try {
            logger.info("Reading {} file", getIdentifier());
            return new ObjectMapper().readValue(getFile(), new TypeReference<>() {
            });
        } catch (IOException e) {
            logger.error("Error reading {} file", getIdentifier());
            throw new CacheException(e);
        }
    }

    /**
     * Record used for caching
     *
     * @param programmingLanguages the list of programming languages
     * @param markupLanguages      the list of markup languages
     * @param software             the list of software
     */
    protected record DbPediaData(List<String> programmingLanguages, List<String> markupLanguages, List<String> software) {
    }

    /**
     * {@return whether a word is a programming language}
     *
     * @param word the word
     */
    public static boolean isWordProgrammingLanguage(String word) {
        return getInstance().getOrRead().programmingLanguages().stream().anyMatch(s -> s.replaceAll("\\s+", "").equalsIgnoreCase(word.replaceAll("\\s+", "")));
    }

    /**
     * {@return whether a word is a markup language}
     *
     * @param word the word
     */
    public static boolean isWordMarkupLanguage(String word) {
        return getInstance().getOrRead().markupLanguages().stream().anyMatch(s -> s.replaceAll("\\s+", "").equalsIgnoreCase(word.replaceAll("\\s+", "")));
    }

    /**
     * {@return whether a word is a software}
     *
     * @param word the word
     */
    public static boolean isWordSoftware(String word) {
        return getInstance().getOrRead().software().stream().anyMatch(s -> s.replaceAll("\\s+", "").equalsIgnoreCase(word.replaceAll("\\s+", "")));
    }
}
