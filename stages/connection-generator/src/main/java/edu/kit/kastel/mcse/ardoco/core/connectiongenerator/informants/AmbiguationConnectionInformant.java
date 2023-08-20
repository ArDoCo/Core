package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.informants;

import edu.kit.kastel.mcse.ardoco.core.api.connectiongenerator.ConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.common.util.AbbreviationDisambiguationHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimUtils;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

public class AmbiguationConnectionInformant extends Informant {
    public AmbiguationConnectionInformant(DataRepository dataRepository) {
        super(AmbiguationConnectionInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void process() {
        DataRepository dataRepository = getDataRepository();
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);
        var connectionStates = DataRepositoryHelper.getConnectionStates(dataRepository);
        for (var model : modelStates.extractionModelIds()) {
            var modelState = modelStates.getModelExtractionState(model);
            Metamodel metamodel = modelState.getMetamodel();
            var recommendationState = recommendationStates.getRecommendationState(metamodel);
            var connectionState = connectionStates.getConnectionState(metamodel);

            ambiguateRecommendedInstances(modelState, recommendationState, connectionState);
            ambiguateModelInstances(modelState, recommendationState, connectionState);
        }
    }

    private void ambiguateRecommendedInstances(ModelExtractionState modelState, RecommendationState recommendationState, ConnectionState connectionState) {
        for (var recommendedInstance : recommendationState.getRecommendedInstances()) {
            var riName = recommendedInstance.getName().toLowerCase();
            var set = AbbreviationDisambiguationHelper.getInstance().ambiguate(riName);
            if (set.isEmpty())
                continue;

            var sameInstances = modelState.getInstances()
                    .select(instance -> set.stream().anyMatch(ambiguatedName -> WordSimUtils.areWordsSimilar(ambiguatedName, instance.getFullName())));
            sameInstances.forEach(instance -> connectionState.addToLinks(recommendedInstance, instance, this, recommendedInstance.getProbability()));
        }
    }

    private void ambiguateModelInstances(ModelExtractionState modelState, RecommendationState recommendationState, ConnectionState connectionState) {
        for (var instance : modelState.getInstances()) {
            var set = AbbreviationDisambiguationHelper.getInstance().ambiguate(instance.getFullName().toLowerCase());
            if (set.isEmpty())
                continue;

            var sameInstances = recommendationState.getRecommendedInstances()
                    .select(ri -> set.stream().anyMatch(ambiguatedName -> WordSimUtils.areWordsSimilar(ambiguatedName, ri.getName())));
            sameInstances.forEach(recommendedInstance -> connectionState.addToLinks(recommendedInstance, instance, this, recommendedInstance.getProbability()));
        }
    }
}
