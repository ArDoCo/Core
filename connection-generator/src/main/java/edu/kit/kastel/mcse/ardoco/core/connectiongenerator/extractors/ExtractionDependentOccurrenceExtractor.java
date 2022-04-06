/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.extractors;

import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractExtractor;
import edu.kit.kastel.mcse.ardoco.core.api.agent.ConnectionAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.common.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

/**
 * This analyzer searches for the occurrence of instance names and types of the extraction state and adds them as names
 * and types to the text extraction state.
 *
 * @author Sophie schulz
 * @author Jan Keim
 */
public class ExtractionDependentOccurrenceExtractor extends AbstractExtractor<ConnectionAgentData> {

    @Configurable
    private double probability = 1.0;

    public ExtractionDependentOccurrenceExtractor() {
        // empty
    }

    @Override
    public void exec(ConnectionAgentData data, IWord word) {
        for (var model : data.getModelIds()) {
            // TODO revisit and check if we want to check something different than only words as well
            searchForName(data.getModelState(model), data.getTextState(), word);
            searchForType(data.getModelState(model), data.getTextState(), word);
        }
    }

    /**
     * This method checks whether a given node is a name of an instance given in the model extraction state. If it
     * appears to be a name this is stored in the text extraction state.
     */
    private void searchForName(IModelState modelState, ITextState textState, IWord word) {
        if (posTagIsUndesired(word) && !wordStartsWithCapitalLetter(word)) {
            return;
        }
        var instanceNameIsSimilar = modelState.getInstances().anySatisfy(i -> SimilarityUtils.isWordSimilarToModelInstance(word, i));
        if (instanceNameIsSimilar) {
            textState.addName(word, this, probability);
        }
    }

    private boolean wordStartsWithCapitalLetter(IWord word) {
        return Character.isUpperCase(word.getText().charAt(0));
    }

    private boolean posTagIsUndesired(IWord word) {
        return !word.getPosTag().getTag().startsWith("NN");
    }

    /**
     * This method checks whether a given node is a type of an instance given in the model extraction state. If it
     * appears to be a type this is stored in the text extraction state. If multiple options are available the node
     * value is taken as reference.
     */
    private void searchForType(IModelState modelState, ITextState textState, IWord word) {
        var instanceTypeIsSimilar = modelState.getInstances().anySatisfy(i -> SimilarityUtils.isWordSimilarToModelInstanceType(word, i));
        if (instanceTypeIsSimilar) {
            textState.addType(word, this, probability);
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // handle additional config
    }
}
