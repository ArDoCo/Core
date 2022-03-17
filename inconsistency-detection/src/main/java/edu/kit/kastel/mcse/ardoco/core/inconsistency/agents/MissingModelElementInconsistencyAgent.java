/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.IInstanceLink;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.MissingElementInconsistencyCandidate;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.MissingElementSupport;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.types.MissingModelInstanceInconsistency;
import edu.kit.kastel.mcse.ardoco.core.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
import edu.kit.kastel.mcse.ardoco.core.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;

@MetaInfServices(InconsistencyAgent.class)
public class MissingModelElementInconsistencyAgent extends InconsistencyAgent {

    private double minSupport = 1;

    public MissingModelElementInconsistencyAgent() {
        super(GenericInconsistencyConfig.class);
    }

    private MissingModelElementInconsistencyAgent(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, IInconsistencyState inconsistencyState, GenericInconsistencyConfig inconsistencyConfig) {
        super(GenericInconsistencyConfig.class, text, textState, modelState, recommendationState, connectionState, inconsistencyState);
        var threshold = inconsistencyConfig.getMissingModelInstanceInconsistencyThreshold();
    }

    @Override
    public InconsistencyAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, IInconsistencyState inconsistencyState, Configuration config) {
        return new MissingModelElementInconsistencyAgent(text, textState, modelState, recommendationState, connectionState, inconsistencyState,
                (GenericInconsistencyConfig) config);
    }

    @Override
    public void exec() {
        var candidates = Sets.mutable.<MissingElementInconsistencyCandidate> empty();

        var candidateElements = Lists.mutable.ofAll(inconsistencyState.getRecommendedInstances());
        var linkedRecommendedInstances = connectionState.getInstanceLinks().collect(IInstanceLink::getTextualInstance);

        // find recommendedInstances with no trace link (also not sharing words with linked RIs)
        candidateElements.removeAllIterable(linkedRecommendedInstances);
        candidateElements = filterCandidatesCoveredByRecommendedInstance(candidateElements, linkedRecommendedInstances);

        for (var candidate : candidateElements) {
            addToCandidates(candidates, candidate, MissingElementSupport.ELEMENT_WITH_NO_TRACE_LINK);
        }

        // find out those elements that are in the same sentence as a traced element
        // need checking!
        for (var relation : recommendationState.getInstanceRelations()) {
            var fromInstance = relation.getFromInstance();
            var toInstance = relation.getToInstance();
            if (linkedRecommendedInstances.contains(fromInstance) && candidateElements.contains(toInstance)) {
                addToCandidates(candidates, toInstance, MissingElementSupport.DEPENDENCY_TO_TRACED_ELEMENT);
            } else if (linkedRecommendedInstances.contains(toInstance) && candidateElements.contains(fromInstance)) {
                addToCandidates(candidates, fromInstance, MissingElementSupport.DEPENDENCY_TO_TRACED_ELEMENT);
            }
        }

        // methods for other kinds of support
        // NONE

        // finally create inconsistencies
        createInconsistencies(candidates);
    }

    /**
     * Filter those that are covered by other RecommendedInstances. covered means that they share at least one word
     *
     * @param candidateElements          candidate RecommendedInstances
     * @param linkedRecommendedInstances already linked RecommendedInstances
     * @return list of candidate RecommendedInstances that are not already covered by other RecommendedInstances
     */
    private MutableList<IRecommendedInstance> filterCandidatesCoveredByRecommendedInstance(MutableList<IRecommendedInstance> candidateElements,
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
        return candidateElements;
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

    private void createInconsistencies(MutableSet<MissingElementInconsistencyCandidate> candidates) {
        for (var candidate : candidates) {
            var support = candidate.getAmountOfSupport();
            if (support >= minSupport) {
                inconsistencyState.addInconsistency(new MissingModelInstanceInconsistency(candidate.getRecommendedInstance()));
            }
        }
    }
}
