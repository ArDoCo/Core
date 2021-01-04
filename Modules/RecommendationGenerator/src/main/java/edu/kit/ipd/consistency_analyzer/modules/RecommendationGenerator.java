package edu.kit.ipd.consistency_analyzer.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.kit.ipd.consistency_analyzer.analyzers_solvers.AnalyzerSolverLoader;
import edu.kit.ipd.consistency_analyzer.analyzers_solvers.IRecommendationAnalyzer;
import edu.kit.ipd.consistency_analyzer.analyzers_solvers.IRecommendationSolver;
import edu.kit.ipd.consistency_analyzer.datastructures.IModelExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendationState;
import edu.kit.ipd.consistency_analyzer.datastructures.IText;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IWord;
import edu.kit.ipd.consistency_analyzer.datastructures.RecommendationState;

public class RecommendationGenerator implements IAnalyzerSolverModule<IRecommendationState> {

	private IText graph;
	private ITextExtractionState textExtractionState;
	private IModelExtractionState modelExtractionState;
	private IRecommendationState recommendationState = new RecommendationState();

	private List<IRecommendationAnalyzer> analyzers = new ArrayList<>();
	private List<IRecommendationSolver> solvers = new ArrayList<>();

	/**
	 * Creates a new model connection agent with the given extraction state and ntr
	 * state.
	 * 
	 * @param graph
	 *
	 * @param extractionState the extraction state
	 * @param ntrState        the name type relation state
	 */
	public RecommendationGenerator(IText graph, IModelExtractionState extractionState, ITextExtractionState ntrState) {
		this.graph = graph;
		this.modelExtractionState = extractionState;
		this.textExtractionState = ntrState;

		initializeAnalyzerSolvers();
	}

	@Override
	public void exec() {

		runAnalyzers();

		runSolvers();

		return;

	}

	/**
	 * Sets the model extraction state.
	 *
	 * @param modelextractionState the current model extraction state.
	 */
	public void setModelExtractionState(IModelExtractionState modelextractionState) {
		this.modelExtractionState = modelextractionState;
	}

	/**
	 * Sets the text extraction state.
	 *
	 * @param textExtractionState the current text extraction state.
	 */
	public void setTextExtractionState(ITextExtractionState textExtractionState) {
		this.textExtractionState = textExtractionState;
	}

	/**
	 * Initializes graph dependent analyzers.
	 */
	private void initializeAnalyzerSolvers() {

		Map<String, IRecommendationAnalyzer> myAnalyzers = AnalyzerSolverLoader.loadLoadable(IRecommendationAnalyzer.class);

		for (String recommendationAnalyzer : RecommendationGeneratorConfig.RECOMMENDATION_AGENT_ANALYZERS) {
			if (!myAnalyzers.containsKey(recommendationAnalyzer)) {
				throw new IllegalArgumentException("RecommendationAnalyzer " + recommendationAnalyzer + " not found");
			}
			analyzers.add(myAnalyzers.get(recommendationAnalyzer).create(textExtractionState, modelExtractionState, recommendationState));
		}

		Map<String, IRecommendationSolver> mySolvers = AnalyzerSolverLoader.loadLoadable(IRecommendationSolver.class);

		for (String recommendationSolver : RecommendationGeneratorConfig.RECOMMENDATION_AGENT_SOLVERS) {
			if (!mySolvers.containsKey(recommendationSolver)) {
				throw new IllegalArgumentException("RecommendationSolver " + recommendationSolver + " not found");
			}
			solvers.add(mySolvers.get(recommendationSolver).create(textExtractionState, modelExtractionState, recommendationState));
		}

	}

	/**
	 * Runs solvers, that create recommendations.
	 */
	@Override
	public void runSolvers() {
		for (IRecommendationSolver solver : solvers) {
			solver.exec();
		}

	}

	/**
	 * Runs analyzers, that work node by node.
	 */
	@Override
	public void runAnalyzers() {
		for (IWord n : graph.getNodes()) {
			for (IRecommendationAnalyzer analyzer : analyzers) {
				analyzer.exec(n);
			}
		}
	}

	@Override
	public IRecommendationState getState() {
		return this.recommendationState;
	}

}
