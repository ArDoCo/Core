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
 * The analyzer examines the incoming dependency arcs of the current node.
 *
 * @author Sophie
 *
 */
@MetaInfServices(TextExtractionExtractor.class)
public class InDepArcsExtractor extends TextExtractionExtractor {

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
    public TextExtractionExtractor create(ITextState textExtractionState, Configuration config) {
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

    private static boolean hasTypeOrNortDependencies(DependencyTag depTag) {
        var hasObjectDependencies = DependencyTag.OBJ == depTag || DependencyTag.IOBJ == depTag || DependencyTag.POBJ == depTag;
        return hasObjectDependencies || DependencyTag.NMOD == depTag || DependencyTag.NSUBJPASS == depTag;
    }

    private static boolean hasNortDependencies(DependencyTag depTag) {
        return DependencyTag.APPOS == depTag || DependencyTag.NSUBJ == depTag || DependencyTag.POSS == depTag;
    }

}
