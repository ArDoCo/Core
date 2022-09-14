/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.informants;

import java.util.Map;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.POSTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;

/**
 * This analyzer classifies all nodes, containing separators, as names and adds them as mappings to the current text
 * extraction state.
 *
 */

public class SeparatedNamesInformant extends Informant {

    @Configurable
    private double probability = 0.8;

    /**
     * Prototype constructor.
     */
    public SeparatedNamesInformant(DataRepository dataRepository) {
        super(SeparatedNamesInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void run() {
        var textState = DataRepositoryHelper.getTextState(getDataRepository());
        for (var word : DataRepositoryHelper.getAnnotatedText(getDataRepository()).words()) {
            exec(textState, word);
        }
    }

    /***
     * Checks if Node Value contains separator. If true, it is split and added separately to the names of the text
     * extraction state.
     */
    private void exec(TextState textState, Word word) {
        checkForSeparatedNode(textState, word);
    }

    /**
     * Checks if Node Value contains separator. If true, it is split and added separately to the names of the text
     * extraction state.
     */
    private void checkForSeparatedNode(TextState textState, Word word) {
        if (word.getPosTag() != POSTag.FOREIGN_WORD && CommonUtilities.containsSeparator(word.getText())) {
            textState.addNounMapping(word, MappingKind.NAME, this, probability);
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // emtpy
    }

}
