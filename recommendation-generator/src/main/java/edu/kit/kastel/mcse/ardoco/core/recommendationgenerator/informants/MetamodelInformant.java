package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.informants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.RichIterable;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

public class MetamodelInformant extends Informant {

    @Configurable
    private double confidence = 1.0;

    public MetamodelInformant(DataRepository dataRepository) {
        super(CompoundRecommendationInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void run() {
        DataRepository dataRepository = getDataRepository();
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var textState = DataRepositoryHelper.getTextState(dataRepository);

        MutableSet<ModelInstance> modelTypes = Sets.mutable.empty();
        for (var model : modelStates.modelIds()) {
            modelTypes.addAllIterable(modelStates.getModelState(model).getInstances());
        }

        rateTypesInTextState(modelTypes.toImmutable(), textState);
    }

    private void rateTypesInTextState(ImmutableSet<ModelInstance> instanceSet, TextState textState) {

        var types = Lists.immutable.withAll(instanceSet.groupBy(ModelInstance::getFullType).toMap().values().stream().map(RichIterable::getAny).toList());

        List<NounMapping> typeMappings = new ArrayList<>();
        List<NounMapping> noTypeMappings = new ArrayList<>();

        for (NounMapping nounMapping : textState.getNounMappings().select(nm -> nm.getProbabilityForKind(MappingKind.TYPE) > 0)) {
            if (types.anySatisfy(instance -> SimilarityUtils.isNounMappingSimilarToTypeOfModelInstance(nounMapping, instance))) {
                nounMapping.addKindWithProbability(MappingKind.TYPE, this, 1.0);
                typeMappings.add(nounMapping);

            } else {
                nounMapping.addKindWithProbability(MappingKind.TYPE, this, 0.001);
                noTypeMappings.add(nounMapping);
            }

            /*if () {
                nounMapping.addKindWithProbability(MappingKind.TYPE, this, 1.0);
            } else {
             
            if (types.noneSatisfy(instance -> SimilarityUtils.isNounMappingSimilarToTypeOfModelInstance(nounMapping, instance))) {
                nounMapping.addKindWithProbability(MappingKind.TYPE, this, 0.001);
            }*/
        }

    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> map) {
        //empty
    }
}
