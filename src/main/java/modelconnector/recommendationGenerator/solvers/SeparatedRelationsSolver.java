package modelconnector.recommendationGenerator.solvers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.kit.ipd.parse.luna.graph.IGraph;
import modelconnector.DependencyType;
import modelconnector.helpers.ModelConnectorConfiguration;
import modelconnector.helpers.SimilarityUtils;
import modelconnector.helpers.Utilis;
import modelconnector.modelExtractor.state.ModelExtractionState;
import modelconnector.recommendationGenerator.state.RecommendationState;
import modelconnector.recommendationGenerator.state.RecommendedInstance;
import modelconnector.textExtractor.state.NounMapping;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * The separated relation solver is a solver for the creation of recommended
 * relations. Whenever a recommended instance owes an occurrence with a
 * separator a separator relation is recommended.
 *
 * @author Sophie
 *
 */
public class SeparatedRelationsSolver extends RecommendationSolver {

	private double probability = ModelConnectorConfiguration.separatedRelationsSolver_Probability;
	private String relName = "separated";

	/**
	 * Creates a new SeparatedRelationsSolver
	 *
	 * @param graph                the PARSE graph
	 * @param textExtractionState  the text extraction state
	 * @param modelExtractionState the model extraction state
	 * @param recommendationState  the recommendation state
	 */
	public SeparatedRelationsSolver(IGraph graph, TextExtractionState textExtractionState, ModelExtractionState modelExtractionState, RecommendationState recommendationState) {
		super(DependencyType.RECOMMENDATION, graph, textExtractionState, modelExtractionState, recommendationState);
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
		for (RecommendedInstance ri : recommendationState.getRecommendedInstances()) {
			String siName = ri.getName();

			List<String> occs = new ArrayList<>();
			for (NounMapping nnm : ri.getNameMappings()) {
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

					List<List<RecommendedInstance>> riPossibilities = new ArrayList<>();

					for (String occPart : occParts) {
						List<RecommendedInstance> ris = this.recommendationState.getRecommendedInstancesBySimilarName(occPart);
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
						List<List<RecommendedInstance>> allRelationProbabilities = Utilis.cartesianProduct(new ArrayList<>(), riPossibilities);

						for (List<RecommendedInstance> possibility : allRelationProbabilities) {

							RecommendedInstance r1 = possibility.get(0);
							RecommendedInstance r2 = possibility.get(1);
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
