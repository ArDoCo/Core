/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.extractors;

import org.eclipse.collections.api.list.ImmutableList;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.common.util.WordHelper;
import edu.kit.kastel.mcse.ardoco.core.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.textextraction.GenericTextConfig;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtractionExtractor;

/**
 * The analyzer examines the outgoing arcs of the current node.
 *
 * @author Sophie
 *
 */

@MetaInfServices(TextExtractionExtractor.class)
public class OutDepArcsExtractor extends TextExtractionExtractor {

    private double probability;

    /**
     * Prototype constructor.
     */
    public OutDepArcsExtractor() {
        this(null);
    }

    /**
     * Instantiates a new out dep arcs extractor.
     *
     * @param textExtractionState the text extraction state
     */
    public OutDepArcsExtractor(ITextState textExtractionState) {
        this(textExtractionState, GenericTextConfig.DEFAULT_CONFIG);
    }

    /**
     * Creates a new OutDepArcsAnalyzer.
     *
     * @param textExtractionState the text extraction state
     * @param config              the module configuration
     */
    public OutDepArcsExtractor(ITextState textExtractionState, GenericTextConfig config) {
        super(textExtractionState);
        probability = config.outDepArcsAnalyzerProbability;
    }

    @Override
    public TextExtractionExtractor create(ITextState textExtractionState, Configuration config) {
        return new OutDepArcsExtractor(textExtractionState, (GenericTextConfig) config);
    }

    @Override
    public void exec(IWord n) {

        String nodeValue = n.getText();
        if (nodeValue.length() == 1 && !Character.isLetter(nodeValue.charAt(0))) {
            return;
        }
        examineOutgoingDepArcs(n);
    }

    /**
     * Examines the outgoing dependencies of a node.
     *
     * @param word the word to check
     */
    private void examineOutgoingDepArcs(IWord word) {

        ImmutableList<DependencyTag> outgoingDepArcs = WordHelper.getOutgoingDependencyTags(word);

        for (DependencyTag shortDepTag : outgoingDepArcs) {

            if (DependencyTag.AGENT == shortDepTag) {
                textState.addNort(word, probability);

            } else if (DependencyTag.NUM == shortDepTag) {
                textState.addType(word, probability);

            } else if (DependencyTag.PREDET == shortDepTag) {
                textState.addType(word, probability);

            } else if (DependencyTag.RCMOD == shortDepTag) {
                textState.addNort(word, probability);
            }
        }
    }
}
