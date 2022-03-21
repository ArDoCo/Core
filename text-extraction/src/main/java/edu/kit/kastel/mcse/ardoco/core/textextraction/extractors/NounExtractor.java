/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.extractors;

import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractExtractor;
import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.common.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.POSTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;

/**
 * The analyzer classifies nouns.
 *
 * @author Sophie
 */
public class NounExtractor extends AbstractExtractor<TextAgentData> {

    @Configurable
    private double probability = 0.2;

    /**
     * Prototype constructor.
     */
    public NounExtractor() {
    }

    /**
     * Exec.
     *
     * @param n the n
     */
    @Override
    public void exec(TextAgentData data, IWord n) {

        String nodeValue = n.getText();
        if (nodeValue.length() == 1 && !Character.isLetter(nodeValue.charAt(0))) {
            return;
        }

        findSingleNouns(data.getTextState(), n);

    }

    /**
     * Finds all nouns and adds them as name-or-type mappings (and types) to the text extraction state.
     */
    private void findSingleNouns(ITextState textState, IWord word) {
        POSTag pos = word.getPosTag();
        if (POSTag.NOUN_PROPER_SINGULAR == pos || POSTag.NOUN == pos || POSTag.NOUN_PROPER_PLURAL == pos) {
            textState.addNort(word, probability);
        }
        if (POSTag.NOUN_PLURAL == pos) {
            textState.addType(word, probability);
        }

    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
    }
}
