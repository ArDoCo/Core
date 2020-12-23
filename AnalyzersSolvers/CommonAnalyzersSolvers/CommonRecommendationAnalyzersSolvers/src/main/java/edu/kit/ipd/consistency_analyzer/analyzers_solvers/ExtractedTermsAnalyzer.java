package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.consistency_analyzer.common.SimilarityUtils;
import edu.kit.ipd.consistency_analyzer.datastructures.IModelExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.INounMapping;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendationState;
import edu.kit.ipd.consistency_analyzer.datastructures.ITermMapping;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IWord;
import edu.kit.ipd.consistency_analyzer.datastructures.MappingKind;

/**
 * This analyzer identifies terms and examines their textual environment.
 *
 * @author Sophie
 * 
 */
@MetaInfServices(IRecommendationAnalyzer.class)
public class ExtractedTermsAnalyzer extends RecommendationAnalyzer {

	private double probabilityAdjacentTerm = GenericRecommendationAnalyzerSolverConfig.EXTRACTED_TERMS_ANALYZER_PROBABILITY_ADJACENT_TERM;
	private double probabilityJustName = GenericRecommendationAnalyzerSolverConfig.EXTRACTED_TERMS_ANALYZER_PROBABILITY_JUST_NAME;
	private double probabilityJustAdjacentNoun = GenericRecommendationAnalyzerSolverConfig.EXTRACTED_TERMS_ANALYZER_PROBABILITY_ADJACENT_NOUN;

	/**
	 * Instantiates a new extracted terms analyzer
	 *
	 * @param graph                the PARSE graph to work with
	 * @param textExtractionState  the text extraction state to work with
	 * @param modelExtractionState the model extractin state to work with
	 * @param recommendationState  the recommendation state to write the results and
	 *                             read existing recommendations from
	 */
	public ExtractedTermsAnalyzer(//
			ITextExtractionState textExtractionState, //
			IModelExtractionState modelExtractionState, IRecommendationState recommendationState) {
		super(DependencyType.TEXT_RECOMMENDATION, textExtractionState, modelExtractionState, recommendationState);
	}

	public ExtractedTermsAnalyzer() {
		this(null, null, null);
	}

	@Override
	public IRecommendationAnalyzer create(ITextExtractionState textExtractionState, IModelExtractionState modelExtractionState, IRecommendationState recommendationState) {
		return new ExtractedTermsAnalyzer(textExtractionState, modelExtractionState, recommendationState);
	}

	@Override
	public void exec(IWord n) {

		createRecommendedInstancesForTerm(n);
	}

	private void createRecommendedInstancesForTerm(IWord node) {

		List<ITermMapping> termMappings = textExtractionState.getTermMappingsByNode(node);

		termMappings = getPossibleOccurredTermMappingsToThisSpot(termMappings, node);

		if (termMappings.isEmpty()) {
			return;

		}
		for (ITermMapping term : termMappings) {

			List<INounMapping> adjacentNounMappings = getTermAdjacentNounMappings(node, term);
			if (adjacentNounMappings.isEmpty() && term.getKind().equals(MappingKind.NAME)) {

				recommendationState.addRecommendedInstanceJustName(term.getReference(), probabilityJustName, term.getMappings());

			} else {
				createRecommendedInstancesForSurroundingNounMappings(term, adjacentNounMappings);

				List<ITermMapping> adjacentTermMappings = new ArrayList<>();

				for (INounMapping surroundingMapping : adjacentNounMappings) {
					adjacentTermMappings.addAll(textExtractionState.getTermsByContainedMapping(surroundingMapping));
				}

				createRecommendedInstancesForAdjacentTermMappings(node, term, adjacentTermMappings);
			}
		}

	}

	private List<ITermMapping> getPossibleOccurredTermMappingsToThisSpot(List<ITermMapping> termMappings, IWord n) {

		List<ITermMapping> possibleOccuredTermMappings = new ArrayList<>();
		String word = n.getText();

		for (ITermMapping term : termMappings) {
			List<INounMapping> termNounMappings = new ArrayList<>(term.getMappings());

			List<INounMapping> wordMappings = SimilarityUtils.getMostLikelyNMappingsByReference(word, termNounMappings);

			for (INounMapping wordMapping : wordMappings) {
				boolean stop = false;

				int position = termNounMappings.indexOf(wordMapping);

				IWord currentNode = n;

				for (int i = position - 1; i >= 0 && !stop; i--) {
					String preWord = currentNode.getPreWord().getText();
					String reference = termNounMappings.get(i).getReference();
					if (!SimilarityUtils.areWordsSimilar(reference, preWord)) {
						stop = true;
					}
				}

				currentNode = n;

				for (int i = position + 1; i < termNounMappings.size() && !stop; i++) {
					String postWord = currentNode.getNextWord().getText();
					String reference = termNounMappings.get(i).getReference();

					if (!SimilarityUtils.areWordsSimilar(reference, postWord)) {
						stop = true;
					}
				}

				if (!stop) {
					possibleOccuredTermMappings.add(term);
					break; // TODO: poss. rework -> assumption: There is only one corresponding term
							// mapping -> change return type
				}

			}

		}
		return possibleOccuredTermMappings;

	}

	private void createRecommendedInstancesForAdjacentTermMappings(IWord termStartNode, ITermMapping term, List<ITermMapping> adjacentTermMappings) {

		List<ITermMapping> adjCompleteTermMappings = new ArrayList<>();
		adjCompleteTermMappings.addAll(getCompletePreAdjTermMappings(adjacentTermMappings, termStartNode));
		adjCompleteTermMappings.addAll(getCompleteAfterAdjTermMappings(adjacentTermMappings, termStartNode, term));

		createRecommendedInstancesOfAdjacentTerms(term, adjCompleteTermMappings);
	}

