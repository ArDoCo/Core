package edu.kit.kastel.mcse.ardoco.core.textextractor.extractors;

import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.common.WordHelper;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.TextExtractor;
import edu.kit.kastel.mcse.ardoco.core.textextractor.GenericTextConfig;

/**
 * This analyzer finds patterns like article type name or article name type.
 *
 * @author Sophie
 *
 */
@MetaInfServices(TextExtractor.class)
public class ArticleTypeNameExtractor extends TextExtractor {

    private double probability;

    @Override
    public TextExtractor create(ITextState textState, Configuration config) {
        return new ArticleTypeNameExtractor(textState, (GenericTextConfig) config);
    }

    /**
     * Prototype constructor.
     */
    public ArticleTypeNameExtractor() {
        this(null);
    }

    /**
     * Instantiates a new article type name extractor.
     *
     * @param textState the text state
     */
    public ArticleTypeNameExtractor(ITextState textState) {
        this(textState, GenericTextConfig.DEFAULT_CONFIG);
    }

    /**
     * Creates a new article type name analyzer.
     *
     * @param textState the text extraction state
     * @param config    the module configuration
     */
    public ArticleTypeNameExtractor(ITextState textState, GenericTextConfig config) {
        super(textState);
        probability = config.articleTypeNameAnalyzerProbability;
    }

    @Override
    public void exec(IWord n) {

        if (!checkIfNodeIsName(n)) {
            checkIfNodeIsType(n);
        }
    }

    /**
     * If the current node is contained by name-or-type mappings, the previous node is contained by type nodes and the
     * preprevious an article the node is added as a name mapping.
     *
     * @param n node to check
     */
    private boolean checkIfNodeIsName(IWord n) {
        if (textState.isNodeContainedByNameOrTypeNodes(n)) {

            IWord prevNode = n.getPreWord();
            if (prevNode != null && textState.isNodeContainedByTypeNodes(prevNode) && WordHelper.hasDeterminerAsPreWord(prevNode)) {

                textState.addName(n, n.getText(), probability);
                return true;

            }
        }
        return false;
    }

    /**
     * If the current node is contained by name-or-type mappings, the previous node is contained by name nodes and the
     * preprevious an article the node is added as a type mapping.
     *
     * @param n node to check
     */
    private boolean checkIfNodeIsType(IWord n) {
        if (textState.isNodeContainedByNameOrTypeNodes(n)) {

            IWord prevNode = n.getPreWord();
            if (prevNode != null && textState.isNodeContainedByNameNodes(prevNode) && WordHelper.hasDeterminerAsPreWord(prevNode)) {

                textState.addType(n, n.getText(), probability);
                return true;
            }

        }
        return false;

    }
}
