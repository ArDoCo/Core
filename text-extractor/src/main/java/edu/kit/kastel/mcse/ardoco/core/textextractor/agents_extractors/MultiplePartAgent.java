package edu.kit.kastel.mcse.ardoco.core.textextractor.agents_extractors;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyType;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.TextAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.util.Utilis;

/**
 * WORK IN PROGRESS
 *
 * @author Sophie
 *
 */

@MetaInfServices(TextAgent.class)
public class MultiplePartAgent extends TextAgent {

	private double probability;

	public MultiplePartAgent() {
		super(GenericTextConfig.class);
	}

	/**
	 * Creates a new multiple part solver.
	 *
	 * @param text      the text
	 * @param textState the text extraction state
	 * @param config    the module configuration
	 */
	private MultiplePartAgent(IText text, ITextState textState, GenericTextConfig config) {
		super(DependencyType.TEXT, GenericTextConfig.class, text, textState);
		probability = config.multiplePartSolverProbability;
	}

	@Override
	public TextAgent create(IText text, ITextState textExtractionState, Configuration config) {
		return new MultiplePartAgent(text, textExtractionState, (GenericTextConfig) config);
	}

	@Override
	public void exec() {
		searchForName();
		searchForType();
	}

	private void searchForName() {
		for (INounMapping nameMap : textState.getNames()) {
			List<IWord> nameNodes = new ArrayList<>(nameMap.getWords());
			for (IWord n : nameNodes) {
				IWord pre = n.getPreWord();
				if (pre != null && textState.isNodeContainedByNounMappings(pre) && !textState.isNodeContainedByTypeNodes(pre)) {
					String ref = pre.getText() + " " + n.getText();
					addTerm(ref, pre, n, MappingKind.NAME);
				}
			}
		}
	}

	private void searchForType() {
		for (INounMapping typeMap : textState.getTypes()) {
			List<IWord> typeNodes = new ArrayList<>(typeMap.getWords());
			for (IWord n : typeNodes) {
				IWord pre = n.getPreWord();
				if (pre != null && textState.isNodeContainedByNounMappings(pre) && !textState.isNodeContainedByNameNodes(pre)) {
					String ref = pre.getText() + " " + n.getText();
					addTerm(ref, pre, n, MappingKind.TYPE);
				}
			}
		}
	}

	private void addTerm(String ref, IWord pre, IWord n, MappingKind kind) {

		List<INounMapping> preMappings = textState.getNounMappingsByNode(pre);
		List<INounMapping> nMappings = textState.getNounMappingsByNode(n);

		List<List<INounMapping>> cartesianProduct = Utilis.cartesianProduct(preMappings, List.of(nMappings));

		for (List<INounMapping> possibleCombination : cartesianProduct) {
			textState.addTerm(ref, possibleCombination.get(0), possibleCombination.get(1), kind, probability);
		}

	}
}
