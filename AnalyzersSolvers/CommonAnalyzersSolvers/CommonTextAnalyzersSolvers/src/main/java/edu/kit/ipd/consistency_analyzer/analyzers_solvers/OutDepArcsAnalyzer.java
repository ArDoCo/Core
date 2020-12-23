package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import java.util.List;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.consistency_analyzer.common.WordHelper;
import edu.kit.ipd.consistency_analyzer.datastructures.DependencyTag;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IWord;

/**
 * The analyzer examines the outgoing arcs of the current node.
 *
 * @author Sophie
 * 
 */

@MetaInfServices(ITextAnalyzer.class)
public class OutDepArcsAnalyzer extends TextExtractionAnalyzer {

	private double probability = GenericTextAnalyzerSolverConfig.OUT_DEP_ARCS_ANALYZER_PROBABILITY;

	/**
	 * Creates a new OutDepArcsAnalyzer
	 *
	 * @param graph               the current PARSE graph
	 * @param textExtractionState the text extraction state
	 */
	public OutDepArcsAnalyzer(ITextExtractionState textExtractionState) {
		super(DependencyType.TEXT, textExtractionState);
	}

	@Override
	public ITextAnalyzer create(ITextExtractionState textExtractionState) {
		return new OutDepArcsAnalyzer(textExtractionState);
	}

	public OutDepArcsAnalyzer() {
		this(null);
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

		List<DependencyTag> outgoingDepArcs = WordHelper.getOutgoingDependencyTags(n);

		for (DependencyTag shortDepTag : outgoingDepArcs) {

			if (shortDepTag.equals(DependencyTag.AGENT)) {
				textExtractionState.addNort(n, n.getText(), probability);

			} else if (shortDepTag.equals(DependencyTag.NUM)) {
				textExtractionState.addType(n, n.getText(), probability);

			} else if (shortDepTag.equals(DependencyTag.PREDET)) {
				textExtractionState.addType(n, n.getText(), probability);

			} else if (shortDepTag.equals(DependencyTag.RCMOD)) {
				textExtractionState.addNort(n, n.getText(), probability);
			}
		}
	}
}
