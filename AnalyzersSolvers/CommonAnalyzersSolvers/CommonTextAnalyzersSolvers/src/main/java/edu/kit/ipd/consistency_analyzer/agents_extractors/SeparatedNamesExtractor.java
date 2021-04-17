package edu.kit.ipd.consistency_analyzer.agents_extractors;

import java.util.List;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.Configuration;
import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.DependencyType;
import edu.kit.ipd.consistency_analyzer.agents_extractors.extractors.TextExtractor;
import edu.kit.ipd.consistency_analyzer.common.SimilarityUtils;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextState;
import edu.kit.ipd.consistency_analyzer.datastructures.IWord;

/**
 * This analyzer classifies all nodes, containing separators, as names and adds them as mappings to the current text
 * extraction state.
 *
 * @author Sophie
 *
 */

@MetaInfServices(TextExtractor.class)
public class SeparatedNamesExtractor extends TextExtractor {

    private double probability;

    public SeparatedNamesExtractor() {
        this(null);
    }

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
        super(DependencyType.TEXT, textExtractionState);
        probability = config.separatedNamesAnalyzerProbability;
    }

    @Override
    public void setProbability(List<Double> probabilities) {
        if (probabilities.size() > 1) {
            throw new IllegalArgumentException(getName() + ": The given probabilities are more than needed!");
        } else if (probabilities.isEmpty()) {
            throw new IllegalArgumentException(getName() + ": The given probabilities are empty!");
        } else {
            probability = probabilities.get(0);
        }
    }

    @Override
    public TextExtractor create(ITextState textExtractionState, Configuration config) {
        return new SeparatedNamesExtractor(textExtractionState, (GenericTextConfig) config);
    }

    /***
     * Checks if Node Value contains separator. If true, it is splitted and added separately to the names of the text
     * extraction state.
     */
    @Override
    public void exec(IWord node) {
        checkForSeparatedNode(node);
    }

    /***
     * Checks if Node Value contains separator. If true, it is splitted and added separately to the names of the text
     * extraction state.
     *
     * @param n node to check
     */
    private void checkForSeparatedNode(IWord n) {
        if (SimilarityUtils.containsSeparator(n.getText())) {
            textState.addName(n, n.getText(), probability);
        }
    }

}
