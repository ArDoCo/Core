package modelconnector.recommendationGenerator.solvers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.kit.ipd.parse.luna.graph.IGraph;
import modelconnector.DependencyType;
import modelconnector.helpers.ModelConnectorConfiguration;
import modelconnector.helpers.SimilarityUtils;
import modelconnector.modelExtractor.state.Instance;
import modelconnector.modelExtractor.state.ModelExtractionState;
import modelconnector.recommendationGenerator.state.RecommendationState;
import modelconnector.textExtractor.state.NounMapping;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * The reference solver finds instances mentioned in the text extraction state
 * as names. If it founds some similar names it creates recommendations.
 *
 * @author Sophie
 *
 */
public class ReferenceSolver extends RecommendationSolver {

	private double probability = ModelConnectorConfiguration.REFERENCE_SOLVER_PROBABILITY;
	private double areNamesSimilarThreshold = ModelConnectorConfiguration.REFERENCE_SOLVER_ARE_NAMES_SIMILAR_THRESHOLD;
	private double proportionalDecrease = ModelConnectorConfiguration.REFERENCE_SOLVER_PROPORTIONAL_DECREASE;

	/**
	 * Creates a new ReferenceSolver.
	 *
	 * @param graph                the PARSE graph
	 * @param modelExtractionState the model extraction state
	 * @param recommendationState  the recommendation state
	 * @param textExtractionState  the text extraction state
	 */
	public ReferenceSolver(IGraph graph, TextExtractionState textExtractionState, ModelExtractionState modelExtractionState, RecommendationState recommendationState) {
		super(DependencyType.TEXT_MODEL_RECOMMENDATION, graph, textExtractionState, modelExtractionState, recommendationState);
	}

	/**
	 * Executes the solver.
	 */
	@Override
	public void exec() {

		solveReferencesOfNames();
	}

	/**
	 * Searches for instances mentioned in the text extraction state as names. If it
	 * founds some similar names it creates recommendations.
	 */
	private void solveReferencesOfNames() {

		for (Instance instance : modelExtractionState.getInstances()) {
			// ntrNodes mit Lemma ca. Name eines Modelelements

			List<NounMapping> similarToInstanceMappings = //
					textExtractionState.getNames().stream().filter(n -> SimilarityUtils.areWordsOfListsSimilar(//
							instance.getNames(), List.of(n.getReference()), areNamesSimilarThreshold)).collect(Collectors.toList());

			if (similarToInstanceMappings.isEmpty()) {

				solveReferenceOfNamesIfSimilarNameIsEmpty(instance);

			} else {

				for (NounMapping similarNameMapping : similarToInstanceMappings) {
					recommendationState.addRecommendedInstanceJustName(similarNameMapping.getReference(), probability, similarToInstanceMappings);
				}
			}

		}

	}

	/**
	 * Searches for the longest name of a given instance in the noun mappings of the
	 * text extraction state. If no similar mapping can be found the search is
	 * continued. Otherwise, the found mapping is added to the recommendation state.
	 *
	 * @param instance the current instance to find as noun mapping
	 */
	private void solveReferenceOfNamesIfSimilarNameIsEmpty(Instance instance) {
		List<NounMapping> similarLongestNameMappings = textExtractionState.getNames().stream().filter(//
				nm -> SimilarityUtils.areWordsSimilar(instance.getLongestName(), nm.getReference())).collect(Collectors.toList());

		if (similarLongestNameMappings.isEmpty()) {
			solveReferenceOfNamesIfNoSimilarLongNamesCouldBeFound(instance);
		} else {
			if (similarLongestNameMappings.size() == 1) {
				recommendationState.addRecommendedInstanceJustName(similarLongestNameMappings.get(0).getReference(), probability, similarLongestNameMappings);
			}
		}
	}

	/**
	 * Searches for each name of the instance a similar mapping in the text
	 * extraction state. If some is found it is added to the recommendation state.
	 * If its more than one the probability is decreased.
	 *
	 * @param instance the current instance to find as noun mapping
	 */
	private void solveReferenceOfNamesIfNoSimilarLongNamesCouldBeFound(Instance instance) {
		List<NounMapping> similarNameMappings = new ArrayList<>();
		for (String name : instance.getNames()) {
			similarNameMappings = textExtractionState.getNames().stream().filter(nm -> SimilarityUtils.areWordsSimilar(name, nm.getReference())).collect(Collectors.toList());
		}

		double prob = probability;
		if (similarNameMappings.size() >= 1) {
			prob = probability * proportionalDecrease;
		}

		for (NounMapping similarNameMapping : similarNameMappings) {
			recommendationState.addRecommendedInstanceJustName(similarNameMapping.getReference(), prob, similarNameMappings);
		}
	}
}
