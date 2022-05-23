/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class CoreNLPProvider implements ITextConnector {
    private static final Logger logger = LoggerFactory.getLogger(CoreNLPProvider.class);
    private static final List<String> COREF_ALGORITHMS = List.of("fastneural", "neural", "statistical", "clustering");
    private static final String ANNOTATORS = "tokenize,ssplit,pos,parse,depparse,lemma"; // further: ",ner,coref"
    private static final String DEPENDENCIES_ANNOTATION = "EnhancedPlusPlusDependenciesAnnotation";
    private final InputStream text;
    private IText annotatedText;

    public CoreNLPProvider(InputStream text) {
        annotatedText = null;
        this.text = text;
    }

    private static Properties getStanfordProperties(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("Properties are null");
        }
        var allStanfordProperties = new Properties(properties);
        allStanfordProperties.setProperty("annotators", ANNOTATORS);

        if (!allStanfordProperties.containsKey("parse")) {
            allStanfordProperties.put("parse", DEPENDENCIES_ANNOTATION);
        }
        if (!allStanfordProperties.containsKey("depparse")) {
            allStanfordProperties.put("depparse", DEPENDENCIES_ANNOTATION);
        }

        String corefAlgorithm = allStanfordProperties.getProperty("coref.algorithm", COREF_ALGORITHMS.get(0));
        if (!COREF_ALGORITHMS.contains(corefAlgorithm)) {
            logger.warn("Provided CoRef-Algorithm not found. Selecting default.");
            corefAlgorithm = COREF_ALGORITHMS.get(0);
        }
        allStanfordProperties.put("coref.algorithm", corefAlgorithm);

        return allStanfordProperties;
    }

    @Override
    public IText getAnnotatedText(String textName) {
        logger.warn("Returning annotated text ignoring the provided name");
        return getAnnotatedText();
    }

    @Override
    public synchronized IText getAnnotatedText() {
        if (annotatedText == null) {
            annotatedText = processText(text);
        }
        return annotatedText;
    }

    private IText processText(InputStream text) {
        var inputText = readInputText(text);
        Properties props = getStanfordProperties(new Properties());
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        CoreDocument document = new CoreDocument(inputText);
        pipeline.annotate(document);
        return new Text(document);
    }

    private String readInputText(InputStream text) {
        var scanner = new Scanner(text, StandardCharsets.UTF_8);
        scanner.useDelimiter("\\A");
        String inputText = scanner.next();
        scanner.close();
        return inputText;
    }
}
