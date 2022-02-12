/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.extractors;

import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.text.POSTag;
import edu.kit.kastel.mcse.ardoco.core.textextraction.GenericTextConfig;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtractionExtractor;

/**
 * This analyzer classifies all nodes, containing separators, as names and adds them as mappings to the current text
 * extraction state.
 *
 * @author Sophie
 *
 */

@MetaInfServices(TextExtractionExtractor.class)
public class SeparatedNamesExtractor extends TextExtractionExtractor {

    private double probability;

    /**
     * Prototype constructor.
     */
    public SeparatedNamesExtractor() {
        this(null);
    }

    /**
     * Instantiates a new separated names extractor.
     *
     * @param textExtractionState the text extraction state
     */
    public SeparatedNamesExtractor(ITextState textExtractionState) {
        this(textExtractionState, GenericTextConfig.DEFAULT_CONFIG);
    }

    /**
     * Creates a new SeparatedNamesIdentifier.
     *
     * @param textExtractionState the text extraction state
     * @param config              the module configuration
     */
    public SeparatedNamesExtractor(ITextState textExtractionState, GenericTextConfig config) {
        super(textExtractionState);
        probability = config.separatedNamesAnalyzerProbability;
    }

    @Override
    public TextExtractionExtractor create(ITextState textExtractionState, Configuration config) {
        return new SeparatedNamesExtractor(textExtractionState, (GenericTextConfig) config);
    }

    /***
     * Checks if Node Value contains separator. If true, it is split and added separately to the names of the text
     * extraction state.
     */
    @Override
    public void exec(IWord word) {
        checkForSeparatedNode(word);
    }

    /***
     * Checks if Node Value contains separator. If true, it is split and added separately to the names of the text
     * extraction state.
     *
     * @param word word to check
     */
    private void checkForSeparatedNode(IWord word) {
        if (word.getPosTag() != POSTag.FOREIGN_WORD && CommonUtilities.containsSeparator(word.getText())) {
            textState.addName(word, probability);
        }
    }

}
