package edu.kit.kastel.mcse.ardoco.core.textextractor.extractors;

import org.eclipse.collections.api.list.ImmutableList;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.TextExtractor;
import edu.kit.kastel.mcse.ardoco.core.textextractor.GenericTextConfig;
import edu.kit.kastel.mcse.ardoco.core.util.WordHelper;

/**
 * The analyzer examines the outgoing arcs of the current node.
 *
 * @author Sophie
 *
 */

@MetaInfServices(TextExtractor.class)
public class OutDepArcsExtractor extends TextExtractor {

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
    public TextExtractor create(ITextState textExtractionState, Configuration config) {
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
     * @param n the node to check
     */
    private void examineOutgoingDepArcs(IWord n) {

        ImmutableList<DependencyTag> outgoingDepArcs = WordHelper.getOutgoingDependencyTags(n);

        for (DependencyTag shortDepTag : outgoingDepArcs) {

            if (DependencyTag.AGENT == shortDepTag) {
                textState.addNort(n, n.getText(), probability);

            } else if (DependencyTag.NUM == shortDepTag) {
                textState.addType(n, n.getText(), probability);

            } else if (DependencyTag.PREDET == shortDepTag) {
                textState.addType(n, n.getText(), probability);

            } else if (DependencyTag.RCMOD == shortDepTag) {
                textState.addNort(n, n.getText(), probability);
            }
        }
    }
}
