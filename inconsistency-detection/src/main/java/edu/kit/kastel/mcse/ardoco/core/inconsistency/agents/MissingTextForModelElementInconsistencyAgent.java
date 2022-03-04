package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.IInstanceLink;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.types.MissingTextForModelElementInconsistency;
import edu.kit.kastel.mcse.ardoco.core.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;

@MetaInfServices(InconsistencyAgent.class)
public class MissingTextForModelElementInconsistencyAgent extends InconsistencyAgent {
    private List<String> whitelist = Lists.mutable.empty();
    private List<String> types = Lists.mutable.empty();

    public MissingTextForModelElementInconsistencyAgent() {
        super(GenericInconsistencyConfig.class);
    }

    private MissingTextForModelElementInconsistencyAgent(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, IInconsistencyState inconsistencyState, GenericInconsistencyConfig inconsistencyConfig) {
        super(GenericInconsistencyConfig.class, text, textState, modelState, recommendationState, connectionState, inconsistencyState);
        // load settings from inconsistencyConfig
        types.add("BasicComponent"); // TODO load from config or similar
    }

    @Override
    public MissingTextForModelElementInconsistencyAgent create(IText text, ITextState textState, IModelState modelState,
            IRecommendationState recommendationState, IConnectionState connectionState, IInconsistencyState inconsistencyState, Configuration config) {
        return new MissingTextForModelElementInconsistencyAgent(text, textState, modelState, recommendationState, connectionState, inconsistencyState,
                (GenericInconsistencyConfig) config);
    }

    @Override
    public void exec() {
        var linkedModelInstances = connectionState.getInstanceLinks().collect(IInstanceLink::getModelInstance).distinct();

        // find model instances of given types that are not linked and, thus, are candidates
        var candidateModelInstances = Lists.mutable.<IModelInstance> empty();
        for (var modelInstance : modelState.getInstances()) {
            if (modelInstanceHasTargetedType(modelInstance) && !linkedModelInstances.contains(modelInstance)) {
                candidateModelInstances.add(modelInstance);
            }
        }

        // further filtering
        candidateModelInstances = filterWithWhitelist(candidateModelInstances);

        // create Inconsistencies
        createInconsistencies(candidateModelInstances);
    }

    private boolean modelInstanceHasTargetedType(IModelInstance modelInstance) {
        if (types.contains(modelInstance.getFullType())) {
            return true;
        }
        for (var instanceType : modelInstance.getTypeParts()) {
            if (types.contains(instanceType)) {
                return true;
            }
        }
        return false;
    }

    private MutableList<IModelInstance> filterWithWhitelist(MutableList<IModelInstance> candidateModelInstances) {
        var filteredCandidates = Lists.mutable.ofAll(candidateModelInstances);
        for (var whitelisting : whitelist) {
            var pattern = Pattern.compile(whitelisting);
            var whitelistedCandidates = filteredCandidates.select(c -> {
                if (pattern.matcher(c.getFullName()).matches()) {
                    return true;
                }
                for (var name : c.getNameParts()) {
                    if (pattern.matcher(name).matches()) {
                        return true;
                    }
                }
                return false;
            });
            filteredCandidates.removeAll(whitelistedCandidates);
        }
        return filteredCandidates;
    }

    private void createInconsistencies(MutableList<IModelInstance> candidateModelInstances) {
        for (var candidate : candidateModelInstances) {
            var inconsistency = new MissingTextForModelElementInconsistency(candidate);
            inconsistencyState.addInconsistency(inconsistency);
        }
    }

}
