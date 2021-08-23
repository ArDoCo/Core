package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.extractors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.GenericConnectionConfig;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.common.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITermMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.ConnectionExtractor;

/**
 * This analyzer identifies terms and examines their textual environment.
 *
 * @author Sophie
 *
 */
@MetaInfServices(ConnectionExtractor.class)
public class ExtractedTermsExtractor extends ConnectionExtractor {

    private double probabilityAdjacentTerm;
    private double probabilityJustName;
    private double probabilityJustAdjacentNoun; // TODO can we make this probability more flexible/dynamic? It is
                                                // responsible for a lot of RI (incl. false positives)

    /**
     * Instantiates a new extracted terms analyzer.
     *
     * @param textExtractionState  the state that contains all information from the text
     * @param modelExtractionState the state that contains all information from the architecture model
     * @param recommendationState  the state that contains all recommended instances and relations
     * @param connectionState      the state that contains all information on possible trace links
     */
    public ExtractedTermsExtractor(ITextState textExtractionState, IModelState modelExtractionState, IRecommendationState recommendationState,
            IConnectionState connectionState) {
        this(textExtractionState, modelExtractionState, recommendationState, connectionState, GenericConnectionConfig.DEFAULT_CONFIG);
    }

    /**
     * Instantiates a new extracted terms analyzer.
     *
     * @param textExtractionState  the state that contains all information from the text
     * @param modelExtractionState the state that contains all information from the architecture model
     * @param recommendationState  the state that contains all recommended instances and relations
     * @param connectionState      the state that contains all information on possible trace links
     * @param config               the configuration to be used
     */
    public ExtractedTermsExtractor(ITextState textExtractionState, IModelState modelExtractionState, IRecommendationState recommendationState,
            IConnectionState connectionState, GenericConnectionConfig config) {
        super(textExtractionState, modelExtractionState, recommendationState, connectionState);
        probabilityAdjacentTerm = config.extractedTermsAnalyzerProbabilityAdjacentTerm;
        probabilityJustName = config.extractedTermsAnalyzerProbabilityJustName;
        probabilityJustAdjacentNoun = config.extractedTermsAnalyzerProbabilityAdjacentNoun;
    }

    /**
     * For deserialization.
     */
    public ExtractedTermsExtractor() {
        this(null, null, null, null);
    }

    @Override
    public ConnectionExtractor create(ITextState textState, IModelState modelExtractionState, IRecommendationState recommendationState,
            IConnectionState connectionState, Configuration config) {
        return new ExtractedTermsExtractor(textState, modelExtractionState, recommendationState, connectionState, (GenericConnectionConfig) config);
    }

    @Override
    public void exec(IWord n) {
        createRecommendedInstancesForTerm(n);
    }

    private void createRecommendedInstancesForTerm(IWord node) {

        ImmutableList<ITermMapping> termMappings = textState.getTermMappingsByNode(node);

        termMappings = getPossibleOccurredTermMappingsToThisSpot(termMappings, node);

        if (termMappings.isEmpty()) {
            return;
        }

        for (ITermMapping term : termMappings) {
            ImmutableList<INounMapping> adjacentNounMappings = getTermAdjacentNounMappings(node, term);
            if (adjacentNounMappings.isEmpty() && MappingKind.NAME == term.getKind()) {
                recommendationState.addRecommendedInstanceJustName(term.getReference(), probabilityJustName, term.getMappings());
            } else {
                createRecommendedInstancesForSurroundingNounMappings(term, adjacentNounMappings);
                MutableList<ITermMapping> adjacentTermMappings = Lists.mutable.empty();
                for (INounMapping surroundingMapping : adjacentNounMappings) {
                    adjacentTermMappings.addAll(textState.getTermsByContainedMapping(surroundingMapping).castToCollection());
                }
                createRecommendedInstancesForAdjacentTermMappings(node, term, adjacentTermMappings.toImmutable());
            }
        }

    }

