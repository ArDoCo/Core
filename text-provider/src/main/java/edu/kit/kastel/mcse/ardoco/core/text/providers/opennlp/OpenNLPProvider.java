package edu.kit.kastel.mcse.ardoco.core.text.providers.opennlp;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.TextProvider;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

public class OpenNLPProvider extends TextProvider {

    private final InputStream text;
    private Text annotatedText;


    @SuppressWarnings("unused")
    private OpenNLPProvider() {
        super(null, null);
        this.text = null;
    }

    public OpenNLPProvider(DataRepository data, InputStream text) {
        super("OpenNLPTextProvider", data);
        this.annotatedText = null;
        this.text = text;
    }

    @Override
    public Text getAnnotatedText(String textName) {
        logger.warn("Returning annotated text ignoring the provided name");
        return getAnnotatedText();
    }

    @Override
    public synchronized Text getAnnotatedText() {
        if (annotatedText == null) {
//            if (DataRepositoryHelper.hasAnnotatedText(getDataRepository())) {
//                annotatedText = DataRepositoryHelper.getAnnotatedText(getDataRepository());
//            } else {
//                annotatedText = processText(text);
//            }
            annotatedText = processText(text);
        }
        return annotatedText;
    }

    private Text processText(InputStream text) {
        String inputText = readInputText(text);
        return new OpenNLPTextImpl(inputText);
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
