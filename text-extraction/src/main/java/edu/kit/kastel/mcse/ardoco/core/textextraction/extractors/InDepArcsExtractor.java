/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.extractors;

import java.util.Map;

import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractExtractor;
import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.WordHelper;

/**
 * The analyzer examines the incoming dependency arcs of the current node.
 *
 * @author Sophie Schulz
 * @author Jan Keim
 */
public class InDepArcsExtractor extends AbstractExtractor<TextAgentData> {

    @Configurable
    private double nameOrTypeWeight = 0.5;

    @Configurable
    private double probability = 1.0;

    /**
     * Prototype constructor.
     */
    public InDepArcsExtractor() {
        // empty
    }

    @Override
    public void exec(TextAgentData data, IWord n) {
        var nodeValue = n.getText();
        if (nodeValue.length() == 1 && !Character.isLetter(nodeValue.charAt(0))) {
            return;
        }

        examineIncomingDepArcs(data.getTextState(), n);
    }

    /**
     * Examines the incoming dependency arcs from the PARSE graph.
     */
    private void examineIncomingDepArcs(ITextState textState, IWord word) {

        var incomingDepArcs = WordHelper.getIncomingDependencyTags(word);

        for (DependencyTag depTag : incomingDepArcs) {
            if (hasNortDependencies(depTag)) {
                textState.addName(word, this, probability * nameOrTypeWeight);
                textState.addType(word, this, probability * nameOrTypeWeight);
            } else if (hasTypeOrNortDependencies(depTag)) {
                if (WordHelper.hasIndirectDeterminerAsPreWord(word)) {
                    textState.addType(word, this, probability);
                }

                textState.addName(word, this, probability * nameOrTypeWeight);
                textState.addType(word, this, probability * nameOrTypeWeight);
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

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // handle delegation
    }
}
