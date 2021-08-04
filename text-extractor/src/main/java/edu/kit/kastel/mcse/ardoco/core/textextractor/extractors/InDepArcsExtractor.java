package edu.kit.kastel.mcse.ardoco.core.textextractor.extractors;

import org.eclipse.collections.api.list.ImmutableList;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.common.WordHelper;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.TextExtractor;
import edu.kit.kastel.mcse.ardoco.core.textextractor.GenericTextConfig;

/**
 * The analyzer examines the incoming dependency arcs of the current node.
 *
 * @author Sophie
 *
 */
@MetaInfServices(TextExtractor.class)
public class InDepArcsExtractor extends TextExtractor {

    private double probability;

    /**
     * Prototype constructor.
     */
    public InDepArcsExtractor() {
        this(null);
    }

    /**
     * Instantiates a new in dep arcs extractor.
     *
     * @param textExtractionState the text extraction state
     */
    public InDepArcsExtractor(ITextState textExtractionState) {
        this(textExtractionState, GenericTextConfig.DEFAULT_CONFIG);
    }

    /**
     * Creates a new InDepArcsAnalyzer.
     *
     * @param textExtractionState the text extraction state
     * @param config              the module configuration
     */
    public InDepArcsExtractor(ITextState textExtractionState, GenericTextConfig config) {
        super(textExtractionState);
        probability = config.inDepArcsAnalyzerProbability;
    }

    @Override
    public TextExtractor create(ITextState textExtractionState, Configuration config) {
        return new InDepArcsExtractor(textExtractionState, (GenericTextConfig) config);
    }

    @Override
    public void exec(IWord n) {
        String nodeValue = n.getText();
        if (nodeValue.length() == 1 && !Character.isLetter(nodeValue.charAt(0))) {
            return;
        }

        examineIncomingDepArcs(n);
    }

    /**
     * Examines the incoming dependency arcs from the PARSE graph.
     *
     * @param n the node to check
     */
    private void examineIncomingDepArcs(IWord n) {

        ImmutableList<DependencyTag> incomingDepArcs = WordHelper.getIncomingDependencyTags(n);

        for (DependencyTag depTag : incomingDepArcs) {

            if (hasNortDependencies(depTag)) {
                textState.addNort(n, n.getText(), probability);
            } else if (hasTypeOrNortDependencies(depTag)) {
                if (WordHelper.hasIndirectDeterminerAsPreWord(n)) {
                    textState.addType(n, n.getText(), probability);
                }

                textState.addNort(n, n.getText(), probability);
            }
        }

    }

    private boolean hasTypeOrNortDependencies(DependencyTag depTag) {
        return DependencyTag.OBJ == depTag || DependencyTag.IOBJ == depTag || DependencyTag.NMOD == depTag || DependencyTag.NSUBJPASS == depTag
                || DependencyTag.POBJ == depTag;
    }

    private boolean hasNortDependencies(DependencyTag depTag) {
        return DependencyTag.APPOS == depTag || DependencyTag.NSUBJ == depTag || DependencyTag.POSS == depTag;
    }

}
