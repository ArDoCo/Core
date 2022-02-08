/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.IInstanceLink;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.MissingElementInconsistencyCandidate;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.MissingElementSupport;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.MissingModelInstanceInconsistency;
import edu.kit.kastel.mcse.ardoco.core.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
import edu.kit.kastel.mcse.ardoco.core.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;

@MetaInfServices(InconsistencyAgent.class)
public class MissingModelElementInconsistencyAgent extends InconsistencyAgent {

    private MutableSet<MissingElementInconsistencyCandidate> candidates = Sets.mutable.empty();
    private double minSupport = 1;
    private double threshold = 0.75d;

    public MissingModelElementInconsistencyAgent() {
        super(GenericInconsistencyConfig.class);
    }

    private MissingModelElementInconsistencyAgent(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, IInconsistencyState inconsistencyState, GenericInconsistencyConfig inconsistencyConfig) {
        super(GenericInconsistencyConfig.class, text, textState, modelState, recommendationState, connectionState, inconsistencyState);
        threshold = inconsistencyConfig.getMissingModelInstanceInconsistencyThreshold();
    }

    @Override
    public InconsistencyAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, IInconsistencyState inconsistencyState, Configuration config) {
        return new MissingModelElementInconsistencyAgent(text, textState, modelState, recommendationState, connectionState, inconsistencyState,
                (GenericInconsistencyConfig) config);
    }

    @Override
    public void exec() {
        var candidateElements = Lists.mutable.ofAll(recommendationState.getRecommendedInstances());
        var linkedRecommendedInstances = connectionState.getInstanceLinks().collect(IInstanceLink::getTextualInstance);

        // find recommendedInstances with no trace link
        candidateElements.removeAllIterable(linkedRecommendedInstances);

        // remove those candidates for which the words are covered by other RecommendedInstances that have a trace link
        // TODO check!
        // candidateElements = removeRecommendedInstancesWithWordsThatAreAlreadyTraced(candidateElements,
        // linkedRecommendedInstances);

        // add support for those who have a probability higher than the set threshold
        for (var candidate : candidateElements) {
            if (candidate.getProbability() >= threshold) {
                addToCandidates(candidate, MissingElementSupport.ELEMENT_WITH_NO_TRACE_LINK);
            }
        }

        // find out those elements that are in the same sentence as a traced element
        // need checking!
        for (var relation : recommendationState.getInstanceRelations()) {
            var fromInstance = relation.getFromInstance();
            var toInstance = relation.getToInstance();
            if (linkedRecommendedInstances.contains(fromInstance) && candidateElements.contains(toInstance)) {
                addToCandidates(toInstance, MissingElementSupport.DEPENDENCY_TO_TRACED_ELEMENT);
            } else if (linkedRecommendedInstances.contains(toInstance) && candidateElements.contains(fromInstance)) {
                addToCandidates(fromInstance, MissingElementSupport.DEPENDENCY_TO_TRACED_ELEMENT);
            }
        }

        // methods for other kinds of support

        createInconsistencies();
    }

    private MutableList<IRecommendedInstance> removeRecommendedInstancesWithWordsThatAreAlreadyTraced(MutableList<IRecommendedInstance> candidateElements,
            ImmutableList<IRecommendedInstance> linkedRecommendedInstances) {
        var tracedWords = linkedRecommendedInstances.flatCollect(IRecommendedInstance::getNameMappings).flatCollect(INounMapping::getWords);
        return candidateElements.reject(candidate -> candidate.getNameMappings().flatCollect(INounMapping::getWords).anySatisfy(tracedWords::contains));
    }

    private void addToCandidates(IRecommendedInstance recommendedInstance, MissingElementSupport support) {
        var existingCandidate = candidates.detectOptional(c -> c.getRecommendedInstance().equals(recommendedInstance));
        if (existingCandidate.isPresent()) {
            existingCandidate.get().addSupport(support);
        } else {
            var candidate = new MissingElementInconsistencyCandidate(recommendedInstance, support);
            candidates.add(candidate);
        }
    }

    private void createInconsistencies() {
        for (var candidate : candidates) {
            var support = candidate.getAmountOfSupport();
            if (support >= minSupport) {
                inconsistencyState.addInconsistency(new MissingModelInstanceInconsistency(candidate.getRecommendedInstance()));
            }
        }
    }
}
