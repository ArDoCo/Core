package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.consistency_analyzer.common.SimilarityUtils;
import edu.kit.ipd.consistency_analyzer.common.Utilis;
import edu.kit.ipd.consistency_analyzer.datastructures.IModelExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.INounMapping;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendationState;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendedInstance;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextExtractionState;

/**
 * The separated relation solver is a solver for the creation of recommended
 * relations. Whenever a recommended instance owes an occurrence with a
 * separator a separator relation is recommended.
 *
 * @author Sophie
 * 
 */
@MetaInfServices(IRecommendationSolver.class)
public class SeparatedRelationsSolver extends RecommendationSolver {

	private double probability = GenericRecommendationAnalyzerSolverConfig.SEPARATED_RELATIONS_SOLVER_PROBABILITY;
	private String relName = "separated";

	/**
	 * Creates a new SeparatedRelationsSolver
	 *
	 * @param graph                the PARSE graph
	 * @param textExtractionState  the text extraction state
	 * @param modelExtractionState the model extraction state
	 * @param recommendationState  the recommendation state
	 */
	public SeparatedRelationsSolver(ITextExtractionState textExtractionState, IModelExtractionState modelExtractionState, IRecommendationState recommendationState) {
		super(DependencyType.RECOMMENDATION, textExtractionState, modelExtractionState, recommendationState);
	}

	public SeparatedRelationsSolver() {
		this(null, null, null);
	}

	@Override
	public IRecommendationSolver create(ITextExtractionState textExtractionState, IModelExtractionState modelExtractionState, IRecommendationState recommendationState) {
		return new SeparatedRelationsSolver(textExtractionState, modelExtractionState, recommendationState);
	}

	/**
	 * Executes the solver
	 */
	@Override
	public void exec() {

		addRelations();
	}

	/**
	 * Searches for recommended instances with a separator in one of their
	 * occurrences. For each part of the splitted occurrences different matchings
	 * for the end points of the relation are possible. Therefore, the carthesian
	 * product is created. For each combination a possible recommendation is
	 * created.
	 */
	private void addRelations() {
		for (IRecommendedInstance ri : recommendationState.getRecommendedInstances()) {
			String siName = ri.getName();

			List<String> occs = new ArrayList<>();
			for (INounMapping nnm : ri.getNameMappings()) {
				occs.addAll(nnm.getOccurrences());
			}
			List<String> occsWithSeparator = occs.stream().filter(SimilarityUtils::containsSeparator).collect(Collectors.toList());
			if (occsWithSeparator.isEmpty()) {
				continue;
			} else {
				for (String occ : occsWithSeparator) {
					occ = SimilarityUtils.splitAtSeparators(occ);
					List<String> occParts = new ArrayList<>(List.of(occ.split(" ")));

					List<String> similarParts = new ArrayList<>();
					List<Integer> similarPositions = new ArrayList<>();
					for (int i = 0; i < occParts.size(); i++) {
						String part = occParts.get(i);
						if (SimilarityUtils.areWordsSimilar(part, siName)) {
							similarParts.add(part);
							similarPositions.add(i);
						}
					}

					int positionOfRi = -1;

					if (similarPositions.size() == 1) {
						positionOfRi = similarPositions.get(0);
						occParts.removeAll(similarParts);
					}

					List<List<IRecommendedInstance>> riPossibilities = new ArrayList<>();

					for (String occPart : occParts) {
						List<IRecommendedInstance> ris = this.recommendationState.getRecommendedInstancesBySimilarName(occPart);
						if (!ris.isEmpty()) {
							riPossibilities.add(this.recommendationState.getRecommendedInstancesBySimilarName(occPart));
						}
					}

					if (positionOfRi >= 0 && riPossibilities.size() == occParts.size()) {
						riPossibilities.add(positionOfRi, List.of(ri));
					}

					if (riPossibilities.size() < 2) {
						break;
					} else {
						List<List<IRecommendedInstance>> allRelationProbabilities = Utilis.cartesianProduct(new ArrayList<>(), riPossibilities);

						for (List<IRecommendedInstance> possibility : allRelationProbabilities) {

							IRecommendedInstance r1 = possibility.get(0);
							IRecommendedInstance r2 = possibility.get(1);
							possibility.remove(r1);
							possibility.remove(r2);

							this.recommendationState.addRecommendedRelation(relName, r1, r2, possibility, probability, new ArrayList<>());

						}
					}

				}
			}

		}
	}

}
