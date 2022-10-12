/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.informants;

import java.util.Map;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

/**
 * This analyzer searches for the occurrence of instance names and types of the extraction state and adds them as names
 * and types to the text extraction state.
 *
 */
public class ExtractionDependentOccurrenceInformant extends Informant {

    @Configurable
    private double probability = 1.0;

    public ExtractionDependentOccurrenceInformant(DataRepository dataRepository) {
        super(ExtractionDependentOccurrenceInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void run() {
        DataRepository dataRepository = getDataRepository();
        var text = DataRepositoryHelper.getAnnotatedText(dataRepository);
        var textState = DataRepositoryHelper.getTextState(dataRepository);
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        for (var word : text.words()) {
            exec(textState, modelStates, word);
        }
    }

    private void exec(TextState textState, ModelStates modelStates, Word word) {
        for (var model : modelStates.modelIds()) {
            var modelState = modelStates.getModelState(model);

            searchForName(modelState, textState, word);
            searchForType(modelState, textState, word);
        }
    }

    /**
     * This method checks whether a given node is a name of an instance given in the model extraction state. If it
     * appears to be a name this is stored in the text extraction state.
     */
    private void searchForName(ModelExtractionState modelState, TextState textState, Word word) {
        if (posTagIsUndesired(word) && !wordStartsWithCapitalLetter(word)) {
            return;
        }
        var instanceNameIsSimilar = modelState.getInstances().anySatisfy(i -> SimilarityUtils.isWordSimilarToModelInstance(word, i));
        if (instanceNameIsSimilar) {
            textState.addNounMapping(word, MappingKind.NAME, this, probability);
        }
    }

    private boolean wordStartsWithCapitalLetter(Word word) {
        return Character.isUpperCase(word.getText().charAt(0));
    }

    private boolean posTagIsUndesired(Word word) {
        return !word.getPosTag().getTag().startsWith("NN");
    }

    /**
     * This method checks whether a given node is a type of an instance given in the model extraction state. If it
     * appears to be a type this is stored in the text extraction state. If multiple options are available the node
     * value is taken as reference.
     */
    private void searchForType(ModelExtractionState modelState, TextState textState, Word word) {
        var instanceTypeIsSimilar = modelState.getInstances().anySatisfy(i -> SimilarityUtils.isWordSimilarToModelInstanceType(word, i));
        if (instanceTypeIsSimilar) {
            textState.addNounMapping(word, MappingKind.TYPE, this, probability);
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // handle additional config
    }
}
