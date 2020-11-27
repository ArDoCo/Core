package modelconnector.textExtractor.analyzers;

import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import modelconnector.Analyzer;
import modelconnector.DependencyType;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * A TextExtractionAnalyzer is an analyzer that is dependent from depArcs and relArcs (PARSE graph).
 *
 * @author Sophie
 *
 */
public abstract class TextExtractionAnalyzer extends Analyzer {

    protected IGraph graph;
    protected IArcType depArcType;
    protected IArcType relArcType;
    protected TextExtractionState textExtractionState;

    /**
     * Creates a new NameTypeRelationAnalyzer
     *
     * @param dependencyType
     *            the dependencies of the analyzer
     * @param graph
     *            PARSE graph which contains the arcs
     * @param textExtractionState
     *            the text extraction state
     */
    protected TextExtractionAnalyzer(DependencyType dependencyType, IGraph graph,
            TextExtractionState textExtractionState) {
        super(dependencyType);
        this.graph = graph;
        depArcType = graph.getArcType("typedDependency");
        relArcType = graph.getArcType("relation");
        this.textExtractionState = textExtractionState;
    }

    @Override
    public abstract void exec(INode node);
}
