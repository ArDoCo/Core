package modelconnector.connectionGenerator.solvers;

import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.IGraph;
import modelconnector.DependencyType;
import modelconnector.Solver;
import modelconnector.connectionGenerator.state.ConnectionState;
import modelconnector.modelExtractor.state.ModelExtractionState;
import modelconnector.recommendationGenerator.state.RecommendationState;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * The connection solver creates connections between the recommendation state and the extraction state.
 *
 * @author Sophie
 *
 */
public abstract class ModelConnectionSolver extends Solver {

    protected IGraph graph;
    protected IArcType depArcType;
    protected IArcType relArcType;
    protected TextExtractionState textExtractionState;
    protected ModelExtractionState modelExtractionState;
    protected RecommendationState recommendationState;
    protected ConnectionState connectionState;

    /**
     * Creates a new solver.
     *
     * @param dependencyType
     *            the dependencies of the analyzer
     * @param graph
     *            the PARSE graph to look up
     * @param textExtractionState
     *            the text extraction state to look up
     * @param modelExtractionState
     *            the model extraction state to look up
     * @param recommendationState
     *            the model extraction state to look up
     * @param connectionState
     *            the connection state to work with
     */
    protected ModelConnectionSolver(//
            DependencyType dependencyType, IGraph graph, TextExtractionState textExtractionState, //
            ModelExtractionState modelExtractionState, RecommendationState recommendationState,
            ConnectionState connectionState) {
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
    public abstract void exec();
}
