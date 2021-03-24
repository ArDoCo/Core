package edu.kit.ipd.consistency_analyzer.agents_extractors;

import java.util.List;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.DependencyType;
import edu.kit.ipd.consistency_analyzer.agents_extractors.extractors.TextExtractor;
import edu.kit.ipd.consistency_analyzer.common.WordHelper;
import edu.kit.ipd.consistency_analyzer.datastructures.DependencyTag;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextState;
import edu.kit.ipd.consistency_analyzer.datastructures.IWord;

/**
 * The analyzer examines the incoming dependency arcs of the current node.
 *
 * @author Sophie
 *
 */

@MetaInfServices(TextExtractor.class)
public class InDepArcsExtractor extends TextExtractor {

	private double probability;

	@Override
	public TextExtractor create(ITextState textExtractionState) {
		return new InDepArcsExtractor(textExtractionState);
	}

	@Override
	public void setProbability(List<Double> probabilities) {
		if (probabilities.size() > 1) {
			throw new IllegalArgumentException(getName() + ": The given probabilities are more than needed!");
		} else if (probabilities.isEmpty()) {
			throw new IllegalArgumentException(getName() + ": The given probabilities are empty!");
		} else {
			probability = probabilities.get(0);
		}
	}

	public InDepArcsExtractor() {
		this(null);
	}

	public InDepArcsExtractor(ITextState textExtractionState) {
		this(null, GenericTextConfig.DEFAULT_CONFIG);
	}

	/**
	 * Creates a new InDepArcsAnalyzer
	 *
	 * @param textExtractionState the text extraction state
	 * @param config              the module configuration
	 */
	public InDepArcsExtractor(ITextState textExtractionState, GenericTextConfig config) {
		super(DependencyType.TEXT, textExtractionState);
		this.probability = config.inDepArcsAnalyzerProbability;
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
				textState.addNort(n, n.getText(), probability);
			} else if (DependencyTag.DOBJ.equals(depTag) || DependencyTag.IOBJ.equals(depTag) || DependencyTag.NMOD.equals(depTag) || DependencyTag.NSUBJPASS.equals(depTag)
					|| DependencyTag.POBJ.equals(depTag)) {
				if (WordHelper.hasIndirectDeterminerAsPreWord(n)) {
					textState.addType(n, n.getText(), probability);
				}

				textState.addNort(n, n.getText(), probability);
			}
		}

	}

}
