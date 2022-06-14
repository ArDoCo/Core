/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.RecommendationAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.RecommendationAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.data.diagram.IBox;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendedInstance;

public class DiagramRecommendationAgent extends RecommendationAgent {

    @Configurable
    private double positiveBoostProbability = 0.9;

    @Configurable
    private double negativeBoostProbability = 0;

    @Configurable
    private boolean useColorGroups = true;
    @Configurable
    private boolean useMergeAll = false;

    /**
     * This value defines at which overlap of words in a color group with RI, the RIs get a positive boost.
     */
    @Configurable
    private double colorGroupMatchingThreshold = 0.49;

    @Configurable
    private boolean enabled = true;

    @Override
    public void execute(RecommendationAgentData data) {
        if (!enabled)
            return;

        if (data.getDiagramDirectory() == null || data.getDiagramDetectionState() == null) {
            logger.debug("Skipping execution of {}", this.getId());
            return;
        }

        var diagramState = data.getDiagramDetectionState();
        for (var diagram : diagramState.getDiagramIds()) {
            // TODO For now we assume the architectural sketches belong to architecture not code.
            processDiagram(diagramState.detectedBoxes(diagram), data.getRecommendationState(Metamodel.ARCHITECTURE));
        }

    }

    private void processDiagram(List<IBox> diagram, IRecommendationState recommendationState) {
        if (useColorGroups)
            processUsingColorGroups(diagram, recommendationState);

        if (useMergeAll)
            processUsingMergedWordLists(diagram, recommendationState);
        /*
         * for (var word : interestingWords) { var recommendations =
         * recommendationState.getRecommendedInstancesBySimilarName(word);
         *
         * if (!recommendations.isEmpty()) {
         * logger.debug("Modifying RecommendedInstances according to Sketches & Diagrams: {}", recommendations); for
         * (var recommendation : recommendations) recommendation.addProbability(this, probability); } }
         */
    }

    private void processUsingMergedWordLists(List<IBox> diagram, IRecommendationState recommendationState) {
        var interestingWords = diagram.stream().flatMap(it -> it.getWordsThatBelongToThisBox().stream()).toList();
        var matchingRIs = getRecommendedInstancesByBoxWords(interestingWords, recommendationState);
        var notMatchingRIs = recommendationState.getRecommendedInstances().stream().filter(it -> !matchingRIs.contains(it)).toList();

        for (var notMatching : notMatchingRIs)
            notMatching.addProbability(this, negativeBoostProbability);

        for (var matching : matchingRIs)
            matching.addProbability(this, positiveBoostProbability);
    }

    private void processUsingColorGroups(List<IBox> diagram, IRecommendationState recommendationState) {
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

    private Map<Color, Double> calculateMatchingScore(Map<Color, List<String>> wordsOfBoxesByColor, IRecommendationState recommendationState) {
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

    private List<IRecommendedInstance> getRecommendedInstancesByBoxWords(List<String> words, IRecommendationState recommendationState) {
        return words.stream().flatMap(iw -> recommendationState.getRecommendedInstancesBySimilarName(iw).stream()).distinct().toList();
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // NOP
    }
}