	private List<ITermMapping> getCompletePreAdjTermMappings(List<ITermMapping> possibleTermMappings, IWord termStartNode) {
		int sentence = termStartNode.getSentenceNo();
		IWord preTermNode = termStartNode.getPreWord();

		List<ITermMapping> adjCompleteTermMappings = new ArrayList<>();

		for (ITermMapping adjTerm : possibleTermMappings) {

			List<INounMapping> nounMappings = new ArrayList<>(adjTerm.getMappings());

			while (!nounMappings.isEmpty()) {
				INounMapping resultOfPreMatch = matchNode(nounMappings, preTermNode);

				if (sentence == preTermNode.getSentenceNo()) {
					break;
				}

				if (resultOfPreMatch != null) {
					nounMappings.remove(resultOfPreMatch);
					preTermNode = preTermNode.getPreWord();
				} else {
					break;
				}
			}
			if (nounMappings.isEmpty()) {
				adjCompleteTermMappings.add(adjTerm);
			}
		}

		return adjCompleteTermMappings;
	}

	private List<ITermMapping> getCompleteAfterAdjTermMappings(List<ITermMapping> possibleTermMappings, IWord termStartNode, ITermMapping term) {

		int sentence = termStartNode.getSentenceNo();
		IWord afterTermNode = getAfterTermNode(termStartNode, term);

		List<ITermMapping> adjCompleteTermMappings = new ArrayList<>();

		for (ITermMapping adjTerm : possibleTermMappings) {

			List<INounMapping> nounMappings = new ArrayList<>(adjTerm.getMappings());

			while (!nounMappings.isEmpty()) {
				INounMapping resultOfPostMatch = matchNode(nounMappings, afterTermNode);

				if (sentence == afterTermNode.getSentenceNo()) {
					break;
				}

				if (resultOfPostMatch != null) {
					nounMappings.remove(resultOfPostMatch);
					afterTermNode = afterTermNode.getNextWord();
				} else {
					break;
				}

			}
			if (nounMappings.isEmpty()) {
				adjCompleteTermMappings.add(adjTerm);
			}

		}

		return adjCompleteTermMappings;

	}

	private INounMapping matchNode(List<INounMapping> nounMappings, IWord node) {
		for (INounMapping mapping : nounMappings) {
			if (mapping.getNodes().contains(node)) {
				return mapping;
			}
		}
		return null;
	}

	private IWord getAfterTermNode(IWord termStartNode, ITermMapping term) {
		IWord afterTermNode = termStartNode.getNextWord();
		for (int i = 0; i < term.getMappings().size(); i++) {
			afterTermNode = afterTermNode.getNextWord();
		}
		return afterTermNode;
	}

	private List<INounMapping> getTermAdjacentNounMappings(IWord node, ITermMapping term) {

		MappingKind kind = term.getKind();
		IWord preTermNode = node.getPreWord();
		IWord afterTermNode = getAfterTermNode(node, term);
		int sentence = node.getSentenceNo();

		List<INounMapping> possibleMappings = new ArrayList<>();

		if (sentence == preTermNode.getSentenceNo()) {

			List<INounMapping> nounMappingsOfPreTermNode = textExtractionState.getNounMappingsByNode(preTermNode);
			possibleMappings.addAll(nounMappingsOfPreTermNode);
		}
		if (sentence == afterTermNode.getSentenceNo()) {
			List<INounMapping> nounMappingsOfAfterTermNode = textExtractionState.getNounMappingsByNode(afterTermNode);
			possibleMappings.addAll(nounMappingsOfAfterTermNode);
		}
		possibleMappings = possibleMappings.stream().filter(nounMapping -> !nounMapping.getKind().equals(kind)).collect(Collectors.toList());

		return possibleMappings;
	}

	private void createRecommendedInstancesOfAdjacentTerms(ITermMapping term, List<ITermMapping> adjacentTerms) {
		MappingKind kind = term.getKind();

		if (kind.equals(MappingKind.NAME) && !adjacentTerms.isEmpty()) {
			for (ITermMapping adjTerm : adjacentTerms) {
				recommendationState.addRecommendedInstance(term.getReference(), adjTerm.getReference(), probabilityAdjacentTerm, term.getMappings(), adjTerm.getMappings());

			}
		} else if (kind.equals(MappingKind.TYPE) && !adjacentTerms.isEmpty()) {
			for (ITermMapping adjTerm : adjacentTerms) {
				recommendationState.addRecommendedInstance(adjTerm.getReference(), term.getReference(), probabilityAdjacentTerm, adjTerm.getMappings(), term.getMappings());
			}

		}
	}

	private void createRecommendedInstancesForSurroundingNounMappings(ITermMapping term, List<INounMapping> surroundingNounMappings) {
		MappingKind kind = term.getKind();

		if (kind.equals(MappingKind.NAME) && !surroundingNounMappings.isEmpty()) {
			for (INounMapping nounMapping : surroundingNounMappings) {

				recommendationState.addRecommendedInstance(term.getReference(), nounMapping.getReference(), probabilityJustAdjacentNoun, term.getMappings(), List.of(nounMapping));
			}
		} else if (kind.equals(MappingKind.TYPE) && !surroundingNounMappings.isEmpty()) {
			for (INounMapping nounMapping : surroundingNounMappings) {

				recommendationState.addRecommendedInstance(nounMapping.getReference(), term.getReference(), probabilityJustAdjacentNoun, List.of(nounMapping), term.getMappings());
			}
		}

	}

}
