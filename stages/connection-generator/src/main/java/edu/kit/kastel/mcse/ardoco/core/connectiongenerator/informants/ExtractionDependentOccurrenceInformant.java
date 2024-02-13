/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.informants;

import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.LegacyModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

/**
 * This analyzer searches for the occurrence of instance names and types of the extraction state and adds them as names
 * and types to the text extraction state.
 */
public class ExtractionDependentOccurrenceInformant extends Informant {

    @Configurable
    private double probability = 1.0;

    public ExtractionDependentOccurrenceInformant(DataRepository dataRepository) {
        super(ExtractionDependentOccurrenceInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void process() {
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
            var modelState = modelStates.getModelExtractionState(model);

            searchForName(modelState, textState, word);
            searchForType(modelState, textState, word);
        }
    }

    /**
     * This method checks whether a given node is a name of an instance given in the model extraction state. If it
     * appears to be a name this is stored in the text extraction state.
     */
    private void searchForName(LegacyModelExtractionState modelState, TextState textState, Word word) {
        if (posTagIsUndesired(word) && !wordStartsWithCapitalLetter(word)) {
            return;
        }
        var instanceNameIsSimilar = modelState.getInstances().anySatisfy(i -> getMetaData().getSimilarityUtils().isWordSimilarToModelInstance(word, i));
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
    private void searchForType(LegacyModelExtractionState modelState, TextState textState, Word word) {
        var similarityUtils = getMetaData().getSimilarityUtils();
        var instanceTypeIsSimilar = modelState.getInstances().anySatisfy(i -> similarityUtils.isWordSimilarToModelInstanceType(word, i));
        if (instanceTypeIsSimilar) {
            textState.addNounMapping(word, MappingKind.TYPE, this, probability);
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        // handle additional config
    }
}
