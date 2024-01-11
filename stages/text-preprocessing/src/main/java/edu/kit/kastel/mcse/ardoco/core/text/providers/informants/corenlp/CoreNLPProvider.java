/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp;

import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.text.NlpInformant;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.textprocessor.TextProcessor;

public class CoreNLPProvider extends NlpInformant {

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
    public void process() {
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

    private Text processText(String inputText) {
        return new TextProcessor().processText(inputText);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> map) {
        // empty
    }
}
