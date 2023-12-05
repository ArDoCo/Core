/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.informants;

import java.util.Locale;

import edu.kit.kastel.mcse.ardoco.core.api.connectiongenerator.ConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.models.LegacyModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.common.util.AbbreviationDisambiguationHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

/**
 * Creates {@link edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.InstanceLink InstanceLinks} by partially ambiguating the recommended instances and model
 * elements.
 */
//TODO Check if this can be removed, may no longer be required due to how ambiguations are treated by the similarity metrics
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
        for (var model : modelStates.modelIds()) {
            var modelState = modelStates.getModelExtractionState(model);
            Metamodel metamodel = modelState.getMetamodel();
            var recommendationState = recommendationStates.getRecommendationState(metamodel);
            var connectionState = connectionStates.getConnectionState(metamodel);

            ambiguateRecommendedInstances(modelState, recommendationState, connectionState);
            ambiguateModelInstances(modelState, recommendationState, connectionState);
        }
    }

    /**
     * Creates trace links by partially ambiguating the names of recommended instances and searching for similar model instance names. E.g., a recommended
     * instance may be named "DatabaseUserAcceptanceTesting" and a model instance may be name "DatabaseUAT". The function calculates "DatabaseUAT",
     * "DBUserAcceptanceTesting" and "DBUAT". Thus the function finds a trace based on the similarity of "DatabaseUAT" (RI) and "DatabaseUAT" (ModelInstance).
     *
     * @param modelState          the model state
     * @param recommendationState the recommendation state
     * @param connectionState     the connection state
     */
    private void ambiguateRecommendedInstances(LegacyModelExtractionState modelState, RecommendationState recommendationState,
            ConnectionState connectionState) {
        for (var recommendedInstance : recommendationState.getRecommendedInstances()) {
            var riName = recommendedInstance.getName().toLowerCase(Locale.ENGLISH);
            var set = AbbreviationDisambiguationHelper.ambiguate(riName);
            if (set.isEmpty())
                continue;

            var sameInstances = modelState.getInstances()
                    .select(instance -> set.stream()
                            .anyMatch(ambiguatedName -> getMetaData().getWordSimUtils().areWordsSimilar(ambiguatedName, instance.getFullName())));
            sameInstances.forEach(instance -> connectionState.addToLinks(recommendedInstance, instance, this, recommendedInstance.getProbability()));
        }
    }

    /**
     * Creates trace links by partially ambiguating the names of model instances and searching for similar recommended instance names.
     *
     * @param modelState          the model state
     * @param recommendationState the recommendation state
     * @param connectionState     the connection state
     * @see #ambiguateRecommendedInstances(LegacyModelExtractionState, RecommendationState, ConnectionState)
     */
    private void ambiguateModelInstances(LegacyModelExtractionState modelState, RecommendationState recommendationState, ConnectionState connectionState) {
        for (var instance : modelState.getInstances()) {
            var set = AbbreviationDisambiguationHelper.ambiguate(instance.getFullName().toLowerCase(Locale.ENGLISH));
            if (set.isEmpty())
                continue;

            var sameInstances = recommendationState.getRecommendedInstances()
                    .select(ri -> set.stream().anyMatch(ambiguatedName -> getMetaData().getWordSimUtils().areWordsSimilar(ambiguatedName, ri.getName())));
            sameInstances.forEach(recommendedInstance -> connectionState.addToLinks(recommendedInstance, instance, this, recommendedInstance.getProbability()));
        }
    }
}
