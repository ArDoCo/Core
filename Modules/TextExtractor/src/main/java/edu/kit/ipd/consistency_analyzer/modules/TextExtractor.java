package edu.kit.ipd.consistency_analyzer.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.kit.ipd.consistency_analyzer.analyzers_solvers.AnalyzerSolverLoader;
import edu.kit.ipd.consistency_analyzer.analyzers_solvers.ITextAnalyzer;
import edu.kit.ipd.consistency_analyzer.analyzers_solvers.ITextSolver;
import edu.kit.ipd.consistency_analyzer.datastructures.IText;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IWord;
import edu.kit.ipd.consistency_analyzer.datastructures.TextExtractionState;

public class TextExtractor implements IAnalyzerSolverModule<ITextExtractionState> {

	private IText graph;
	private ITextExtractionState textExtractionState = new TextExtractionState();
	private List<ITextAnalyzer> analyzers = new ArrayList<>();
	private List<ITextSolver> solvers = new ArrayList<>();

	/**
	 * Creates a new model connection agent with the given extraction states.
	 *
	 * @param graph                the PARSE graph
	 * @param modelExtractionState the model extraction state
	 * @param textExtractionState  the text extraction state
	 * @param recommendationState  the state with the recommendations
	 */
	public TextExtractor(IText graph) {
		this.graph = graph;

		initializeAnalyzerSolvers();
	}

	@Override
	public void exec() {

		runAnalyzers();

		runSolvers();

	}

	/**
	 * Initializes graph dependent analyzers and solvers
	 */

	private void initializeAnalyzerSolvers() {

		Map<String, ITextAnalyzer> myAnalyzers = AnalyzerSolverLoader.loadLoadable(ITextAnalyzer.class);

		for (String textAnalyzer : TextExtractorConfig.TEXT_EXTRACTION_AGENT_ANALYZERS) {
			if (!myAnalyzers.containsKey(textAnalyzer)) {
				throw new IllegalArgumentException("TextAnalyzer " + textAnalyzer + " not found");
			}
			analyzers.add(myAnalyzers.get(textAnalyzer).create(textExtractionState));
		}

		Map<String, ITextSolver> mySolvers = AnalyzerSolverLoader.loadLoadable(ITextSolver.class);

		for (String textSolver : TextExtractorConfig.TEXT_EXTRACTION_AGENT_SOLVERS) {
			if (!mySolvers.containsKey(textSolver)) {
				throw new IllegalArgumentException("TextSolver " + textSolver + " not found");
			}
			solvers.add(mySolvers.get(textSolver).create(textExtractionState));
		}
	}

	@Override
	public void runAnalyzers() {
		for (IWord n : graph.getNodes()) {
			for (ITextAnalyzer analyzer : analyzers) {
				analyzer.exec(n);
			}
		}
	}

	@Override
	public void runSolvers() {
		for (ITextSolver solver : solvers) {
			solver.exec();
		}
	}

	@Override
	public ITextExtractionState getState() {
		return this.textExtractionState;
	}

}