    private static ImmutableList<ITermMapping> getPossibleOccurredTermMappingsToThisSpot(ImmutableList<ITermMapping> termMappings, IWord n) {

        MutableList<ITermMapping> possibleOccuredTermMappings = Lists.mutable.empty();
        String word = n.getText();

        for (ITermMapping term : termMappings) {
            ImmutableList<INounMapping> termNounMappings = Lists.immutable.withAll(term.getMappings());

            ImmutableList<INounMapping> wordMappings = SimilarityUtils.getMostLikelyNMappingsByReference(word, termNounMappings);

            for (INounMapping wordMapping : wordMappings) {

                int position = termNounMappings.indexOf(wordMapping);

                if (!arePreviousWordsEqualToReferences(termNounMappings, n, position) && !areNextWordsEqualToReferences(termNounMappings, n, position)) {
                    possibleOccuredTermMappings.add(term);
                    break;
                    // TODO: poss. rework -> assumption: There is only one corresponding term
                    // mapping -> change return type
                }

            }

        }
        return possibleOccuredTermMappings.toImmutable();

    }

    private static boolean areNextWordsEqualToReferences(ImmutableList<INounMapping> references, IWord currentWord, int currentPosition) {
        var stop = false;

        for (int i = currentPosition + 1; i < references.size() && !stop; i++) {
            var nextWord = currentWord.getNextWord();
            if (nextWord == null) {
                continue;
            }
            String postWord = nextWord.getText();
            String reference = references.get(i).getReference();

            if (!SimilarityUtils.areWordsSimilar(reference, postWord)) {
                stop = true;
            }
        }

        return stop;
    }

    private static boolean arePreviousWordsEqualToReferences(ImmutableList<INounMapping> references, IWord currentWord, int currentPosition) {
        var stop = false;

        for (int i = currentPosition - 1; i >= 0 && !stop; i--) {
            IWord previousWord = currentWord.getPreWord();
            if (previousWord == null) {
                continue;
            }
            String preWord = previousWord.getText();
            String reference = references.get(i).getReference();
            if (!SimilarityUtils.areWordsSimilar(reference, preWord)) {
                stop = true;
            }
        }

        return stop;
    }

    private void createRecommendedInstancesForAdjacentTermMappings(IWord termStartNode, ITermMapping term, ImmutableList<ITermMapping> adjacentTermMappings) {

        MutableList<ITermMapping> adjCompleteTermMappings = Lists.mutable.withAll(getCompletePreAdjTermMappings(adjacentTermMappings, termStartNode));
        adjCompleteTermMappings.addAll(getCompleteAfterAdjTermMappings(adjacentTermMappings, termStartNode, term).castToCollection());

        createRecommendedInstancesOfAdjacentTerms(term, adjCompleteTermMappings.toImmutable());
    }

    private static ImmutableList<ITermMapping> getCompletePreAdjTermMappings(ImmutableList<ITermMapping> possibleTermMappings, IWord termStartNode) {
        int sentence = termStartNode.getSentenceNo();
        IWord preTermNode = termStartNode.getPreWord();

        MutableList<ITermMapping> adjCompleteTermMappings = Lists.mutable.empty();

        for (ITermMapping adjTerm : possibleTermMappings) {

            MutableList<INounMapping> nounMappings = Lists.mutable.withAll(adjTerm.getMappings());

            while (!nounMappings.isEmpty()) {
                INounMapping resultOfPreMatch = matchNode(nounMappings.toImmutable(), preTermNode);

                if (preTermNode == null || sentence == preTermNode.getSentenceNo() || resultOfPreMatch == null) {
                    break;
                }

                nounMappings.remove(resultOfPreMatch);
                preTermNode = preTermNode.getPreWord();

            }
            if (nounMappings.isEmpty()) {
                adjCompleteTermMappings.add(adjTerm);
            }
        }

        return adjCompleteTermMappings.toImmutable();
    }

