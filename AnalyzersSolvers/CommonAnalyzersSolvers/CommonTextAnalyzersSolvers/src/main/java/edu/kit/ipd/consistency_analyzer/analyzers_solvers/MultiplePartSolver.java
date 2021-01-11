package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.consistency_analyzer.common.Utilis;
import edu.kit.ipd.consistency_analyzer.datastructures.INounMapping;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IWord;
import edu.kit.ipd.consistency_analyzer.datastructures.MappingKind;

/**
 * WORK IN PROGRESS
 *
 * @author Sophie
 *
 */

@MetaInfServices(ITextSolver.class)
public class MultiplePartSolver extends TextExtractionSolver {

	private double probability;

	/**
	 * Creates a new multiple part solver.
	 *
	 * @param graph               the PARSE graph
	 * @param textExtractionState the text extraction state
	 */
	public MultiplePartSolver(ITextExtractionState textExtractionState) {
		super(DependencyType.TEXT, textExtractionState);
		probability = GenericTextAnalyzerSolverConfig.MULTIPLE_PART_SOLVER_PROBABILITY;
	}

	public MultiplePartSolver(ITextExtractionState textExtractionState, double probability) {

		this(textExtractionState);
		this.probability = probability;

	}

	@Override
	public ITextSolver create(ITextExtractionState textExtractionState) {
		return new MultiplePartSolver(textExtractionState);
	}

	public MultiplePartSolver() {
		this(null);
	}

	@Override
	public void exec() {

		searchForName();
		searchForType();
	}

	private void searchForName() {
		for (INounMapping nameMap : textExtractionState.getNames()) {
			List<IWord> nameNodes = new ArrayList<>(nameMap.getNodes());
			for (IWord n : nameNodes) {
				IWord pre = n.getPreWord();
				if (pre != null && textExtractionState.isNodeContainedByNounMappings(pre) && !textExtractionState.isNodeContainedByTypeNodes(pre)) {
					String ref = pre.getText() + " " + n.getText();
					addTerm(ref, pre, n, MappingKind.NAME);
				}
			}
		}
	}

	private void searchForType() {
		for (INounMapping typeMap : textExtractionState.getTypes()) {
			List<IWord> typeNodes = new ArrayList<>(typeMap.getNodes());
			for (IWord n : typeNodes) {
				IWord pre = n.getPreWord();
				if (pre != null && textExtractionState.isNodeContainedByNounMappings(pre) && !textExtractionState.isNodeContainedByNameNodes(pre)) {
					String ref = pre.getText() + " " + n.getText();
					addTerm(ref, pre, n, MappingKind.TYPE);
				}
			}
		}
	}

	private void addTerm(String ref, IWord pre, IWord n, MappingKind kind) {

		List<INounMapping> preMappings = textExtractionState.getNounMappingsByNode(pre);
		List<INounMapping> nMappings = textExtractionState.getNounMappingsByNode(n);

		List<List<INounMapping>> cartesianProduct = Utilis.cartesianProduct(preMappings, List.of(nMappings));

		for (List<INounMapping> possibleCombination : cartesianProduct) {
			textExtractionState.addTerm(ref, possibleCombination.get(0), possibleCombination.get(1), kind, probability);
		}

	}
}
