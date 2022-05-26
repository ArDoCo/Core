/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.InconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.InconsistencyAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.IInstanceLink;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.MissingElementInconsistencyCandidate;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.MissingElementSupport;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.types.MissingModelInstanceInconsistency;

public class MissingModelElementInconsistencyAgent extends InconsistencyAgent {

    @Configurable
    private double minSupport = 1;

    public MissingModelElementInconsistencyAgent() {
        // empty
    }

    @Override
    public void execute(InconsistencyAgentData data) {
        for (var model : data.getModelIds()) {
            findMissingModelElementInconsistencies(data, model);
        }
    }

    private void findMissingModelElementInconsistencies(InconsistencyAgentData data, String model) {
        var inconsistencyState = data.getInconsistencyState(model);
            var connectionState = data.getConnectionState(model);

            var candidates = Sets.mutable.<MissingElementInconsistencyCandidate> empty();

            var candidateElements = Lists.mutable.ofAll(inconsistencyState.getRecommendedInstances());
            var linkedRecommendedInstances = connectionState.getInstanceLinks().collect(IInstanceLink::getTextualInstance);

            // find recommendedInstances with no trace link (also not sharing words with linked RIs)
            candidateElements.removeAllIterable(linkedRecommendedInstances);
            filterCandidatesCoveredByRecommendedInstance(candidateElements, linkedRecommendedInstances);

            for (var candidate : candidateElements) {
                addToCandidates(candidates, candidate, MissingElementSupport.ELEMENT_WITH_NO_TRACE_LINK);
            }

            // methods for other kinds of support
            // NONE

            // finally create inconsistencies
            createInconsistencies(candidates, inconsistencyState);
    }

    /**
     * Filter those that are covered by other RecommendedInstances. covered means that they share at least one word
     *
     * @param candidateElements          candidate RecommendedInstances
     * @param linkedRecommendedInstances already linked RecommendedInstances
     */
    private void filterCandidatesCoveredByRecommendedInstance(MutableList<IRecommendedInstance> candidateElements,
            ImmutableList<IRecommendedInstance> linkedRecommendedInstances) {
        for (var linkedRecommendedInstance : linkedRecommendedInstances) {
            var linkedWords = linkedRecommendedInstance.getNameMappings().flatCollect(INounMapping::getWords);
            var candidatesToRemove = Lists.mutable.<IRecommendedInstance> empty();
            for (var candidate : candidateElements) {
                if (CommonUtilities.wordListContainsAnyWordFromRecommendedInstance(linkedWords, candidate)) {
                    candidatesToRemove.add(candidate);
                }
            }
            candidateElements.removeAll(candidatesToRemove);
        }
    }

    private void addToCandidates(MutableSet<MissingElementInconsistencyCandidate> candidates, IRecommendedInstance recommendedInstance,
            MissingElementSupport support) {
        for (var candidate : candidates) {
            var candidateRecommendedInstance = candidate.getRecommendedInstance();
            if (candidateRecommendedInstance.equals(recommendedInstance)) {
                candidate.addSupport(MissingElementSupport.MULTIPLE_OVERLAPPING_RECOMMENDED_INSTANCES);
                return;
            }
            var candidateWords = candidateRecommendedInstance.getNameMappings().flatCollect(INounMapping::getWords);
            if (CommonUtilities.wordListContainsAnyWordFromRecommendedInstance(candidateWords, recommendedInstance)) {
                candidate.addSupport(MissingElementSupport.MULTIPLE_OVERLAPPING_RECOMMENDED_INSTANCES);
                // TODO what to do here?
                // A) return here, but for sure miss some correct sentences
                // B) do not return and do nothing else, this causes a lot of candidates
                // C) merge candidates? or merge the underlying recommendedInstances?
            }
        }

        var candidate = new MissingElementInconsistencyCandidate(recommendedInstance, support);
        candidates.add(candidate);
    }

    private void createInconsistencies(MutableSet<MissingElementInconsistencyCandidate> candidates, IInconsistencyState inconsistencyState) {
        for (var candidate : candidates) {
            var support = candidate.getAmountOfSupport();
            if (support >= minSupport) {
                IRecommendedInstance recommendedInstance = candidate.getRecommendedInstance();
                double confidence = recommendedInstance.getProbability();
                for (var word : recommendedInstance.getNameMappings().flatCollect(INounMapping::getWords).distinct()) {
                    var sentenceNo = word.getSentenceNo() + 1;
                    var wordText = word.getText();
                    inconsistencyState.addInconsistency(new MissingModelInstanceInconsistency(wordText, sentenceNo, confidence));
                }
            }
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // empty
    }
}
