/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.extractors;

import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractExtractor;
import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.common.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.WordHelper;

/**
 * The analyzer examines the outgoing arcs of the current node.
 *
 * @author Sophie
 */

public class OutDepArcsExtractor extends AbstractExtractor<TextAgentData> {

    @Configurable
    private double probability = 0.8;

    /**
     * Prototype constructor.
     */
    public OutDepArcsExtractor() {
    }

    @Override
    public void exec(TextAgentData data, IWord n) {

        String nodeValue = n.getText();
        if (nodeValue.length() == 1 && !Character.isLetter(nodeValue.charAt(0))) {
            return;
        }
        examineOutgoingDepArcs(data.getTextState(), n);
    }

    /**
     * Examines the outgoing dependencies of a node.
     */
    private void examineOutgoingDepArcs(ITextState textState, IWord word) {

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

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
    }
}
