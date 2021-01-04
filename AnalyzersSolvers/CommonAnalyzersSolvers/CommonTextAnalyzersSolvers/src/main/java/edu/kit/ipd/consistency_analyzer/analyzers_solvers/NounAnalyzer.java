package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.consistency_analyzer.datastructures.ITextExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IWord;
import edu.kit.ipd.consistency_analyzer.datastructures.PosTag;

/**
 * The analyzer classifies nouns.
 *
 * @author Sophie
 *
 */

@MetaInfServices(ITextAnalyzer.class)
public class NounAnalyzer extends TextExtractionAnalyzer {

	double probability = GenericTextAnalyzerSolverConfig.NOUN_ANALYZER_PROBABILITY;

	/**
	 * Creates a new NounAnalyzer
	 *
	 * @param graph               PARSE graph to run on
	 * @param textExtractionState the text extraction state
	 */
	public NounAnalyzer(ITextExtractionState textExtractionState) {
		super(DependencyType.TEXT, textExtractionState);
	}

	public NounAnalyzer() {
		this(null);
	}

	@Override
	public void exec(IWord n) {

		String nodeValue = n.getText();
		if (nodeValue.length() == 1 && !Character.isLetter(nodeValue.charAt(0))) {
			return;
		}

		this.findSingleNouns(n);

	}

	/**
	 * Finds all nouns and adds them as name-or-type mappings (and types) to the
	 * text extraction state.
	 *
	 * @param n node to check
	 */
	private void findSingleNouns(IWord n) {
		PosTag pos = n.getPosTag();
		if (PosTag.NNP.equals(pos) || //
				PosTag.NN.equals(pos) || //
				PosTag.NNPS.equals(pos)) {

			textExtractionState.addNort(n, n.getText(), probability);
		}
		if (PosTag.NNS.equals(pos)) {
			textExtractionState.addType(n, n.getText(), probability);
		}

	}

	@Override
	public ITextAnalyzer create(ITextExtractionState textExtractionState) {
		return new NounAnalyzer(textExtractionState);
	}

}
