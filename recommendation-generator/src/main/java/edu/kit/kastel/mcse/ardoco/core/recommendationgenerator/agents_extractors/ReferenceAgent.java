package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents_extractors;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyType;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.RecommendationAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.common.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;

/**
 * The reference solver finds instances mentioned in the text extraction state
 * as names. If it founds some similar names it creates recommendations.
 *
 * @author Sophie
 *
 */
@MetaInfServices(RecommendationAgent.class)
public class ReferenceAgent extends RecommendationAgent {

	private double probability;
	private double areNamesSimilarThreshold;
	private double proportionalDecrease;

	public ReferenceAgent() {
		super(GenericRecommendationConfig.class);
	}

	private ReferenceAgent(IText text, ITextState textExtractionState, IModelState modelExtractionState, IRecommendationState recommendationState,
			GenericRecommendationConfig config) {
		super(DependencyType.TEXT_MODEL_RECOMMENDATION, GenericRecommendationConfig.class, text, textExtractionState, modelExtractionState,
				recommendationState);
		probability = config.referenceSolverProbability;
		areNamesSimilarThreshold = config.referenceSolverAreNamesSimilarThreshold;
		proportionalDecrease = config.referenceSolverProportionalDecrease;
	}

	@Override
	public RecommendationAgent create(IText text, ITextState textState, IModelState modelExtractionState, IRecommendationState recommendationState,
			Configuration config) {
		return new ReferenceAgent(text, textState, modelExtractionState, recommendationState, (GenericRecommendationConfig) config);
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
		} else if (similarLongestNameMappings.size() == 1) {
			recommendationState.addRecommendedInstanceJustName(similarLongestNameMappings.get(0).getReference(), probability, similarLongestNameMappings);
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
			similarNameMappings = textState.getNames().stream().filter(nm -> SimilarityUtils.areWordsSimilar(name, nm.getReference()))
					.collect(Collectors.toList());
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