    private static ImmutableList<ITermMapping> getCompleteAfterAdjTermMappings(ImmutableList<ITermMapping> possibleTermMappings, IWord termStartNode,
            ITermMapping term) {

        int sentence = termStartNode.getSentenceNo();
        IWord afterTermNode = getAfterTermNode(termStartNode, term);

        MutableList<ITermMapping> adjCompleteTermMappings = Lists.mutable.empty();

        if (afterTermNode == null) {
            return adjCompleteTermMappings.toImmutable();
        }

        for (ITermMapping adjTerm : possibleTermMappings) {

            MutableList<INounMapping> nounMappings = Lists.mutable.withAll(adjTerm.getMappings());

            while (!nounMappings.isEmpty()) {
                INounMapping resultOfPostMatch = matchNode(nounMappings.toImmutable(), afterTermNode);

                if (sentence == afterTermNode.getSentenceNo() || resultOfPostMatch == null) {
                    break;
                }
                nounMappings.remove(resultOfPostMatch);
                afterTermNode = afterTermNode.getNextWord();

            }
            if (nounMappings.isEmpty()) {
                adjCompleteTermMappings.add(adjTerm);
            }

        }

        return adjCompleteTermMappings.toImmutable();

    }

    private static INounMapping matchNode(ImmutableList<INounMapping> nounMappings, IWord node) {
        if (node == null) {
            return null;
        }
        for (INounMapping mapping : nounMappings) {
            if (mapping.getWords().contains(node)) {
                return mapping;
            }
        }
        return null;
    }

    private static IWord getAfterTermNode(IWord termStartNode, ITermMapping term) {
        IWord afterTermNode = termStartNode.getNextWord();
        for (var i = 0; i < term.getMappings().size() && afterTermNode != null; i++) {
            afterTermNode = afterTermNode.getNextWord();
        }

        return afterTermNode;
    }

    private ImmutableList<INounMapping> getTermAdjacentNounMappings(IWord node, ITermMapping term) {

        MappingKind kind = term.getKind();
        IWord preTermNode = node.getPreWord();
        IWord afterTermNode = getAfterTermNode(node, term);
        int sentence = node.getSentenceNo();

        MutableList<INounMapping> possibleMappings = Lists.mutable.empty();

        if (preTermNode != null && sentence == preTermNode.getSentenceNo()) {

            ImmutableList<INounMapping> nounMappingsOfPreTermNode = textState.getNounMappingsByWord(preTermNode);
            possibleMappings.addAll(nounMappingsOfPreTermNode.castToCollection());
        }
        if (afterTermNode != null && sentence == afterTermNode.getSentenceNo()) {
            ImmutableList<INounMapping> nounMappingsOfAfterTermNode = textState.getNounMappingsByWord(afterTermNode);
            possibleMappings.addAll(nounMappingsOfAfterTermNode.castToCollection());
        }
        return possibleMappings.select(nounMapping -> nounMapping.getKind() != kind).toImmutable();
    }

    private void createRecommendedInstancesOfAdjacentTerms(ITermMapping term, ImmutableList<ITermMapping> adjacentTerms) {
        MappingKind kind = term.getKind();

        if (MappingKind.NAME == kind && !adjacentTerms.isEmpty()) {
            for (ITermMapping adjTerm : adjacentTerms) {
                recommendationState.addRecommendedInstance(term.getReference(), adjTerm.getReference(), probabilityAdjacentTerm, term.getMappings(),
                        adjTerm.getMappings());

            }
        } else if (MappingKind.TYPE == kind && !adjacentTerms.isEmpty()) {
            for (ITermMapping adjTerm : adjacentTerms) {
                recommendationState.addRecommendedInstance(adjTerm.getReference(), term.getReference(), probabilityAdjacentTerm, adjTerm.getMappings(),
                        term.getMappings());
            }

        }
    }

    private void createRecommendedInstancesForSurroundingNounMappings(ITermMapping term, ImmutableList<INounMapping> surroundingNounMappings) {
        MappingKind kind = term.getKind();

        if (MappingKind.NAME == kind && !surroundingNounMappings.isEmpty()) {
            for (INounMapping nounMapping : surroundingNounMappings) {

                recommendationState.addRecommendedInstance(term.getReference(), nounMapping.getReference(), probabilityJustAdjacentNoun, term.getMappings(),
                        Lists.immutable.with(nounMapping));
            }
        } else if (MappingKind.TYPE == kind && !surroundingNounMappings.isEmpty()) {
            for (INounMapping nounMapping : surroundingNounMappings) {

                recommendationState.addRecommendedInstance(nounMapping.getReference(), term.getReference(), probabilityJustAdjacentNoun,
                        Lists.immutable.with(nounMapping), term.getMappings());
            }
        }

    }

}
