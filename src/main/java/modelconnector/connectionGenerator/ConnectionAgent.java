package modelconnector.connectionGenerator;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.parse.luna.agent.AbstractAgent;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import modelconnector.Analyzer;
import modelconnector.connectionGenerator.analyzers.ModelConnectionAnalyzerType;
import modelconnector.connectionGenerator.solvers.ModelConnectionSolver;
import modelconnector.connectionGenerator.solvers.ModelConnectionSolverType;
import modelconnector.connectionGenerator.state.ConnectionState;
import modelconnector.helpers.ModelConnectorConfiguration;
import modelconnector.modelExtractor.state.ModelExtractionState;
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
public class ConnectionAgent extends AbstractAgent {

	private ModelExtractionState modelExtractionState;
	private TextExtractionState textExtractionState;
	private ConnectionState connectionState = new ConnectionState();
	private RecommendationState recommendationState = new RecommendationState();

	private List<Analyzer> analyzers = new ArrayList<>();
	private List<ModelConnectionSolver> solvers = new ArrayList<>();

	/**
	 * Creates a new model connection agent with the given extraction states.
	 *
	 * @param graph                the PARSE graph
	 * @param modelExtractionState the model extraction state
	 * @param textExtractionState  the text extraction state
	 * @param recommendationState  the state with the recommendations
	 */
	public ConnectionAgent(IGraph graph, ModelExtractionState modelExtractionState, TextExtractionState textExtractionState, RecommendationState recommendationState) {
		this.graph = graph;
		this.modelExtractionState = modelExtractionState;
		this.textExtractionState = textExtractionState;
		this.recommendationState = recommendationState;
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
	 * @param modelExtractionState the current model extraction state.
	 */
	public void setModelExtractionState(ModelExtractionState modelExtractionState) {
		this.modelExtractionState = modelExtractionState;
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

		for (ModelConnectionAnalyzerType analyzerType : ModelConnectorConfiguration.MODEL_CONNECTION_AGENT_ANALYZERS) {
			analyzers.add(analyzerType.create(graph, textExtractionState, modelExtractionState, recommendationState, connectionState));
		}

		for (ModelConnectionSolverType solverType : ModelConnectorConfiguration.MODEL_CONNECTION_AGENT_SOLVERS) {
			solvers.add(solverType.create(graph, textExtractionState, modelExtractionState, recommendationState, connectionState));
		}
	}

	/**
	 * Runs solvers, that connect model extraction State and Recommendation State.
	 */
	public void runSolvers() {
		for (ModelConnectionSolver solver : solvers) {
			solver.exec();
		}
	}

	/**
	 * Runs analyzers, that work node by node.
	 */
	private void runAnalyzers() {
		for (INode n : graph.getNodesOfType(graph.getNodeType("token"))) {
			for (Analyzer analyzer : analyzers) {
				analyzer.exec(n);
			}
		}
	}

	/**
	 * Returns the connection state.
	 *
	 * @return the current connection state
	 */
	public ConnectionState getConnectionState() {
		return connectionState;
	}
}