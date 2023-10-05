package edu.kit.kastel.mcse.ardoco.core.models.informants;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.Disambiguation;
import edu.kit.kastel.mcse.ardoco.core.common.util.AbbreviationDisambiguationHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

public class ModelDisambiguationInformant extends Informant {
    public ModelDisambiguationInformant(DataRepository dataRepository) {
        super(ModelDisambiguationInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void process() {
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        for (var modelId : modelStates.extractionModelIds()) {
            var modelExtractionState = modelStates.getModelExtractionState(modelId);
            var instances = modelExtractionState.getInstances();
            for (var instance : instances) {
                var names = instance.getNameParts();
                for (var name : names) {
                    var abbreviations = AbbreviationDisambiguationHelper.getAbbreviationCandidates(name);
                    var meaningsMap = abbreviations.stream().collect(Collectors.toMap(a -> a, AbbreviationDisambiguationHelper::disambiguate));
                    for (Map.Entry<String, Set<String>> e : meaningsMap.entrySet()) {
                        AbbreviationDisambiguationHelper.addTransient(new Disambiguation(e.getKey(), e.getValue().toArray(new String[0])));
                    }
                }
            }
        }
    }
}
