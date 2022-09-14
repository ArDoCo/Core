/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.informants;

import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

public class ReferenceInformant extends Informant {

    @Configurable
    private double probability = 0.75;

    public ReferenceInformant(DataRepository dataRepository) {
        super(ReferenceInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void run() {
        DataRepository dataRepository = getDataRepository();
        var textState = DataRepositoryHelper.getTextState(dataRepository);
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);
        for (var model : modelStates.modelIds()) {
            var modelState = modelStates.getModelState(model);
            var recommendationState = recommendationStates.getRecommendationState(modelState.getMetamodel());
            findRecommendedInstancesFromNounMappingsThatAreSimilarToInstances(modelState, recommendationState, textState);
        }
    }

    /**
     * Searches for instances mentioned in the text extraction state as names. If it founds some similar names it
     * creates recommendations.
     */
    private void findRecommendedInstancesFromNounMappingsThatAreSimilarToInstances(ModelExtractionState modelState, RecommendationState recommendationState,
            TextState textState) {
        for (ModelInstance instance : modelState.getInstances()) {
            var similarToInstanceMappings = getSimilarNounMappings(instance, textState);

            for (NounMapping similarNameMapping : similarToInstanceMappings) {
                recommendationState.addRecommendedInstance(similarNameMapping.getReference(), this, probability, similarToInstanceMappings);
            }
        }

    }

    private ImmutableList<NounMapping> getSimilarNounMappings(ModelInstance instance, TextState textState) {
        return textState.getNounMappingsOfKind(MappingKind.NAME)
                .select(nounMapping -> SimilarityUtils.isNounMappingSimilarToModelInstance(nounMapping, instance));
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> map) {
        // empty
    }
}
