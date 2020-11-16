package modelconnector.recommendationGenerator;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.parse.luna.agent.AbstractAgent;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import modelconnector.helpers.ModelConnectorConfiguration;
import modelconnector.modelExtractor.state.ModelExtractionState;
import modelconnector.recommendationGenerator.analyzers.RecommendationAnalyzer;
import modelconnector.recommendationGenerator.analyzers.RecommendationAnalyzerType;
import modelconnector.recommendationGenerator.solvers.RecommendationSolver;
import modelconnector.recommendationGenerator.solvers.RecommendationSolverType;
import modelconnector.recommendationGenerator.state.RecommendationState;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * The ModelConnectionAgent runs different analyzers and solvers. This agent
 * creates recommendations as well as matchings between text and model. The
 * order is important: All connections should run after the recommendations have
 * been made.
 *
 * @author Sophie
 *
 */
@MetaInfServices(AbstractAgent.class)
public class RecommendationAgent extends AbstractAgent {

	private TextExtractionState textExtractionState;
	private ModelExtractionState modelExtractionState;
	private RecommendationState recommendationState = new RecommendationState();

	private List<RecommendationAnalyzer> analyzers = new ArrayList<>();
	private List<RecommendationSolver> solvers = new ArrayList<>();

	/**
	 * Creates a new model connection agent with the given extraction state and ntr
	 * state.
	 *
	 * @param graph
	 *
	 * @param extractionState the extraction state
	 * @param ntrState        the name type relation state
	 */
	public RecommendationAgent(IGraph graph, ModelExtractionState extractionState, TextExtractionState ntrState) {
		this.graph = graph;
		this.modelExtractionState = extractionState;
		this.textExtractionState = ntrState;
	}

	@Override
	public void init() {
		setId("modelConnectionAgent");
	}

	@Override
	protected void exec() {

		initializeWithGraph();

		runAnalyzers();

		runSolvers();

		return;
	}

	/**
	 * Sets the model extraction state.
	 *
	 * @param modelextractionState the current model extraction state.
	 */
	public void setModelExtractionState(ModelExtractionState modelextractionState) {
		this.modelExtractionState = modelextractionState;
	}

	/**
	 * Sets the text extraction state.
	 *
	 * @param textExtractionState the current text extraction state.
	 */
	public void setTextExtractionState(TextExtractionState textExtractionState) {
		this.textExtractionState = textExtractionState;
	}

	/**
	 * Initializes graph dependent analyzers.
	 */
	private void initializeWithGraph() {

		for (RecommendationAnalyzerType analyzerType : ModelConnectorConfiguration.recommendationAgent_Analyzers) {
			analyzers.add(analyzerType.create(graph, textExtractionState, modelExtractionState, recommendationState));
		}

		for (RecommendationSolverType solverType : ModelConnectorConfiguration.recommendationAgent_Solvers) {
			solvers.add(solverType.create(graph, textExtractionState, modelExtractionState, recommendationState));
		}

	}

	/**
	 * Runs solvers, that create recommendations.
	 */
	public void runSolvers() {
		for (RecommendationSolver solver : solvers) {
			solver.exec();
		}

	}

	/**
	 * Runs analyzers, that work node by node.
	 */
	private void runAnalyzers() {
		for (INode n : graph.getNodesOfType(graph.getNodeType("token"))) {
			for (RecommendationAnalyzer analyzer : analyzers) {
				analyzer.exec(n);
			}
		}
	}

	/**
	 * Returns the recommendation state.
	 *
	 * @return the current recommendation state
	 */
	public RecommendationState getRecommendationState() {
		return recommendationState;
	}
}