/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.informants;

import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.POSTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;

/**
 * The analyzer classifies nouns.
 *
 */
public class NounInformant extends Informant {
    @Configurable
    private double nameOrTypeWeight = 0.5;

    @Configurable
    private double probability = 0.2;

    /**
     * Prototype constructor.
     */
    public NounInformant(DataRepository data) {
        super(NounInformant.class.getSimpleName(), data);
    }

    @Override
    public void run() {
        ImmutableList<Word> words = DataRepositoryHelper.getAnnotatedText(getDataRepository()).words();
        var textState = DataRepositoryHelper.getTextState(getDataRepository());
        for (var word : words) {
            var text = word.getText();
            if (text.length() > 1 && Character.isLetter(text.charAt(0))) {
                findSingleNouns(textState, word);
            }
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
