/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp;

import java.util.Map;
import java.util.Properties;

import edu.kit.kastel.mcse.ardoco.core.api.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.text.NlpInformant;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class CoreNLPProvider extends NlpInformant {
    private static final String ANNOTATORS = "tokenize,ssplit,pos,parse,depparse,lemma"; // further: ",ner,coref"
    private static final String DEPENDENCIES_ANNOTATION = "EnhancedPlusPlusDependenciesAnnotation";

    private Text annotatedText;

    // Needed for Configuration Generation
    @SuppressWarnings("unused")
    private CoreNLPProvider() {
        super(CoreNLPProvider.class.getSimpleName(), null);
    }

    public CoreNLPProvider(DataRepository data) {
        super(CoreNLPProvider.class.getSimpleName(), data);
        annotatedText = null;
    }

    @Override
    public void run() {
        if (!DataRepositoryHelper.hasAnnotatedText(getDataRepository())) {
            var preprocessingData = new PreprocessingData(getAnnotatedText());
            DataRepositoryHelper.putPreprocessingData(getDataRepository(), preprocessingData);
        }
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
                String text = DataRepositoryHelper.getInputText(getDataRepository());
                annotatedText = processText(text);
            }
        }
        return annotatedText;
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

    private Text processText(String inputText) {
        Properties props = getStanfordProperties(new Properties());
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        CoreDocument document = new CoreDocument(inputText);
        pipeline.annotate(document);
        return new TextImpl(document);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> map) {
        // empty
    }
}
