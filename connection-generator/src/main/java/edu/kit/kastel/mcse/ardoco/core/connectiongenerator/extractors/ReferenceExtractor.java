/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.extractors;

import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

public class ReferenceExtractor extends Informant {

    @Configurable
    private double probability = 0.75;

    public ReferenceExtractor(DataRepository dataRepository) {
        super("ReferenceExtractor", dataRepository);
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
    private void findRecommendedInstancesFromNounMappingsThatAreSimilarToInstances(IModelState modelState, IRecommendationState recommendationState,
            ITextState textState) {
        for (IModelInstance instance : modelState.getInstances()) {
            var similarToInstanceMappings = getSimilarNounMappings(instance, textState);

            for (INounMapping similarNameMapping : similarToInstanceMappings) {
                recommendationState.addRecommendedInstance(similarNameMapping.getReference(), this, probability, similarToInstanceMappings);
            }
        }

    }

    private ImmutableList<INounMapping> getSimilarNounMappings(IModelInstance instance, ITextState textState) {
        return textState.getNounMappingsOfKind(MappingKind.NAME)
                .select(nounMapping -> SimilarityUtils.isNounMappingSimilarToModelInstance(nounMapping, instance));
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> map) {
        // empty
    }
}
