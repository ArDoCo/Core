package edu.kit.ipd.consistency_analyzer.agents_extractors;

import java.util.List;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.DependencyType;
import edu.kit.ipd.consistency_analyzer.agents_extractors.extractors.TextExtractor;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextState;
import edu.kit.ipd.consistency_analyzer.datastructures.IWord;
import edu.kit.ipd.consistency_analyzer.datastructures.PosTag;

/**
 * The analyzer classifies nouns.
 *
 * @author Sophie
 *
 */

@MetaInfServices(TextExtractor.class)
public class NounExtractor extends TextExtractor {

    double probability = GenericTextConfig.NOUN_ANALYZER_PROBABILITY;

    /**
     * Creates a new NounAnalyzer
     *
     * @param graph               PARSE graph to run on
     * @param textExtractionState the text extraction state
     */
    public NounExtractor(ITextState textExtractionState) {
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

    public NounExtractor() {
        this(null);
    }

    @Override
    public TextExtractor create(ITextState textExtractionState) {
        return new NounExtractor(textExtractionState);
    }

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
        PosTag pos = n.getPosTag();
        if (PosTag.NNP.equals(pos) || //
                PosTag.NN.equals(pos) || //
                PosTag.NNPS.equals(pos)) {

            textState.addNort(n, n.getText(), probability);
        }
        if (PosTag.NNS.equals(pos)) {
            textState.addType(n, n.getText(), probability);
        }

    }

}
