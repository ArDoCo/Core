package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.consistency_analyzer.common.SimilarityUtils;
import edu.kit.ipd.consistency_analyzer.datastructures.IInstance;
import edu.kit.ipd.consistency_analyzer.datastructures.IModelExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.INounMapping;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendationState;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextExtractionState;

/**
 * The reference solver finds instances mentioned in the text extraction state
 * as names. If it founds some similar names it creates recommendations.
 *
 * @author Sophie
 *
 */
@MetaInfServices(IRecommendationSolver.class)
public class ReferenceSolver extends RecommendationSolver {

	private double probability = GenericRecommendationAnalyzerSolverConfig.REFERENCE_SOLVER_PROBABILITY;
	private double areNamesSimilarThreshold = GenericRecommendationAnalyzerSolverConfig.REFERENCE_SOLVER_ARE_NAMES_SIMILAR_THRESHOLD;
	private double proportionalDecrease = GenericRecommendationAnalyzerSolverConfig.REFERENCE_SOLVER_PROPORTIONAL_DECREASE;

	/**
	 * Creates a new ReferenceSolver.
	 *
	 * @param graph                the PARSE graph
	 * @param modelExtractionState the model extraction state
	 * @param recommendationState  the recommendation state
	 * @param textExtractionState  the text extraction state
	 */
	public ReferenceSolver(ITextExtractionState textExtractionState, IModelExtractionState modelExtractionState, IRecommendationState recommendationState) {
		super(DependencyType.TEXT_MODEL_RECOMMENDATION, textExtractionState, modelExtractionState, recommendationState);
		probability = GenericRecommendationAnalyzerSolverConfig.REFERENCE_SOLVER_PROBABILITY;
		areNamesSimilarThreshold = GenericRecommendationAnalyzerSolverConfig.REFERENCE_SOLVER_ARE_NAMES_SIMILAR_THRESHOLD;
		proportionalDecrease = GenericRecommendationAnalyzerSolverConfig.REFERENCE_SOLVER_PROPORTIONAL_DECREASE;
	}

	public ReferenceSolver(ITextExtractionState textExtractionState, IModelExtractionState modelExtractionState, IRecommendationState recommendationState, double probability,
			double areNamesSimilarThreshold, double proportionalDecrease) {
		this(textExtractionState, modelExtractionState, recommendationState);
		this.probability = probability;
		this.areNamesSimilarThreshold = areNamesSimilarThreshold;
		this.proportionalDecrease = proportionalDecrease;
	}

	public ReferenceSolver() {
		this(null, null, null);
	}

	@Override
	public IRecommendationSolver create(ITextExtractionState textExtractionState, IModelExtractionState modelExtractionState, IRecommendationState recommendationState) {
		return new ReferenceSolver(textExtractionState, modelExtractionState, recommendationState);
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

		for (IInstance instance : modelExtractionState.getInstances()) {
			// ntrNodes mit Lemma ca. Name eines Modelelements

			List<INounMapping> similarToInstanceMappings = //
					textExtractionState.getNames().stream().filter(n -> SimilarityUtils.areWordsOfListsSimilar(//
							instance.getNames(), List.of(n.getReference()), areNamesSimilarThreshold)).collect(Collectors.toList());

			if (similarToInstanceMappings.isEmpty()) {

				solveReferenceOfNamesIfSimilarNameIsEmpty(instance);

			} else {

				for (INounMapping similarNameMapping : similarToInstanceMappings) {
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
	private void solveReferenceOfNamesIfSimilarNameIsEmpty(IInstance instance) {
		List<INounMapping> similarLongestNameMappings = textExtractionState.getNames().stream().filter(//
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
	private void solveReferenceOfNamesIfNoSimilarLongNamesCouldBeFound(IInstance instance) {
		List<INounMapping> similarNameMappings = new ArrayList<>();
		for (String name : instance.getNames()) {
			similarNameMappings = textExtractionState.getNames().stream().filter(nm -> SimilarityUtils.areWordsSimilar(name, nm.getReference())).collect(Collectors.toList());
		}

		double prob = probability;
		if (!similarNameMappings.isEmpty()) {
			prob = probability * proportionalDecrease;
		}

		for (INounMapping similarNameMapping : similarNameMappings) {
			recommendationState.addRecommendedInstanceJustName(similarNameMapping.getReference(), prob, similarNameMappings);
		}
	}
}
