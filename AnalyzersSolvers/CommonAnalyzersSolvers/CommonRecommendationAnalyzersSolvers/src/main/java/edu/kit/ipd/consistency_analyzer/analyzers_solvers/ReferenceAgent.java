package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.consistency_analyzer.agents.AgentDatastructure;
import edu.kit.ipd.consistency_analyzer.agents.DependencyType;
import edu.kit.ipd.consistency_analyzer.agents.RecommendationAgent;
import edu.kit.ipd.consistency_analyzer.common.SimilarityUtils;
import edu.kit.ipd.consistency_analyzer.datastructures.IInstance;
import edu.kit.ipd.consistency_analyzer.datastructures.IModelState;
import edu.kit.ipd.consistency_analyzer.datastructures.INounMapping;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendationState;
import edu.kit.ipd.consistency_analyzer.datastructures.IText;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextState;

/**
 * The reference solver finds instances mentioned in the text extraction state
 * as names. If it founds some similar names it creates recommendations.
 *
 * @author Sophie
 *
 */
@MetaInfServices(RecommendationAgent.class)
public class ReferenceAgent extends RecommendationAgent {

	private double probability = GenericRecommendationConfig.REFERENCE_SOLVER_PROBABILITY;
	private double areNamesSimilarThreshold = GenericRecommendationConfig.REFERENCE_SOLVER_ARE_NAMES_SIMILAR_THRESHOLD;
	private double proportionalDecrease = GenericRecommendationConfig.REFERENCE_SOLVER_PROPORTIONAL_DECREASE;

	/**
	 * Creates a new ReferenceSolver.
	 *
	 * @param graph                the PARSE graph
	 * @param modelExtractionState the model extraction state
	 * @param recommendationState  the recommendation state
	 * @param textExtractionState  the text extraction state
	 */
	public ReferenceAgent(IText text, ITextState textExtractionState, IModelState modelExtractionState, IRecommendationState recommendationState) {
		super(DependencyType.TEXT_MODEL_RECOMMENDATION, text, textExtractionState, modelExtractionState, recommendationState);
		probability = GenericRecommendationConfig.REFERENCE_SOLVER_PROBABILITY;
		areNamesSimilarThreshold = GenericRecommendationConfig.REFERENCE_SOLVER_ARE_NAMES_SIMILAR_THRESHOLD;
		proportionalDecrease = GenericRecommendationConfig.REFERENCE_SOLVER_PROPORTIONAL_DECREASE;
	}

	public ReferenceAgent(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState, double probability, double areNamesSimilarThreshold,
			double proportionalDecrease) {
		this(text, textState, modelState, recommendationState);
		this.probability = probability;
		this.areNamesSimilarThreshold = areNamesSimilarThreshold;
		this.proportionalDecrease = proportionalDecrease;
	}

	public ReferenceAgent(AgentDatastructure data, double probability, double areNamesSimilarThreshold, double proportionalDecrease) {
		this(data);
		this.probability = probability;
		this.areNamesSimilarThreshold = areNamesSimilarThreshold;
		this.proportionalDecrease = proportionalDecrease;
	}

	public ReferenceAgent(AgentDatastructure data) {
		this(data.getText(), data.getTextState(), data.getModelState(), data.getRecommendationState());
	}

	public ReferenceAgent() {
		super(DependencyType.TEXT_MODEL_RECOMMENDATION);
	}

	@Override
	public RecommendationAgent create(IText text, ITextState textState, IModelState modelExtractionState, IRecommendationState recommendationState) {
		return new ReferenceAgent(text, textState, modelExtractionState, recommendationState);
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

		for (IInstance instance : modelState.getInstances()) {
			// ntrNodes mit Lemma ca. Name eines Modelelements

			List<INounMapping> similarToInstanceMappings = //
					textState.getNames().stream().filter(n -> SimilarityUtils.areWordsOfListsSimilar(//
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
		List<INounMapping> similarLongestNameMappings = textState.getNames().stream().filter(//
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
			similarNameMappings = textState.getNames().stream().filter(nm -> SimilarityUtils.areWordsSimilar(name, nm.getReference())).collect(Collectors.toList());
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
