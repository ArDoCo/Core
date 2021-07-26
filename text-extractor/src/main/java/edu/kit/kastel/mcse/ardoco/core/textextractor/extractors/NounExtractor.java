package edu.kit.kastel.mcse.ardoco.core.textextractor.extractors;

import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.POSTag;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.TextExtractor;
import edu.kit.kastel.mcse.ardoco.core.textextractor.GenericTextConfig;

/**
 * The analyzer classifies nouns.
 *
 * @author Sophie
 *
 */

@MetaInfServices(TextExtractor.class)
public class NounExtractor extends TextExtractor {

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
    public TextExtractor create(ITextState textExtractionState, Configuration config) {
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
        if (POSTag.NOUN_PROPER_SINGULAR.equals(pos) || //
                POSTag.NOUN.equals(pos) || //
                POSTag.NOUN_PROPER_PLURAL.equals(pos)) {

            textState.addNort(n, n.getText(), probability);
        }
        if (POSTag.NOUN_PLURAL.equals(pos)) {
            textState.addType(n, n.getText(), probability);
        }

    }

}
