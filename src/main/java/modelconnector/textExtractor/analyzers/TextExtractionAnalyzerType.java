package modelconnector.textExtractor.analyzers;

import edu.kit.ipd.parse.luna.graph.IGraph;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * Factory class for the creation of text extraction analyzers.
 *
 * @author Sophie
 *
 */
public enum TextExtractionAnalyzerType {

	/**
	 * Redirection of the creation of a noun analyzer.
	 */
	NOUN_ANALYZER(NounAnalyzer::new),

	/**
	 * Redirection of the creation of a name type analyzer.
	 */
	INDEPARCS_ANALYZER(InDepArcsAnalyzer::new),
	/**
	 * Redirection of the creation of an out dep arcs analyzer.
	 */
	OUTDEPARCS_ANALYZER(OutDepArcsAnalyzer::new),
	/**
	 * Redirection of the creation of an article type name analyzer.
	 */
	ARTICLETYPENAME_ANALYZER(ArticleTypeNameAnalyzer::new),
	/**
	 * Redirection of the creation of a separated names analyzer.
	 */
	SEPARATEDNAMES_ANALYZER(SeparatedNamesAnalyzer::new);

	private Crea f;

	TextExtractionAnalyzerType(Crea f) {
		this.f = f;
	}

	/**
	 * Creates a new analyzer.
	 *
	 * @param graph               the PARSE graph to look up
	 * @param textExtractionState the text extraction state to work with
	 * @return the created analyzer
	 */
	public TextExtractionAnalyzer create(IGraph graph, TextExtractionState textExtractionState) {
		return this.f.crea(graph, textExtractionState);
	}

	private interface Crea {
		TextExtractionAnalyzer crea(IGraph graph, TextExtractionState textExtractionState);
	}
}
