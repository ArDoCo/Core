package modelconnector.recommendationGenerator.analyzers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import modelconnector.DependencyType;
import modelconnector.helpers.GraphUtils;
import modelconnector.helpers.ModelConnectorConfiguration;
import modelconnector.helpers.SimilarityUtils;
import modelconnector.modelExtractor.state.ModelExtractionState;
import modelconnector.recommendationGenerator.state.RecommendationState;
import modelconnector.textExtractor.state.MappingKind;
import modelconnector.textExtractor.state.NounMapping;
import modelconnector.textExtractor.state.TermMapping;
import modelconnector.textExtractor.state.TextExtractionState;

/**
 * This analyzer identifies terms and examines their textual environment.
 *
 * @author Sophie
 *
 */
public class ExtractedTermsAnalyzer extends RecommendationAnalyzer {

	private double probabilityAdjacentTerm = ModelConnectorConfiguration.extractedTermsAnalyzer_ProbabilityAdjacentTerm;
	private double probabilityJustName = ModelConnectorConfiguration.extractedTermsAnalyzer_ProbabilityJustName;
	private double probabilityJustAdjacentNoun = ModelConnectorConfiguration.extractedTermsAnalyzer_ProbabilityAdjacentNoun;

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
			IGraph graph, TextExtractionState textExtractionState, //
			ModelExtractionState modelExtractionState, RecommendationState recommendationState) {
		super(DependencyType.TEXT_RECOMMENDATION, graph, textExtractionState, modelExtractionState, recommendationState);
	}

	@Override
	public void exec(INode n) {

		createRecommendedInstancesForTerm(n);
	}

	private void createRecommendedInstancesForTerm(INode node) {

		List<TermMapping> termMappings = this.textExtractionState.getTermMappingsByNode(node);

		termMappings = getPossibleOccurredTermMappingsToThisSpot(termMappings, node);

		if (termMappings.size() < 1) {
			return;

		}
		for (TermMapping term : termMappings) {

			List<NounMapping> adjacentNounMappings = getTermAdjacentNounMappings(node, term);
			if (adjacentNounMappings.isEmpty() && term.getKind().equals(MappingKind.NAME)) {

				recommendationState.addRecommendedInstanceJustName(term.getReference(), probabilityJustName, term.getMappings());

			} else {
				createRecommendedInstancesForSurroundingNounMappings(term, adjacentNounMappings);

				List<TermMapping> adjacentTermMappings = new ArrayList<>();

				for (NounMapping surroundingMapping : adjacentNounMappings) {
					adjacentTermMappings.addAll(textExtractionState.getTermsByContainedMapping(surroundingMapping));
				}

				createRecommendedInstancesForAdjacentTermMappings(node, term, adjacentTermMappings);
			}
		}

	}

	private List<TermMapping> getPossibleOccurredTermMappingsToThisSpot(List<TermMapping> termMappings, INode n) {

		List<TermMapping> possibleOccuredTermMappings = new ArrayList<>();
		String word = GraphUtils.getNodeValue(n);

		for (TermMapping term : termMappings) {
			List<NounMapping> termNounMappings = new ArrayList<>(term.getMappings());

			List<NounMapping> wordMappings = SimilarityUtils.getMostLikelyNMappingsByReference(word, termNounMappings);
			boolean stop = false;

			for (NounMapping wordMapping : wordMappings) {

				if (stop) {
					break;
				}

				int position = termNounMappings.indexOf(wordMapping);

				INode currentNode = n;

				for (int i = position - 1; i >= 0 && !stop; i--) {
					String preWord = GraphUtils.getNodeValue(GraphUtils.getPreviousNode(currentNode, relArcType));
					if (SimilarityUtils.areWordsSimilar(termNounMappings.get(i).getReference(), preWord)) {
						continue;
					} else {
						stop = true;
						break;
					}
				}

				currentNode = n;

				for (int i = position + 1; i < termNounMappings.size() && !stop; i++) {
					String postWord = GraphUtils.getNodeValue(GraphUtils.getNextNode(currentNode, relArcType));
					if (SimilarityUtils.areWordsSimilar(termNounMappings.get(i).getReference(), postWord)) {
						continue;
					} else {
						stop = true;
						break;
					}
				}

				if (!stop) {
					possibleOccuredTermMappings.add(term);
					break;
				}

			}

		}
		return possibleOccuredTermMappings;

	}

	private void createRecommendedInstancesForAdjacentTermMappings(INode termStartNode, TermMapping term, List<TermMapping> adjacentTermMappings) {

		List<TermMapping> adjCompleteTermMappings = new ArrayList<>();
		adjCompleteTermMappings.addAll(this.getCompletePreAdjTermMappings(adjacentTermMappings, termStartNode, term));
		adjCompleteTermMappings.addAll(this.getCompleteAfterAdjTermMappings(adjacentTermMappings, termStartNode, term));

		createRecommendedInstancesOfAdjacentTerms(term, adjCompleteTermMappings);
	}

	private List<TermMapping> getCompletePreAdjTermMappings(List<TermMapping> possibleTermMappings, INode termStartNode, TermMapping term) {
		String sentence = termStartNode.getAttributeValue("sentenceNumber").toString();
		INode preTermNode = getPreTermNode(termStartNode, term);

		List<TermMapping> adjCompleteTermMappings = new ArrayList<>();

		for (TermMapping adjTerm : possibleTermMappings) {

			List<NounMapping> nounMappings = new ArrayList<>(adjTerm.getMappings());

			while (nounMappings.size() == 0) {
				NounMapping resultOfPreMatch = matchNode(nounMappings, preTermNode);

				if (sentence.contentEquals(preTermNode.getAttributeValue("sentenceNumber").toString())) {
					break;
				}

				if (resultOfPreMatch != null) {
					nounMappings.remove(resultOfPreMatch);
					preTermNode = GraphUtils.getPreviousNode(preTermNode, relArcType);
				} else {
					break;
				}
			}
			if (nounMappings.size() == 0) {
				adjCompleteTermMappings.add(adjTerm);
			}
		}

		return adjCompleteTermMappings;
	}

	private List<TermMapping> getCompleteAfterAdjTermMappings(List<TermMapping> possibleTermMappings, INode termStartNode, TermMapping term) {

		String sentence = termStartNode.getAttributeValue("sentenceNumber").toString();
		INode afterTermNode = getAfterTermNode(termStartNode, term);

		List<TermMapping> adjCompleteTermMappings = new ArrayList<>();

		for (TermMapping adjTerm : possibleTermMappings) {

			List<NounMapping> nounMappings = new ArrayList<>(adjTerm.getMappings());

			while (nounMappings.size() == 0) {
				NounMapping resultOfPostMatch = matchNode(nounMappings, afterTermNode);

				if (sentence.contentEquals(afterTermNode.getAttributeValue("sentenceNumber").toString())) {
					break;
				}

				if (resultOfPostMatch != null) {
					nounMappings.remove(resultOfPostMatch);
					afterTermNode = GraphUtils.getNextNode(afterTermNode, relArcType);
				} else {
					break;
				}

			}
			if (nounMappings.size() == 0) {
				adjCompleteTermMappings.add(adjTerm);
			}

		}

		return adjCompleteTermMappings;

	}

	private NounMapping matchNode(List<NounMapping> nounMappings, INode node) {
		for (NounMapping mapping : nounMappings) {
			if (mapping.getNodes().contains(node)) {
				return mapping;
			}
		}
		return null;
	}

	private INode getPreTermNode(INode termStartNode, TermMapping term) {
		return GraphUtils.getPreviousNode(termStartNode, relArcType);
	}

	private INode getAfterTermNode(INode termStartNode, TermMapping term) {
		INode afterTermNode = GraphUtils.getNextNode(termStartNode, relArcType);
		for (int i = 0; i < term.getMappings().size(); i++) {
			afterTermNode = GraphUtils.getNextNode(afterTermNode, relArcType);
		}
		return afterTermNode;
	}

	private List<NounMapping> getTermAdjacentNounMappings(INode node, TermMapping term) {

		MappingKind kind = term.getKind();
		INode preTermNode = getPreTermNode(node, term);
		INode afterTermNode = getAfterTermNode(node, term);
		String sentence = node.getAttributeValue("sentenceNumber").toString();

		List<NounMapping> possibleMappings = new ArrayList<>();

		if (sentence.contentEquals(preTermNode.getAttributeValue("sentenceNumber").toString())) {

			List<NounMapping> nounMappingsOfPreTermNode = this.textExtractionState.getNounMappingsByNode(preTermNode);
			possibleMappings.addAll(nounMappingsOfPreTermNode);
		}
		if (sentence.contentEquals(afterTermNode.getAttributeValue("sentenceNumber").toString())) {
			List<NounMapping> nounMappingsOfAfterTermNode = this.textExtractionState.getNounMappingsByNode(afterTermNode);
			possibleMappings.addAll(nounMappingsOfAfterTermNode);
		}
		possibleMappings = possibleMappings.stream().filter(nounMapping -> !nounMapping.getKind().equals(kind)).collect(Collectors.toList());

		return possibleMappings;
	}

	private void createRecommendedInstancesOfAdjacentTerms(TermMapping term, List<TermMapping> adjacentTerms) {
		MappingKind kind = term.getKind();

		if (kind.equals(MappingKind.NAME) && !adjacentTerms.isEmpty()) {
			for (TermMapping adjTerm : adjacentTerms) {
				recommendationState.addRecommendedInstance(term.getReference(), adjTerm.getReference(), probabilityAdjacentTerm, term.getMappings(), adjTerm.getMappings());

			}
		} else if (kind.equals(MappingKind.TYPE) && !adjacentTerms.isEmpty()) {
			for (TermMapping adjTerm : adjacentTerms) {
				recommendationState.addRecommendedInstance(adjTerm.getReference(), term.getReference(), probabilityAdjacentTerm, adjTerm.getMappings(), term.getMappings());
			}

		}
	}

	private void createRecommendedInstancesForSurroundingNounMappings(TermMapping term, List<NounMapping> surroundingNounMappings) {
		MappingKind kind = term.getKind();

		if (kind.equals(MappingKind.NAME) && !surroundingNounMappings.isEmpty()) {
			for (NounMapping nounMapping : surroundingNounMappings) {

				recommendationState.addRecommendedInstance(term.getReference(), nounMapping.getReference(), probabilityJustAdjacentNoun, term.getMappings(), List.of(nounMapping));
			}
		} else if (kind.equals(MappingKind.TYPE) && !surroundingNounMappings.isEmpty()) {
			for (NounMapping nounMapping : surroundingNounMappings) {

				recommendationState.addRecommendedInstance(nounMapping.getReference(), term.getReference(), probabilityJustAdjacentNoun, List.of(nounMapping), term.getMappings());
			}
		}

	}

}
