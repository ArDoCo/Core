package edu.kit.ipd.consistency_analyzer.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.kit.ipd.consistency_analyzer.analyzers_solvers.AnalyzerSolverLoader;
import edu.kit.ipd.consistency_analyzer.analyzers_solvers.IConnectionAnalyzer;
import edu.kit.ipd.consistency_analyzer.analyzers_solvers.IConnectionSolver;
import edu.kit.ipd.consistency_analyzer.datastructures.ConnectionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IConnectionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IModelExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendationState;
import edu.kit.ipd.consistency_analyzer.datastructures.IText;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IWord;

/**
 * The ModelConnectionAgent runs different analyzers and solvers. This agent
 * creates recommendations as well as matchings between text and model. The
 * order is important: All connections should run after the recommendations have
 * been made.
 *
 * @author Sophie
 *
 */
public class ConnectionGenerator implements IAnalyzerSolverModule<IConnectionState> {

	private IText graph;
	private IModelExtractionState modelExtractionState;
	private ITextExtractionState textExtractionState;
	private IConnectionState connectionState = new ConnectionState();
	private IRecommendationState recommendationState;

	private List<IConnectionAnalyzer> analyzers = new ArrayList<>();
	private List<IConnectionSolver> solvers = new ArrayList<>();

	/**
	 * Creates a new model connection agent with the given extraction states.
	 *
	 * @param graph                the PARSE graph
	 * @param modelExtractionState the model extraction state
	 * @param textExtractionState  the text extraction state
	 * @param recommendationState  the state with the recommendations
	 */
	public ConnectionGenerator(IText graph, IModelExtractionState modelExtractionState, ITextExtractionState textExtractionState, IRecommendationState recommendationState) {
		this.graph = graph;
		this.modelExtractionState = modelExtractionState;
		this.textExtractionState = textExtractionState;
		this.recommendationState = recommendationState;

		initializeAnalyzerSolvers();
	}

	@Override
	public void exec() {
		runAnalyzers();
		runSolvers();
	}

	/**
	 * Sets the model extraction state.
	 *
	 * @param modelExtractionState the current model extraction state.
	 */
	public void setModelExtractionState(IModelExtractionState modelExtractionState) {
		this.modelExtractionState = modelExtractionState;
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

		Map<String, IConnectionAnalyzer> myAnalyzers = AnalyzerSolverLoader.loadLoadable(IConnectionAnalyzer.class);

		for (String connectionAnalyzer : ConnectionGeneratorConfig.MODEL_CONNECTION_AGENT_ANALYZERS) {
			if (!myAnalyzers.containsKey(connectionAnalyzer)) {
				throw new IllegalArgumentException("ConnectionAnalyzer " + connectionAnalyzer + " not found");
			}
			analyzers.add(myAnalyzers.get(connectionAnalyzer).create(textExtractionState, modelExtractionState, recommendationState, connectionState));
		}

		Map<String, IConnectionSolver> mySolvers = AnalyzerSolverLoader.loadLoadable(IConnectionSolver.class);

		for (String connectionSolver : ConnectionGeneratorConfig.MODEL_CONNECTION_AGENT_SOLVERS) {
			if (!mySolvers.containsKey(connectionSolver)) {
				throw new IllegalArgumentException("ConnectionSolver " + connectionSolver + " not found");
			}
			solvers.add(mySolvers.get(connectionSolver).create(textExtractionState, modelExtractionState, recommendationState, connectionState));
		}
	}

	/**
	 * Runs solvers, that connect model extraction State and Recommendation State.
	 */
	@Override
	public void runSolvers() {
		for (IConnectionSolver solver : solvers) {
			solver.exec();
		}
	}

	/**
	 * Runs analyzers, that work node by node.
	 */
	@Override
	public void runAnalyzers() {
		for (IWord n : graph.getNodes()) {
			for (IConnectionAnalyzer analyzer : analyzers) {
				analyzer.exec(n);
			}
		}
	}

	/**
	 * Returns the connection state.
	 *
	 * @return the current connection state
	 */
	public IConnectionState getConnectionState() {
		return connectionState;
	}

	@Override
	public IConnectionState getState() {
		return connectionState;
	}
}