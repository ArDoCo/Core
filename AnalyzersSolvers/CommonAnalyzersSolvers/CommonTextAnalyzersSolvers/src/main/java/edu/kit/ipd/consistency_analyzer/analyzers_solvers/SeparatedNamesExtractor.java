package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import java.util.List;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.consistency_analyzer.agents.DependencyType;
import edu.kit.ipd.consistency_analyzer.common.SimilarityUtils;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextState;
import edu.kit.ipd.consistency_analyzer.datastructures.IWord;
import edu.kit.ipd.consistency_analyzer.extractors.TextExtractor;

/**
 * This analyzer classifies all nodes, containing separators, as names and adds them as mappings to the current text
 * extraction state.
 *
 * @author Sophie
 *
 */

@MetaInfServices(TextExtractor.class)
public class SeparatedNamesExtractor extends TextExtractor {

    double probability = GenericTextConfig.SEPARATED_NAMES_ANALYZER_PROBABILITY;

    /**
     * Creates a new SeparatedNamesIdentifier.
     *
     * @param graph               current PARSE graph
     * @param textExtractionState the text extraction state
     */
    public SeparatedNamesExtractor(ITextState textExtractionState) {
        super(DependencyType.TEXT, textExtractionState);
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
    public TextExtractor create(ITextState textExtractionState) {
        return new SeparatedNamesExtractor(textExtractionState);
    }

    public SeparatedNamesExtractor() {
        this(null);
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
