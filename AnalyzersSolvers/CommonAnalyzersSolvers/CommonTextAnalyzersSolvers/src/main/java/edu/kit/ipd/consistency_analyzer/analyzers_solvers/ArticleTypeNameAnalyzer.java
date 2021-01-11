package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.consistency_analyzer.common.WordHelper;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IWord;

/**
 * This analyzer finds patterns like article type name or article name type.
 *
 * @author Sophie
 *
 */
@MetaInfServices(ITextAnalyzer.class)
public class ArticleTypeNameAnalyzer extends TextExtractionAnalyzer {

	private double probability = GenericTextAnalyzerSolverConfig.ARTICLE_TYPE_NAME_ANALYZER_PROBABILITY;

	@Override
	public ITextAnalyzer create(ITextExtractionState textExtractionState) {
		return new ArticleTypeNameAnalyzer(textExtractionState);
	}

	public ITextAnalyzer create(ITextExtractionState textExtractionState, double probability) {
		ArticleTypeNameAnalyzer analyzer = new ArticleTypeNameAnalyzer(textExtractionState);
		analyzer.probability = probability;
		return analyzer;
	}

	public ArticleTypeNameAnalyzer() {
		this(null);
	}

	/**
	 * Creates a new article type name analyzer.
	 *
	 * @param graph
	 * @param textExtractionState the text extraction state
	 */
	public ArticleTypeNameAnalyzer(ITextExtractionState textExtractionState) {
		super(DependencyType.TEXT, textExtractionState);
	}

	@Override
	public void exec(IWord n) {

		if (!checkIfNodeIsName(n)) {
			checkIfNodeIsType(n);
		}
	}

	/**
	 * If the current node is contained by name-or-type mappings, the previous node
	 * is contained by type nodes and the preprevious an article the node is added
	 * as a name mapping.
	 *
	 * @param n node to check
	 */
	private boolean checkIfNodeIsName(IWord n) {
		if (textExtractionState.isNodeContainedByNameOrTypeNodes(n)) {

			IWord prevNode = n.getPreWord();
			if (prevNode != null && textExtractionState.isNodeContainedByTypeNodes(prevNode) && WordHelper.hasDeterminerAsPreWord(prevNode)) {

				textExtractionState.addName(n, n.getText(), probability);
				return true;

			}
		}
		return false;
	}

	/**
	 * If the current node is contained by name-or-type mappings, the previous node
	 * is contained by name nodes and the preprevious an article the node is added
	 * as a type mapping.
	 *
	 * @param n node to check
	 */
	private boolean checkIfNodeIsType(IWord n) {
		if (textExtractionState.isNodeContainedByNameOrTypeNodes(n)) {

			IWord prevNode = n.getPreWord();
			if (prevNode != null && textExtractionState.isNodeContainedByNameNodes(prevNode) && WordHelper.hasDeterminerAsPreWord(prevNode)) {

				textExtractionState.addType(n, n.getText(), probability);
				return true;
			}

		}
		return false;

	}
}
