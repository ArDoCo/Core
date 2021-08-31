package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.InconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInconsistencyState;
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
        var recommendedInstances = Lists.mutable.ofAll(recommendationState.getRecommendedInstances());

        List<IRecommendedInstance> candidatesLikelyElements = findLikelyTextElementsWithNoTraceLinks(recommendedInstances);
        for (var candidate : candidatesLikelyElements) {
            candidates.add(new MissingElementInconsistencyCandidate(candidate, MissingElementSupport.ELEMENT_WITH_NO_TRACE_LINK));
        }

        // TODO methods for other kinds of support

        createInconsistencies();
    }

    private void createInconsistencies() {
        for (var candidate : candidates) {
            var support = candidate.getAmountOfSupport();
            if (support >= minSupport) {
                inconsistencyState.addInconsistency(new MissingModelInstanceInconsistency(candidate.getRecommendedInstance()));
            }
        }
    }

    private MutableList<IRecommendedInstance> findLikelyTextElementsWithNoTraceLinks(List<IRecommendedInstance> recommendedInstances) {
        MutableList<IRecommendedInstance> potentialCandidates = Lists.mutable.ofAll(recommendedInstances);

        // remove all recommended instances that were used in an instanceLink (trace link)
        for (var tracelink : connectionState.getInstanceLinks()) {
            var textualInstance = tracelink.getTextualInstance();
            potentialCandidates.remove(textualInstance);
        }

        return potentialCandidates.select(c -> c.getProbability() >= threshold);
    }
}
