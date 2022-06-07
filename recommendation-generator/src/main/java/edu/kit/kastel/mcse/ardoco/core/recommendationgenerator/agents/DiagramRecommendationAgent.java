/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents;

import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.RecommendationAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.RecommendationAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.data.diagram.IBox;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendationState;

import java.util.List;
import java.util.Map;

public class DiagramRecommendationAgent extends RecommendationAgent {

    @Configurable
    private double probability = 0.9;
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
        var interestingWords = diagram.stream().flatMap(it -> it.getWordsThatBelongToThisBox().stream()).toList();
        var matchingRIs = interestingWords.stream().flatMap(iw -> recommendationState.getRecommendedInstancesByName(iw).stream()).distinct().toList();
        var notMatchingRIs = recommendationState.getRecommendedInstances().stream().filter(it -> !matchingRIs.contains(it)).toList();

        for (var notMatching : notMatchingRIs)
            notMatching.addProbability(this, 0);

        for (var matching : matchingRIs)
            matching.addProbability(this, probability);
        /*
         * for (var word : interestingWords) { var recommendations =
         * recommendationState.getRecommendedInstancesBySimilarName(word);
         * 
         * if (!recommendations.isEmpty()) {
         * logger.debug("Modifying RecommendedInstances according to Sketches & Diagrams: {}", recommendations); for
         * (var recommendation : recommendations) recommendation.addProbability(this, probability); } }
         */
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // NOP
    }
}
