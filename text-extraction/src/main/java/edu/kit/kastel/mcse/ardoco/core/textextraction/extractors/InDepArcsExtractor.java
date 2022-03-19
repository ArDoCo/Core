/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.extractors;

import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractExtractor;
import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.common.Configurable;
import edu.kit.kastel.mcse.ardoco.core.common.util.WordHelper;

/**
 * The analyzer examines the incoming dependency arcs of the current node.
 *
 * @author Sophie
 */
public class InDepArcsExtractor extends AbstractExtractor<TextAgentData> {

    @Configurable
    private double probability = 1.0;

    /**
     * Prototype constructor.
     */
    public InDepArcsExtractor() {
    }

    @Override
    public void exec(TextAgentData data, IWord n) {
        String nodeValue = n.getText();
        if (nodeValue.length() == 1 && !Character.isLetter(nodeValue.charAt(0))) {
            return;
        }

        examineIncomingDepArcs(data.getTextState(), n);
    }

    /**
     * Examines the incoming dependency arcs from the PARSE graph.
     *
     * @param textState
     * @param word      the node to check
     */
    private void examineIncomingDepArcs(ITextState textState, IWord word) {

        ImmutableList<DependencyTag> incomingDepArcs = WordHelper.getIncomingDependencyTags(word);

        for (DependencyTag depTag : incomingDepArcs) {

            if (hasNortDependencies(depTag)) {
                textState.addNort(word, probability);
            } else if (hasTypeOrNortDependencies(depTag)) {
                if (WordHelper.hasIndirectDeterminerAsPreWord(word)) {
                    textState.addType(word, probability);
                }

                textState.addNort(word, probability);
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
    }
}
