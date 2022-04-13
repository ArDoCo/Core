/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.extractors;

import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractExtractor;
import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.common.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.WordHelper;

/**
 * This analyzer finds patterns like article type name or article name type.
 *
 * @author Sophie, Jan
 */
//TODO make operation again
// enable getting NORTs (prob for name = prob for type) and then perform these heuristics
@Deprecated(since = "Currently Not Operational")
public class ArticleTypeNameExtractor extends AbstractExtractor<TextAgentData> {
    @Configurable
    private boolean enabled = false;

    @Configurable
    private double probability = 1.0;

    /**
     * Prototype constructor.
     */
    public ArticleTypeNameExtractor() {
        // empty
    }

    @Override
    public void exec(TextAgentData data, IWord n) {
        if (!enabled) {
            return;
        }
        if (!checkIfNodeIsName(data.getTextState(), n)) {
            checkIfNodeIsType(data.getTextState(), n);
        }
    }

    /**
     * If the current node is contained by either kind of NounMapping, the previous node is contained by type nodes and
     * the preprevious an article. The node is then set as a name mapping.
     *
     * @param word word to check
     */
    private boolean checkIfNodeIsName(ITextState textState, IWord word) {
        if (textState.isWordContainedByNameOrTypeMapping(word)) {
            var prevNode = word.getPreWord();
            if (prevNode != null && textState.isWordContainedByTypeMapping(prevNode) && WordHelper.hasDeterminerAsPreWord(prevNode)) {
                textState.addName(word, this, probability);
                return true;
            }
        }
        return false;
    }

    /**
     * If the current node is contained by either kind of NounMapping, the previous node is contained by name nodes and
     * the preprevious an article the node is added as a type mapping.
     *
     * @param word word to check
     */
    private void checkIfNodeIsType(ITextState textState, IWord word) {
        if (textState.isWordContainedByNameOrTypeMapping(word)) {
            var prevNode = word.getPreWord();
            if (prevNode != null && textState.isWordContainedByNameMapping(prevNode) && WordHelper.hasDeterminerAsPreWord(prevNode)) {
                textState.addType(word, this, probability);
            }
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // handle additional configuration
    }
}
