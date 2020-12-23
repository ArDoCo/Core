package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import edu.kit.ipd.consistency_analyzer.datastructures.IModelExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendationState;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IWord;

/**
 * This class represents all analyzers that create work on recommendations.
 *
 * @author Sophie
 *
 */
public abstract class RecommendationAnalyzer extends Analyzer implements IRecommendationAnalyzer {

	protected ITextExtractionState textExtractionState;
	protected IModelExtractionState modelExtractionState;
	protected IRecommendationState recommendationState;

	/**
	 * Creates a new analyzer.
	 *
	 * @param dependencyType       the dependencies of the analyzer
	 * @param graph                the PARSE graph to look up
	 * @param textExtractionState  the text extraction state to look up
	 * @param modelExtractionState the model extraction state to look up
	 * @param recommendationState  the model extraction state to work with
	 */
	protected RecommendationAnalyzer(//
			DependencyType dependencyType, ITextExtractionState textExtractionState, IModelExtractionState modelExtractionState, IRecommendationState recommendationState) {
		super(dependencyType);
		this.textExtractionState = textExtractionState;
		this.modelExtractionState = modelExtractionState;
		this.recommendationState = recommendationState;
	}

	@Override
	public abstract void exec(IWord node);

}
