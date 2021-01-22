package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import java.util.List;
import java.util.stream.Collectors;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.consistency_analyzer.agents.DependencyType;
import edu.kit.ipd.consistency_analyzer.common.SimilarityUtils;
import edu.kit.ipd.consistency_analyzer.datastructures.IInstance;
import edu.kit.ipd.consistency_analyzer.datastructures.IModelState;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendationState;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextState;
import edu.kit.ipd.consistency_analyzer.datastructures.IWord;
import edu.kit.ipd.consistency_analyzer.extractors.RecommendationExtractor;

/**
 * This analyzer searches for the occurrence of instance names and types of the
 * extraction state and adds them as names and types to the text extraction
 * state.
 *
 * @author Sophie
 *
 */
@MetaInfServices(RecommendationExtractor.class)
public class ExtractionDependentOccurrenceExtractor extends RecommendationExtractor {

	private double probability = GenericRecommendationConfig.EXTRACTION_DEPENDENT_OCCURRENCE_ANALYZER_PROBABILITY;

	/**
	 * Creates a new extraction dependent occurrence marker.
	 *
	 * @param graph                the PARSE graph to run on
	 * @param textExtractionState  the text extraction state
	 * @param modelExtractionState the model extraction state to work with
	 * @param recommendationState  the state with the recommendations
	 */
	public ExtractionDependentOccurrenceExtractor(//
			ITextState textExtractionState, IModelState modelExtractionState, IRecommendationState recommendationState) {
		super(DependencyType.TEXT_MODEL, textExtractionState, modelExtractionState, recommendationState);
	}

	public ExtractionDependentOccurrenceExtractor() {
		this(null, null, null);
	}

	@Override
	public RecommendationExtractor create(ITextState textState, IModelState modelExtractionState, IRecommendationState recommendationState) {
		return new ExtractedTermsExtractor(textState, modelExtractionState, recommendationState);
	}

	@Override
	public void exec(IWord n) {

		searchForName(n);
		searchForType(n);
	}

	/**
	 * This method checks whether a given node is a name of an instance given in the
	 * model extraction state. If it appears to be a name this is stored in the text
	 * extraction state. If multiple options are available the node value is taken
	 * as reference.
	 *
	 * @param n the node to check
	 */
	private void searchForName(IWord n) {
		List<IInstance> instances = modelState.getInstances().stream().filter(//
				i -> SimilarityUtils.areWordsOfListsSimilar(i.getNames(), List.of(n.getText()))).collect(Collectors.toList());
		if (instances.size() == 1) {
			textState.addName(n, instances.get(0).getLongestName(), probability);

		} else if (instances.size() > 1) {
			textState.addName(n, n.getText(), probability);
		}
	}

	/**
	 * This method checks whether a given node is a type of an instance given in the
	 * model extraction state. If it appears to be a type this is stored in the text
	 * extraction state. If multiple options are available the node value is taken
	 * as reference.
	 *
	 * @param n the node to check
	 */
	private void searchForType(IWord n) {
		List<IInstance> instances = modelState.getInstances().stream().filter(//
				i -> SimilarityUtils.areWordsOfListsSimilar(i.getTypes(), List.of(n.getText()))).collect(Collectors.toList());
		if (instances.size() == 1) {
			textState.addType(n, instances.get(0).getLongestType(), probability);

		} else if (instances.size() > 1) {
			textState.addType(n, n.getText(), probability);
		}
	}

}
