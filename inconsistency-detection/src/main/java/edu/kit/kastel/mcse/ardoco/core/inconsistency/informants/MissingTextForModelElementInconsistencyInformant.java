/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.informants;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.InstanceLink;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.InconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.types.MissingTextForModelElementInconsistency;

public class MissingTextForModelElementInconsistencyInformant extends Informant {

    @Configurable
    private List<String> whitelist = Lists.mutable.of("DummyRecommender", "Cache");
    @Configurable
    private List<String> types = Lists.mutable.of("BasicComponent", "CompositeComponent");

    public MissingTextForModelElementInconsistencyInformant(DataRepository dataRepository) {
        super(MissingTextForModelElementInconsistencyInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void run() {
        var dataRepository = getDataRepository();
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var connectionStates = DataRepositoryHelper.getConnectionStates(dataRepository);
        var inconsistencyStates = DataRepositoryHelper.getInconsistencyStates(dataRepository);

        for (var model : modelStates.modelIds()) {
            var modelState = modelStates.getModelState(model);
            var metaModel = modelState.getMetamodel();
            var connectionState = connectionStates.getConnectionState(metaModel);
            var inconsistencyState = inconsistencyStates.getInconsistencyState(metaModel);

            var linkedModelInstances = connectionState.getInstanceLinks().collect(InstanceLink::getModelInstance).distinct();

            // find model instances of given types that are not linked and, thus, are candidates
            var candidateModelInstances = Lists.mutable.<ModelInstance>empty();
            for (var modelInstance : modelState.getInstances()) {
                if (modelInstanceHasTargetedType(modelInstance, types) && !linkedModelInstances.contains(modelInstance)) {
                    candidateModelInstances.add(modelInstance);
                }
            }

            // further filtering
            candidateModelInstances = filterWithWhitelist(candidateModelInstances, whitelist);

            // create Inconsistencies
            createInconsistencies(candidateModelInstances, inconsistencyState);
        }
    }

    public static boolean modelInstanceHasTargetedType(ModelInstance modelInstance, List<String> types) {
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

    public static MutableList<ModelInstance> filterWithWhitelist(MutableList<ModelInstance> candidateModelInstances, List<String> whitelist) {
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

    private void createInconsistencies(MutableList<ModelInstance> candidateModelInstances, InconsistencyState inconsistencyState) {
        for (var candidate : candidateModelInstances) {
            var inconsistency = new MissingTextForModelElementInconsistency(candidate);
            inconsistencyState.addInconsistency(inconsistency);
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> map) {
        // empty
    }
}
