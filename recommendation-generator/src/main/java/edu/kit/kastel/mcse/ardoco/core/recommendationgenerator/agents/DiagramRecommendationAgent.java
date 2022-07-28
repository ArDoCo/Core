/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.api.data.diagram.Box;
import edu.kit.kastel.mcse.ardoco.core.api.data.diagram.DiagramDetectionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;

public class DiagramRecommendationAgent extends PipelineAgent {

    @Configurable
    private double positiveBoostProbability = 0.9;

    @Configurable
    private double negativeBoostProbability = 0;

    @Configurable
    private boolean useColorGroups = true;
    @Configurable
    private boolean useMergeAll = false;

    @Configurable
    private boolean deleteRIsWithLowConfidence = false;
    @Configurable
    private double minConfidenceToPreventDeletion = 0.6;

    /**
     * This value defines at which overlap of words in a color group with RI, the RIs get a positive boost.
     */
    @Configurable
    private double colorGroupMatchingThreshold = 0.49;

    @Configurable
    private boolean enabled = true;

    public DiagramRecommendationAgent(DataRepository dataRepository) {
        super(DiagramRecommendationAgent.class.getSimpleName(), dataRepository);
    }

    @Override
    public void run() {
        if (!enabled)
            return;

        var diagramStateOptional = getDataRepository().getData(DiagramDetectionState.ID, DiagramDetectionState.class);

        if (diagramStateOptional.isEmpty()) {
            logger.debug("Skipping execution of {}", this.getId());
            return;
        }

        var diagramState = diagramStateOptional.get();
        for (var diagram : diagramState.getDiagramIds()) {
            // TODO For now we assume the architectural sketches belong to architecture not code.
            processDiagram(diagramState.detectedBoxes(diagram),
                    DataRepositoryHelper.getRecommendationStates(getDataRepository()).getRecommendationState(Metamodel.ARCHITECTURE));
        }

    }

    private void processDiagram(List<Box> diagram, RecommendationState recommendationState) {
        if (useColorGroups)
            processUsingColorGroups(diagram, recommendationState);

        if (useMergeAll)
            processUsingMergedWordLists(diagram, recommendationState);

        if (deleteRIsWithLowConfidence)
            deleteRIsWithLowConfidence(recommendationState);
        /*
         * for (var word : interestingWords) { var recommendations =
         * recommendationState.getRecommendedInstancesBySimilarName(word);
         *
         * if (!recommendations.isEmpty()) {
         * logger.debug("Modifying RecommendedInstances according to Sketches & Diagrams: {}", recommendations); for
         * (var recommendation : recommendations) recommendation.addProbability(this, probability); } }
         */
    }

    private void processUsingMergedWordLists(List<Box> diagram, RecommendationState recommendationState) {
        var interestingWords = diagram.stream().flatMap(it -> it.getWordsThatBelongToThisBox().stream()).toList();
        var matchingRIs = getRecommendedInstancesByBoxWords(interestingWords, recommendationState);
        var notMatchingRIs = recommendationState.getRecommendedInstances().stream().filter(it -> !matchingRIs.contains(it)).toList();

        for (var notMatching : notMatchingRIs)
            notMatching.addProbability(this, negativeBoostProbability);

        for (var matching : matchingRIs)
            matching.addProbability(this, positiveBoostProbability);
    }

    private void processUsingColorGroups(List<Box> diagram, RecommendationState recommendationState) {
        Map<Color, List<String>> wordsOfBoxesByColor = new LinkedHashMap<>();
        for (var box : diagram) {
            for (var text : box.getWordsThatBelongToThisBoxGroupedByColor().entrySet()) {
                if (!wordsOfBoxesByColor.containsKey(text.getKey()))
                    wordsOfBoxesByColor.put(text.getKey(), new ArrayList<>());
                wordsOfBoxesByColor.get(text.getKey()).addAll(text.getValue());
            }
        }

        Map<Color, Double> matchingScore = calculateMatchingScore(wordsOfBoxesByColor, recommendationState);
        for (var colorGroup : matchingScore.entrySet()) {
            var recommendedInstances = getRecommendedInstancesByBoxWords(wordsOfBoxesByColor.get(colorGroup.getKey()), recommendationState);

            if (colorGroup.getValue() > colorGroupMatchingThreshold) {
                // Positive Boost
                recommendedInstances.forEach(ri -> ri.addProbability(this, positiveBoostProbability));
            } else {
                // Negative Boost
                recommendedInstances.forEach(ri -> ri.addProbability(this, negativeBoostProbability));
            }

        }
        // TODO Better Use matching scores to identify types of words
    }

    private Map<Color, Double> calculateMatchingScore(Map<Color, List<String>> wordsOfBoxesByColor, RecommendationState recommendationState) {
        Map<Color, Double> matchings = new LinkedHashMap<>();
        for (var wordGroup : wordsOfBoxesByColor.entrySet()) {
            var matchingRIs = getRecommendedInstancesByBoxWords(wordGroup.getValue(), recommendationState);
            // var notMatchingRIs = recommendationState.getRecommendedInstances().stream().filter(it ->
            // !matchingRIs.contains(it)).toList();
            double score = 1.0 * matchingRIs.size() / wordGroup.getValue().size();
            matchings.put(wordGroup.getKey(), score);
        }
        return matchings;
    }

    private void deleteRIsWithLowConfidence(RecommendationState recommendationState) {
        for (var ri : recommendationState.getRecommendedInstances()) {
            var confidence = ri.getConfidencesForClaimant(this);
            if (ri.getProbability() < minConfidenceToPreventDeletion && confidence.getConfidence() < minConfidenceToPreventDeletion) {
                recommendationState.removeRecommendedInstance(ri);
            }
        }
    }

    private List<RecommendedInstance> getRecommendedInstancesByBoxWords(List<String> words, RecommendationState recommendationState) {
        return words.stream().flatMap(iw -> recommendationState.getRecommendedInstancesBySimilarName(iw).stream()).distinct().toList();
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> map) {
        // NOP
    }
}
