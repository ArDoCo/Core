/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.extractors;

import java.util.Map;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractExtractor;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.POSTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextState;

/**
 * This analyzer classifies all nodes, containing separators, as names and adds them as mappings to the current text
 * extraction state.
 *
 * @author Sophie
 */

public class SeparatedNamesExtractor extends AbstractExtractor {

    @Configurable
    private double probability = 0.8;

    /**
     * Prototype constructor.
     */
    public SeparatedNamesExtractor(DataRepository dataRepository) {
        super("SeparatedNamesExtractor", dataRepository);
    }

    @Override
    public void run() {
        var textState = TextExtraction.getTextState(getDataRepository());
        for (var word : TextExtraction.getAnnotatedText(getDataRepository()).getWords()) {
            exec(textState, word);
        }
    }

    /***
     * Checks if Node Value contains separator. If true, it is split and added separately to the names of the text
     * extraction state.
     */
    private void exec(TextState textState, IWord word) {
        checkForSeparatedNode(textState, word);
    }

    /**
     * Checks if Node Value contains separator. If true, it is split and added separately to the names of the text
     * extraction state.
     */
    private void checkForSeparatedNode(ITextState textState, IWord word) {
        if (word.getPosTag() != POSTag.FOREIGN_WORD && CommonUtilities.containsSeparator(word.getText())) {
            textState.addNounMapping(word, MappingKind.NAME, this, probability);
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        this.applyConfiguration(additionalConfiguration);
    }

}
