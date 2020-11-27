package modelconnector.recommendationGenerator.solvers;

import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.IGraph;
import modelconnector.DependencyType;
import modelconnector.Solver;
import modelconnector.modelExtractor.state.ModelExtractionState;
import modelconnector.recommendationGenerator.state.RecommendationState;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * A solver that creates recommendations.
 *
 * @author Sophie
 *
 */
public abstract class RecommendationSolver extends Solver {

    protected IGraph graph;
    protected IArcType depArcType;
    protected IArcType relArcType;
    protected TextExtractionState textExtractionState;
    protected ModelExtractionState modelExtractionState;
    protected RecommendationState recommendationState;

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
     *            the model extraction state to work with
     */
    protected RecommendationSolver(//
            DependencyType dependencyType, IGraph graph, TextExtractionState textExtractionState, //
            ModelExtractionState modelExtractionState, RecommendationState recommendationState) {
        super(dependencyType);
        this.graph = graph;
        depArcType = graph.getArcType("typedDependency");
        relArcType = graph.getArcType("relation");
        this.textExtractionState = textExtractionState;
        this.modelExtractionState = modelExtractionState;
        this.recommendationState = recommendationState;
    }

    @Override
    public abstract void exec();
}
