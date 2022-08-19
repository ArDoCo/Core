/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents;

import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.PhraseMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

public class TermBuilder extends PipelineAgent {

    public static final double MIN_COSINE_SIMILARITY = 0.6;

    public TermBuilder(DataRepository dataRepository) {
        super(TermBuilder.class.getSimpleName(), dataRepository);

    }

    @Override
    public void run() {
        TextState textState = DataRepositoryHelper.getTextState(getDataRepository());
        var modelStates = DataRepositoryHelper.getModelStatesData(getDataRepository());
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(getDataRepository());

        for (var model : modelStates.modelIds()) {
            var modelState = modelStates.getModelState(model);

            increaseProbabilityForKnownTypes(modelState, textState);
            decreaseProbabilityForUnknownTypes(modelState, textState);
            combineMetaModelTypes(textState, modelState);
        }
        combineMappings(textState, MappingKind.TYPE, 0.6);
        combineMappings(textState, MappingKind.NAME, 0.1);

    }

    private void combineMappings(TextState textState, MappingKind kind, double threshold) {

        var nounMappings = textState.getNounMappingsOfKind(kind).select(nm -> nm.getProbabilityForKind(kind) > threshold);

        boolean restart = false;

        for (NounMapping nounMapping : nounMappings) {

            if (!textState.getNounMappings().contains(nounMapping)) {
                restart = true;
                break;
            }

            PhraseMapping phraseMapping = textState.getPhraseMappingByNounMapping(nounMapping);
            ImmutableList<NounMapping> nounMappingsOfTheSamePhraseMapping = textState.getNounMappingsByPhraseMapping(phraseMapping)
                    .select(nm -> nm.getProbabilityForKind(kind) > threshold);

            if (nounMappingsOfTheSamePhraseMapping.size() < 1)
                continue;

            MutableList<NounMapping> nounMappingsToMerge = Lists.mutable.empty();

            for (NounMapping nounMappingOfTheSamePhraseMapping : nounMappingsOfTheSamePhraseMapping) {

                if (!textState.getNounMappings().contains(nounMappingOfTheSamePhraseMapping)) {
                    restart = true;
                    nounMappingsToMerge = Lists.mutable.empty();
                    break;
                }

                int counter = 0;
                var words = nounMapping.getWords();
                for (Word word : words) {
                    for (Word wordOfNounMappingOfTheSamePhraseMapping : nounMappingOfTheSamePhraseMapping.getWords())
                        if (word.getNextWord().getText().equalsIgnoreCase(wordOfNounMappingOfTheSamePhraseMapping.getText())) {
                            counter++;
                            break;
                        }
                }
                if (counter > 0.5 * words.size()) {
                    nounMapping.addKindWithProbability(kind, this, 1.0);
                    NounMapping currentNounMapping = nounMappingOfTheSamePhraseMapping;
                    currentNounMapping.addKindWithProbability(kind, this, 1.0);

                    nounMappingsToMerge.add(currentNounMapping);

                }

            }
            if (nounMappingsToMerge.size() > 0) {
                textState.mergeNounMappings(nounMapping, nounMappingsToMerge, this);
            }

        }

        if (restart) {
            combineMappings(textState, kind, threshold);
        }

    }

    private void decreaseProbabilityForUnknownTypes(ModelExtractionState modelState, TextState textState) {
        ImmutableSet<String> modelTypes = modelState.getInstanceTypes();

        var nounMappingsOfTextState = textState.getNounMappings();
        for (NounMapping nounMapping : nounMappingsOfTextState) {
            for (String modelType : modelTypes) {
                var modelTypeParts = modelType.split(" ");
                for (String modelTypePart : modelTypeParts) {

                    if (!SimilarityUtils.areWordsSimilar(modelTypePart, nounMapping.getReference())) {

                        nounMapping.addKindWithProbability(MappingKind.TYPE, this, nounMapping.getProbabilityForKind(MappingKind.TYPE) * 0.5);
                        nounMapping.addKindWithProbability(MappingKind.NAME, this, nounMapping.getProbabilityForKind(MappingKind.NAME) * 1.5);

                    }

                }
            }

        }
    }

    private void increaseProbabilityForKnownTypes(ModelExtractionState modelState, TextState textState) {

        ImmutableSet<String> modelTypes = modelState.getInstanceTypes();

        for (String modelType : modelTypes) {
            var modelTypeParts = modelType.split(" ");

            for (String modelTypePart : modelTypeParts) {
                var nounMappingsWithModelTypePart = textState.getNounMappings().select(nm -> SimilarityUtils.areWordsSimilar(modelTypePart, nm.getReference()));

                for (NounMapping nounMappingThatIsModelTypePart : nounMappingsWithModelTypePart) {
                    nounMappingThatIsModelTypePart.addKindWithProbability(MappingKind.TYPE, this, 1.0);
                    nounMappingThatIsModelTypePart.addKindWithProbability(MappingKind.NAME, this, 0.0);
                }
            }
        }
    }

