package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.informants;

import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
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
        var text = DataRepositoryHelper.getAnnotatedText(dataRepository);
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var textState = DataRepositoryHelper.getTextState(dataRepository);

        /*MutableSet<ModelInstance> modelTypes = Sets.mutable.empty();
        for (var model : modelStates.modelIds()) {
            modelTypes.addAllIterable(modelStates.getModelState(model).getInstances());
        }*/

        ImmutableSet<String> typeIdentifier = getTypeIdentifier(modelStates);

        findAndRateNewNounMappingsViaTypeIdentifiers(typeIdentifier, text, textState);
        //rateTypesViaIdentifiers(typeIdentifier, textState);
    }

    private void findAndRateNewNounMappingsViaTypeIdentifiers(ImmutableSet<String> typeIdentifier, Text text, TextState textState) {
        MutableList<Word> typeWords = Lists.mutable.empty();

        for (var word : text.words()) {
            Word typeWord = findNewNounMappingsViaTypeIdentifiers(typeIdentifier, word, textState);
            if (typeWord != null) {
                typeWords.add(typeWord);
            }
        }
        int i = 0;
        typeWords.forEach(w -> textState.addNounMapping(w, MappingKind.TYPE, this, 1.0));
    }

    private Word findNewNounMappingsViaTypeIdentifiers(ImmutableSet<String> typeIdentifier, Word word, TextState textState) {

        var sameTypes = Lists.immutable.fromStream(typeIdentifier.stream().filter(typeId -> SimilarityUtils.areWordsSimilar(typeId, word.getText())));

        if (!sameTypes.isEmpty()) {
            return word;
        }

        return null;
    }

    private ImmutableSet<String> getTypeIdentifier(ModelStates modelStates) {
        MutableSet<String> modelTypes = Sets.mutable.empty();
        for (var model : modelStates.modelIds()) {
            modelTypes.addAllIterable(CommonUtilities.getTypeIdentifiers(modelStates.getModelState(model)));
        }
        return modelTypes.toImmutable();
    }

    private void rateTypesViaIdentifiers(ImmutableSet<String> typeIdentifier, TextState textState) {

        MutableSet<NounMapping> typeMappings = Sets.mutable.empty();
        MutableSet<NounMapping> smallAmountOfTypes = Sets.mutable.empty();
        MutableSet<NounMapping> noTypeMappings = Sets.mutable.empty();
        double threshold = 0.5;

        var nounMappings = textState.getNounMappings();
        for (NounMapping nounMapping : nounMappings) {
            double typeCounter = 0;
            var words = nounMapping.getWords();
            for (Word word : words) {
                var sameTypes = Lists.immutable.fromStream(typeIdentifier.stream().filter(typeId -> SimilarityUtils.areWordsSimilar(typeId, word.getText())));

                if (!sameTypes.isEmpty()) {
                    typeCounter++;
                }
            }
            if (typeCounter >= threshold * words.size()) {
                typeMappings.add(nounMapping);
            } else if (typeCounter == 0) {
                noTypeMappings.add(nounMapping);
            } else {
                smallAmountOfTypes.add(nounMapping);
            }
        }

        typeMappings.forEach(typeMapping -> typeMapping.addKindWithProbability(MappingKind.TYPE, this, 1.0));
        noTypeMappings = noTypeMappings.select(mapping -> mapping.getProbabilityForKind(MappingKind.TYPE) > 0);
        noTypeMappings.forEach(noTypeMapping -> noTypeMapping.addKindWithProbability(MappingKind.TYPE, this, 0.01));

    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> map) {
        //empty
    }
}
