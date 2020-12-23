package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import java.util.List;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.consistency_analyzer.common.WordHelper;
import edu.kit.ipd.consistency_analyzer.datastructures.DependencyTag;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IWord;

/**
 * The analyzer examines the incoming dependency arcs of the current node.
 *
 * @author Sophie
 *
 */

@MetaInfServices(ITextAnalyzer.class)
public class InDepArcsAnalyzer extends TextExtractionAnalyzer {

	private double probability = GenericTextAnalyzerSolverConfig.IN_DEP_ARCS_ANALYZER_PROBABILITY;

	@Override
	public ITextAnalyzer create(ITextExtractionState textExtractionState) {
		return new InDepArcsAnalyzer(textExtractionState);
	}

	public InDepArcsAnalyzer() {
		this(null);
	}

	/**
	 * Creates a new InDepArcsAnalyzer
	 *
	 * @param graph               the PARSE graph
	 * @param textExtractionState the text extraction state
	 */
	public InDepArcsAnalyzer(ITextExtractionState textExtractionState) {
		super(DependencyType.TEXT, textExtractionState);
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

		List<DependencyTag> incomingDepArcs = WordHelper.getIncomingDependencyTags(n);

		for (DependencyTag depTag : incomingDepArcs) {

			if (DependencyTag.APPOS.equals(depTag) || DependencyTag.NSUBJ.equals(depTag) || DependencyTag.POSS.equals(depTag)) {
				textExtractionState.addNort(n, n.getText(), probability);
			} else if (DependencyTag.DOBJ.equals(depTag) || DependencyTag.IOBJ.equals(depTag) || DependencyTag.NMOD.equals(depTag) || DependencyTag.NSUBJPASS.equals(depTag)
					|| DependencyTag.POBJ.equals(depTag)) {
				if (WordHelper.hasIndirectDeterminerAsPreWord(n)) {
					textExtractionState.addType(n, n.getText(), probability);
				}

				textExtractionState.addNort(n, n.getText(), probability);
			}
		}

	}

}