    private void combineMetaModelTypes(TextState textState, ModelExtractionState modelState) {

        ImmutableList<NounMapping> nounMappings = textState.getNounMappings();
        ImmutableSet<String> modelTypes = modelState.getInstanceTypes();

        for (String modelType : modelTypes) {
            var modelTypeParts = modelType.split(" ");

            if (modelTypeParts.length < 2) {
                continue;
            }

            MutableMap<String, MutableList<NounMapping>> modelTypePartMap = Maps.mutable.empty();
            MutableMap<String, MutableList<PhraseMapping>> containedPhraseMappings = Maps.mutable.empty();

            for (int i = 0; i < modelTypeParts.length; i++) {
                String modelTypePart = modelTypeParts[i];
                MutableList<NounMapping> nounMappingsWithExactWording = nounMappings.select(nm -> nm.getReference().equalsIgnoreCase(modelTypePart)).toList();
                modelTypePartMap.put(modelTypePart, nounMappingsWithExactWording);
                containedPhraseMappings.putIfAbsent(modelTypePart, Lists.mutable.empty());
                nounMappingsWithExactWording.forEach(nm -> containedPhraseMappings.get(modelTypePart).add(textState.getPhraseMappingByNounMapping(nm)));
            }

            if (modelTypePartMap.keySet().size() != modelTypeParts.length) {
                continue;
            }

            // TODO: Think : Do I want to iterate over the phrase mappings or the phrases?
            // If i iterate over the phrases, I could split & merge phrase mappings
            // If i iterate over the mappings, I acknowledge the substitution principle

            MutableList<PhraseMapping> usedPhraseMappings = containedPhraseMappings.flatCollect(pm -> pm).toSet().toList();
            PhraseMapping mostUsedPhraseMapping = null;
            int highestSimilarity = 0;

            for (int i = 0; i < usedPhraseMappings.size(); i++) {
                var usedPhraseMapping = usedPhraseMappings.get(i);
                var similarityCounter = 0;

                for (int j = 0; j < usedPhraseMappings.size(); j++) {

                    if (i != j && SimilarityUtils.getPhraseMappingSimilarity(textState, usedPhraseMapping, usedPhraseMappings.get(j),
                            SimilarityUtils.PhraseMappingAggregatorStrategy.MAX_SIMILARITY) > MIN_COSINE_SIMILARITY) {
                        similarityCounter++;
                    }
                }

                if (similarityCounter > highestSimilarity) {
                    highestSimilarity = similarityCounter;
                    mostUsedPhraseMapping = usedPhraseMapping;
                }
            }

            if (mostUsedPhraseMapping == null) {
                continue;
            }

            MutableList<NounMapping> nounMappingsToMerge = Lists.mutable.empty();
            MutableSet<PhraseMapping> phraseMappingsToMerge = Sets.mutable.empty();

            for (String modelTypePart : containedPhraseMappings.keySet()) {

                MutableList<PhraseMapping> phraseMappings = containedPhraseMappings.get(modelTypePart);
                final PhraseMapping finalMostUsedPhraseMapping = mostUsedPhraseMapping;
                phraseMappings.removeIf(pm -> SimilarityUtils.getPhraseMappingSimilarity(textState, pm, finalMostUsedPhraseMapping,
                        SimilarityUtils.PhraseMappingAggregatorStrategy.MAX_SIMILARITY) <= MIN_COSINE_SIMILARITY);

                if (phraseMappings.size() > 1) {
                    phraseMappings.removeIf(pm -> !pm.equals(finalMostUsedPhraseMapping));
                }
                if (phraseMappings.size() == 1) {
                    var nounMappingsOfModelTypePart = modelTypePartMap.get(modelTypePart);
                    var nounMappingsWithPhraseMapping = nounMappingsOfModelTypePart.select(nm -> phraseMappings.contains(textState
                            .getPhraseMappingByNounMapping(nm)));
                    assert (nounMappingsWithPhraseMapping.size() == 1) : "There should be exactly one noun mapping that is accessed by the phrase mapping";
                    nounMappingsToMerge.addAll(nounMappingsWithPhraseMapping);
                    phraseMappingsToMerge.add(phraseMappings.getOnly());
                }
            }

            if (nounMappingsToMerge.size() == modelTypeParts.length) {
                NounMapping typeNounMapping = nounMappings.get(0);

                typeNounMapping.addKindWithProbability(MappingKind.TYPE, this, 1.0);
                for (int i = 1; i < nounMappingsToMerge.size(); i++) {
                    NounMapping currentNounMapping = nounMappingsToMerge.get(i);
                    currentNounMapping.addKindWithProbability(MappingKind.TYPE, this, 1.0);
                    textState.mergeNounMappings(typeNounMapping, currentNounMapping, this);
                    var references = typeNounMapping.getReferenceWords().toList();
                    references.addAllIterable(currentNounMapping.getReferenceWords());
                    textState.setReferenceOfNounMapping(typeNounMapping, references.toImmutable(), null);
                }

                ImmutableList<PhraseMapping> phraseMappingListToMerge = usedPhraseMappings.toImmutableList();
                PhraseMapping typePhraseMapping = phraseMappingListToMerge.get(0);
                for (int i = 1; i < phraseMappingsToMerge.size(); i++) {
                    textState.mergePhraseMappings(typePhraseMapping, phraseMappingListToMerge.get(i));
                }

            }
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // No Delegates
    }

}