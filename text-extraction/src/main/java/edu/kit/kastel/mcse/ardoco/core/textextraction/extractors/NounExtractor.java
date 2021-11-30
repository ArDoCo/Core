/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.extractors;


import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.text.POSTag;
import edu.kit.kastel.mcse.ardoco.core.textextraction.GenericTextConfig;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtractionExtractor;
import org.kohsuke.MetaInfServices;

/**
 * The analyzer classifies nouns.
 *
 * @author Sophie
 *
 */

@MetaInfServices(TextExtractionExtractor.class)
public class NounExtractor extends TextExtractionExtractor {

    private double probability;

    /**
     * Prototype constructor.
     */
    public NounExtractor() {
        this(null);
    }

    /**
     * Instantiates a new noun extractor.
     *
     * @param textExtractionState the text extraction state
     */
    public NounExtractor(ITextState textExtractionState) {
        this(textExtractionState, GenericTextConfig.DEFAULT_CONFIG);
    }

    /**
     * Creates a new NounAnalyzer.
     *
     * @param textExtractionState the text extraction state
     * @param config              the module configuration
     */
    public NounExtractor(ITextState textExtractionState, GenericTextConfig config) {
        super(textExtractionState);
        probability = config.nounAnalyzerProbability;
    }

    @Override
    public TextExtractionExtractor create(ITextState textExtractionState, Configuration config) {
        return new NounExtractor(textExtractionState, (GenericTextConfig) config);
    }

    /**
     * Exec.
     *
     * @param n the n
     */
    @Override
    public void exec(IWord n) {

        String nodeValue = n.getText();
        if (nodeValue.length() == 1 && !Character.isLetter(nodeValue.charAt(0))) {
            return;
        }

        findSingleNouns(n);

    }

    /**
     * Finds all nouns and adds them as name-or-type mappings (and types) to the text extraction state.
     *
     * @param n node to check
     */
    private void findSingleNouns(IWord n) {
        POSTag pos = n.getPosTag();
        if (POSTag.NOUN_PROPER_SINGULAR == pos || POSTag.NOUN == pos || POSTag.NOUN_PROPER_PLURAL == pos) {
            textState.addNort(n, n.getText(), probability);
        }
        if (POSTag.NOUN_PLURAL == pos) {
            textState.addType(n, n.getText(), probability);
        }

    }

}
