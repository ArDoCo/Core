package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.MutableSet;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.InconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstanceLink;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.datastructures.MissingElementInconsistencyCandidate;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.datastructures.MissingElementSupport;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.datastructures.MissingModelInstanceInconsistency;

@MetaInfServices(InconsistencyAgent.class)
public class MissingModelElementInconsistencyAgent extends InconsistencyAgent {
    // TODO add some structure like "support" and filter inconsistency with low support (<2)
    // support is gained if one test identifies this as inconsistency. If another test confirms it, support is increased

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

        // add support for those who have a probability higher than the set threshold
        for (var candidate : candidateElements) {
            if (candidate.getProbability() >= threshold) {
                candidates.add(new MissingElementInconsistencyCandidate(candidate, MissingElementSupport.ELEMENT_WITH_NO_TRACE_LINK));
            }
        }

        // find out those elements that are in the same sentence as a traced element
        // TODO check!
        for (var relation : recommendationState.getInstanceRelations()) {
            var fromInstance = relation.getFromInstance();
            var toInstance = relation.getToInstance();
            if (linkedRecommendedInstances.contains(fromInstance) && candidateElements.contains(toInstance)) {
                addToCandidates(toInstance);
            } else if (linkedRecommendedInstances.contains(toInstance) && candidateElements.contains(fromInstance)) {
                addToCandidates(fromInstance);
            }
        }

        // TODO methods for other kinds of support

        createInconsistencies();
    }

    private void addToCandidates(IRecommendedInstance recommendedInstance) {
        var existingCandidate = candidates.detectOptional(c -> c.getRecommendedInstance().equals(recommendedInstance));
        if (existingCandidate.isPresent()) {
            logger.info("Found dependency to traced element for candidate {}", recommendedInstance.getName());
            existingCandidate.get().addSupport(MissingElementSupport.DEPENDENCY_TO_TRACED_ELEMENT);
        } else {
            candidates.add(new MissingElementInconsistencyCandidate(recommendedInstance, MissingElementSupport.DEPENDENCY_TO_TRACED_ELEMENT));
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
