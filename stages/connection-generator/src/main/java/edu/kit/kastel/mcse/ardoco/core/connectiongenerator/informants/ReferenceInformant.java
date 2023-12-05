/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.informants;

import java.util.SortedMap;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.models.LegacyModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

public class ReferenceInformant extends Informant {

    @Configurable
    private double probability = 0.75;

    public ReferenceInformant(DataRepository dataRepository) {
        super(ReferenceInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void process() {
        DataRepository dataRepository = getDataRepository();
        var textState = DataRepositoryHelper.getTextState(dataRepository);
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);
        for (var model : modelStates.modelIds()) {
            var modelState = modelStates.getModelExtractionState(model);
            var recommendationState = recommendationStates.getRecommendationState(modelState.getMetamodel());
            findRecommendedInstancesFromNounMappingsThatAreSimilarToInstances(modelState, recommendationState, textState);
        }
    }

    /**
     * Searches for instances mentioned in the text extraction state as names. If it founds some similar names it
     * creates recommendations.
     */
    private void findRecommendedInstancesFromNounMappingsThatAreSimilarToInstances(LegacyModelExtractionState modelState,
            RecommendationState recommendationState, TextState textState) {
        for (ModelInstance instance : modelState.getInstances()) {
            var similarToInstanceMappings = getSimilarNounMappings(instance, textState);

            for (NounMapping similarNameMapping : similarToInstanceMappings) {
                recommendationState.addRecommendedInstance(similarNameMapping.getReference(), this, probability, similarToInstanceMappings);
            }
        }

    }

    private ImmutableList<NounMapping> getSimilarNounMappings(ModelInstance instance, TextState textState) {
        return textState.getNounMappingsOfKind(MappingKind.NAME)
                .select(nounMapping -> getMetaData().getSimilarityUtils().isNounMappingSimilarToModelInstance(nounMapping, instance));
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> map) {
        // empty
    }
}
