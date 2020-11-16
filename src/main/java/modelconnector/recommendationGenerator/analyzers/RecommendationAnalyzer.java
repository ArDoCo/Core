package modelconnector.recommendationGenerator.analyzers;

import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import modelconnector.Analyzer;
import modelconnector.DependencyType;
import modelconnector.modelExtractor.state.ModelExtractionState;
import modelconnector.recommendationGenerator.state.RecommendationState;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * This class represents all analyzers that create work on recommendations.
 *
 * @author Sophie
 *
 */
public abstract class RecommendationAnalyzer extends Analyzer {

	protected IGraph graph;
	protected IArcType depArcType;
	protected IArcType relArcType;
	protected TextExtractionState textExtractionState;
	protected ModelExtractionState modelExtractionState;
	protected RecommendationState recommendationState;

	/**
	 * Creates a new analyzer.
	 *
	 * @param dependencyType       the dependencies of the analyzer
	 * @param graph                the PARSE graph to look up
	 * @param textExtractionState  the text extraction state to look up
	 * @param modelExtractionState the model extraction state to look up
	 * @param recommendationState  the model extraction state to work with
	 */
	public RecommendationAnalyzer(//
			DependencyType dependencyType, IGraph graph, TextExtractionState textExtractionState, ModelExtractionState modelExtractionState, RecommendationState recommendationState) {
		super(dependencyType);
		this.graph = graph;
		depArcType = graph.getArcType("typedDependency");
		relArcType = graph.getArcType("relation");
		this.textExtractionState = textExtractionState;
		this.modelExtractionState = modelExtractionState;
		this.recommendationState = recommendationState;
	}

	@Override
	public abstract void exec(INode node);

}
