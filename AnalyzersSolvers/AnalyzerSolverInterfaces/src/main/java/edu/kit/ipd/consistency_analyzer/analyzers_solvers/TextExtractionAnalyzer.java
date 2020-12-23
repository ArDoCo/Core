package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import edu.kit.ipd.consistency_analyzer.datastructures.ITextExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IWord;

/**
 * A TextExtractionAnalyzer is an analyzer that is dependent from depArcs and
 * relArcs (PARSE graph).
 *
 * @author Sophie
 * 
 */
public abstract class TextExtractionAnalyzer extends Analyzer implements ITextAnalyzer {

	protected ITextExtractionState textExtractionState;

	/**
	 * Creates a new NameTypeRelationAnalyzer
	 *
	 * @param dependencyType      the dependencies of the analyzer
	 * @param graph               PARSE graph which contains the arcs
	 * @param textExtractionState the text extraction state
	 */
	protected TextExtractionAnalyzer(DependencyType dependencyType, ITextExtractionState textExtractionState) {
		super(dependencyType);
		this.textExtractionState = textExtractionState;
	}

	@Override
	public abstract void exec(IWord node);

}
