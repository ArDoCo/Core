/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.TextProvider;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class CoreNLPProvider extends TextProvider {
    private static final String ANNOTATORS = "tokenize,ssplit,pos,parse,depparse,lemma"; // further: ",ner,coref"
    private static final String DEPENDENCIES_ANNOTATION = "EnhancedPlusPlusDependenciesAnnotation";
    private final InputStream text;
    private Text annotatedText;

    // Needed for Configuration Generation
    @SuppressWarnings("unused")
    private CoreNLPProvider() {
        super(null, null);
        this.text = null;
    }

    public CoreNLPProvider(DataRepository data, InputStream text) {
        super("CoreNLPTextProvider", data);
        annotatedText = null;
        this.text = text;
    }

    private static Properties getStanfordProperties(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("Properties are null");
        }
        var allStanfordProperties = new Properties(properties);
        allStanfordProperties.setProperty("annotators", ANNOTATORS);

        allStanfordProperties.put("parse", DEPENDENCIES_ANNOTATION);
        allStanfordProperties.put("depparse", DEPENDENCIES_ANNOTATION);
        allStanfordProperties.put("coref.algorithm", "fastneural");

        return allStanfordProperties;
    }

    @Override
    public Text getAnnotatedText(String textName) {
        logger.warn("Returning annotated text ignoring the provided name");
        return getAnnotatedText();
    }

    @Override
    public synchronized Text getAnnotatedText() {
        if (annotatedText == null) {
            if (DataRepositoryHelper.hasAnnotatedText(getDataRepository())) {
                annotatedText = DataRepositoryHelper.getAnnotatedText(getDataRepository());
            } else {
                annotatedText = processText(text);
            }
        }
        return annotatedText;
    }

    private Text processText(InputStream text) {
        var inputText = readInputText(text);
        Properties props = getStanfordProperties(new Properties());
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        CoreDocument document = new CoreDocument(inputText);
        pipeline.annotate(document);
        return new TextImpl(document);
    }

    private String readInputText(InputStream text) {
        var scanner = new Scanner(text, StandardCharsets.UTF_8);
        scanner.useDelimiter("\\A");
        String inputText = scanner.next();
        scanner.close();
        return inputText;
    }

    @Override
    public void run() {
        if (!DataRepositoryHelper.hasAnnotatedText(getDataRepository())) {
            var preprocessingData = new PreprocessingData(getAnnotatedText());
            DataRepositoryHelper.putPreprocessingData(getDataRepository(), preprocessingData);
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> map) {
        // empty
    }
}
