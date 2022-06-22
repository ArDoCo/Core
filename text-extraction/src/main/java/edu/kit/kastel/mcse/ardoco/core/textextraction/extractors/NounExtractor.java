/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.extractors;

import java.util.Map;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractInformant;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.POSTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;

/**
 * The analyzer classifies nouns.
 *
 * @author Sophie Schulz
 * @author Jan Keim
 */
public class NounExtractor extends AbstractInformant {
    @Configurable
    private double nameOrTypeWeight = 0.5;

    @Configurable
    private double probability = 0.2;

    /**
     * Prototype constructor.
     */
    public NounExtractor(DataRepository data) {
        super("NounExtractor", data);
    }

    @Override
    public void run() {
        for (var n : DataRepositoryHelper.getAnnotatedText(getDataRepository()).words()) {
            var nodeValue = n.getText();
            if (nodeValue.length() == 1 && !Character.isLetter(nodeValue.charAt(0))) {
                return;
            }

            var textState = DataRepositoryHelper.getTextState(getDataRepository());
            findSingleNouns(textState, n);
        }
    }

    /**
     * Finds all nouns and adds them as name-or-type mappings (and types) to the text extraction state.
     */
    private void findSingleNouns(TextState textState, Word word) {
        var pos = word.getPosTag();
        if (POSTag.NOUN_PROPER_SINGULAR == pos || POSTag.NOUN == pos || POSTag.NOUN_PROPER_PLURAL == pos) {
            textState.addNounMapping(word, MappingKind.NAME, this, probability * nameOrTypeWeight);
            textState.addNounMapping(word, MappingKind.TYPE, this, probability * nameOrTypeWeight);
        }
        if (POSTag.NOUN_PLURAL == pos) {
            textState.addNounMapping(word, MappingKind.TYPE, this, probability);
        }

    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // empty
    }
}
