package modelconnector.connectionGenerator.analyzers;

import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import modelconnector.Analyzer;
import modelconnector.DependencyType;
import modelconnector.connectionGenerator.state.ConnectionState;
import modelconnector.modelExtractor.state.ModelExtractionState;
import modelconnector.recommendationGenerator.state.RecommendationState;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * This class represents an analyzer, that works on the level of the connection
 * creation.
 *
 * @author Sophie
 *
 */
public abstract class ModelConnectionAnalyzer extends Analyzer {

	protected IGraph graph;
	protected IArcType depArcType;
	protected IArcType relArcType;
	protected TextExtractionState textExtractionState;
	protected ModelExtractionState modelExtractionState;
	protected RecommendationState recommendationState;
	protected ConnectionState connectionState;

	/**
	 * Creates a new analyzer.
	 *
	 * @param dependencyType       the dependencies of the analyzer
	 * @param graph                the PARSE graph to look up
	 * @param textExtractionState  the text extraction state to look up
	 * @param modelExtractionState the model extraction state to look up
	 * @param recommendationState  the model extraction state to look up
	 * @param connectionState      the connection state to work with
	 */
	public ModelConnectionAnalyzer(//
			DependencyType dependencyType, IGraph graph, TextExtractionState textExtractionState, ModelExtractionState modelExtractionState, //
			RecommendationState recommendationState, ConnectionState connectionState) {
		super(dependencyType);
		this.graph = graph;
		depArcType = graph.getArcType("typedDependency");
		relArcType = graph.getArcType("relation");
		this.textExtractionState = textExtractionState;
		this.modelExtractionState = modelExtractionState;
		this.recommendationState = recommendationState;
		this.connectionState = connectionState;
	}

	@Override
	public abstract void exec(INode node);

}
