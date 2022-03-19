/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.agent.InconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.InconsistencyAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.IInstanceLink;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.types.NameInconsistency;

//TODO Need to rethink the checks as some other code changed and we cannot simply analyze words anymore (Problematic for compounds)
public class NameInconsistencyAgent extends InconsistencyAgent {
    private static final String REGEX_DIVIDER = "[\\.:]";

    public NameInconsistencyAgent() {
    }

    @Override
    public void execute(InconsistencyAgentData data) {
        for (var model : data.getModelIds()) {
            var connectionState = data.getConnectionState(model);
            var inconsistencyState = data.getInconsistencyState(model);

            ImmutableList<IInstanceLink> tracelinks = connectionState.getInstanceLinks();
            for (IInstanceLink tracelink : tracelinks) {
                IModelInstance modelInstance = tracelink.getModelInstance();
                var recommendationInstance = tracelink.getTextualInstance();
                var nameMappings = recommendationInstance.getNameMappings();
                for (INounMapping nameMapping : nameMappings) {
                    var words = nameMapping.getWords();
                    for (IWord word : words) {
                        analyseWord(modelInstance, word, inconsistencyState);
                    }
                }
            }
        }
    }

    private void analyseWord(IModelInstance modelInstance, IWord word, IInconsistencyState inconsistencyState) {
        if (!equalTextOrLemma(modelInstance, word)) {
            if (partOfDividerEqualsModelInstance(modelInstance, word)) {
                return;
            }
            var nameInconsistency = new NameInconsistency(modelInstance, word);
            inconsistencyState.addInconsistency(nameInconsistency);
        }
    }

    /**
     * Return if text or lemma of provided word matches to the model instance
     *
     * @param modelInstance the model instance
     * @param word          the word
     * @return <code>true</code>, if there is a match, otherwise <code>false</code>
     */
    private static boolean equalTextOrLemma(IModelInstance modelInstance, IWord word) {
        String modelName = modelInstance.getFullName();
        String text = word.getText();
        String lemma = word.getLemma();

        // NOTE: maybe employ other/better String comparison instead of exact equals?
        return modelName.equalsIgnoreCase(text) && modelName.equalsIgnoreCase(lemma);
    }

    private static boolean partOfDividerEqualsModelInstance(IModelInstance modelInstance, IWord word) {
        String text = word.getText();
        String modelName = modelInstance.getFullName();
        for (String part : text.split(REGEX_DIVIDER)) {
            // NOTE: employ other/better String comparison instead of exact equals?
            if (part.equalsIgnoreCase(modelName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
    }
}
