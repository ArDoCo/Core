package modelconnector.textExtractor;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.parse.luna.agent.AbstractAgent;
import edu.kit.ipd.parse.luna.graph.INode;
import modelconnector.helpers.ModelConnectorConfiguration;
import modelconnector.textExtractor.analyzers.TextExtractionAnalyzer;
import modelconnector.textExtractor.analyzers.TextExtractionAnalyzerType;
import modelconnector.textExtractor.solvers.TextExtractionSolver;
import modelconnector.textExtractor.solvers.TextExtractionSolverType;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * The name type relation agent uses analyzers, patterns and simple graph
 * queries to extract names, types and relations. It only uses informations
 * stored in the graph and is independent from other states. It inherits from
 * the PARSE abstract agent.
 *
 * @author Sophie
 *
 */
@MetaInfServices(AbstractAgent.class)
public class TextExtractionAgent extends AbstractAgent {

	private TextExtractionState textExtractionState = new TextExtractionState();
	private List<TextExtractionAnalyzer> analyzers = new ArrayList<>();
	private List<TextExtractionSolver> solvers = new ArrayList<>();

	/**
	 * Creates a new name type relation Agent
	 */
	public TextExtractionAgent() {
	}

	/**
	 * Initializes the agent with its id.
	 */
	@Override
	public void init() {
		setId("textExtractionAgent");
	}

	/**
	 * Runs the agent with its analyzers and finders.
	 */
	@Override
	protected void exec() {

		initializeWithGraph();

		runAnalyzers();

		runSolvers();
	}

	/**
	 * Initializes graph dependent analyzers and solvers
	 */
	private void initializeWithGraph() {

		for (TextExtractionAnalyzerType textAnalyzerType : ModelConnectorConfiguration.textExtractionAgent_Analyzers) {
			analyzers.add(textAnalyzerType.create(graph, textExtractionState));
		}

		for (TextExtractionSolverType textSolverType : ModelConnectorConfiguration.textExtractionAgemt_Solvers) {
			solvers.add(textSolverType.create(graph, textExtractionState));
		}
	}

	/**
	 * Runs finders. In contrast to analyzers finders aren't executed on a single
	 * node, but on the whole graph.
	 */
	private void runSolvers() {
		for (TextExtractionSolver solver : solvers) {
			solver.exec();
		}
	}

	/**
	 * Runs the analyzers.
	 */
	private void runAnalyzers() {

		for (INode n : graph.getNodesOfType(graph.getNodeType("token"))) {
			for (TextExtractionAnalyzer analyzer : analyzers) {
				analyzer.exec(n);
			}
		}
	}

	/**
	 * Returns the current text extraction state.
	 *
	 * @return current name type relation state
	 */
	public TextExtractionState getState() {
		return textExtractionState;
	}

}