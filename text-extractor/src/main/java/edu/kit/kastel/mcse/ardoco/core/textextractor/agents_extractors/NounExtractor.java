package edu.kit.kastel.mcse.ardoco.core.textextractor.agents_extractors;

import java.util.List;

import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyType;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.PosTag;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.TextExtractor;

/**
 * The analyzer classifies nouns.
 *
 * @author Sophie
 *
 */

@MetaInfServices(TextExtractor.class)
public class NounExtractor extends TextExtractor {

	private double probability;

	public NounExtractor() {
		this(null);
	}

	public NounExtractor(ITextState textExtractionState) {
		this(textExtractionState, GenericTextConfig.DEFAULT_CONFIG);
	}

	/**
	 * Creates a new NounAnalyzer
	 *
	 * @param graph               PARSE graph to run on
	 * @param textExtractionState the text extraction state
	 * @param config              the module configuration
	 */
	public NounExtractor(ITextState textExtractionState, GenericTextConfig config) {
		super(DependencyType.TEXT, textExtractionState);
		probability = config.nounAnalyzerProbability;
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

	@Override
	public TextExtractor create(ITextState textExtractionState, Configuration config) {
		return new NounExtractor(textExtractionState, (GenericTextConfig) config);
	}

	@Override
	public void exec(IWord n) {

		String nodeValue = n.getText();
		if (nodeValue.length() == 1 && !Character.isLetter(nodeValue.charAt(0))) {
			return;
		}

		findSingleNouns(n);

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

			textState.addNort(n, n.getText(), probability);
		}
		if (PosTag.NNS.equals(pos)) {
			textState.addType(n, n.getText(), probability);
		}

	}

}
