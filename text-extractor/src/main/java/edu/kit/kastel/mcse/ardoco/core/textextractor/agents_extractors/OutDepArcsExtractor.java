package edu.kit.kastel.mcse.ardoco.core.textextractor.agents_extractors;

import java.util.List;

import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyType;
import edu.kit.kastel.mcse.ardoco.core.datastructures.common.WordHelper;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.TextExtractor;

/**
 * The analyzer examines the outgoing arcs of the current node.
 *
 * @author Sophie
 *
 */

@MetaInfServices(TextExtractor.class)
public class OutDepArcsExtractor extends TextExtractor {

	private double probability;

	public OutDepArcsExtractor() {
		this(null);
	}

	public OutDepArcsExtractor(ITextState textExtractionState) {
		this(textExtractionState, GenericTextConfig.DEFAULT_CONFIG);
	}

	/**
	 * Creates a new OutDepArcsAnalyzer
	 *
	 * @param textExtractionState the text extraction state
	 * @param config              the module configuration
	 */
	public OutDepArcsExtractor(ITextState textExtractionState, GenericTextConfig config) {
		super(DependencyType.TEXT, textExtractionState);
		probability = config.outDepArcsAnalyzerProbability;
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
		return new OutDepArcsExtractor(textExtractionState, (GenericTextConfig) config);
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
				textState.addNort(n, n.getText(), probability);

			} else if (shortDepTag.equals(DependencyTag.NUM)) {
				textState.addType(n, n.getText(), probability);

			} else if (shortDepTag.equals(DependencyTag.PREDET)) {
				textState.addType(n, n.getText(), probability);

			} else if (shortDepTag.equals(DependencyTag.RCMOD)) {
				textState.addNort(n, n.getText(), probability);
			}
		}
	}
}
