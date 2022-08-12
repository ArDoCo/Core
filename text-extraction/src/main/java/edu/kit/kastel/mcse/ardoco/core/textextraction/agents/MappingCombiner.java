/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;

import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.api.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.PhraseMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

public class MappingCombiner extends PipelineAgent {

    public static final double MIN_COSINE_SIMILARITY = 0.4;

    public MappingCombiner(DataRepository dataRepository) {
        super(MappingCombiner.class.getSimpleName(), dataRepository);
    }

    @Override
    public void run() {
        TextState textState = DataRepositoryHelper.getTextState(getDataRepository());
        combineSimilarPhraseMappings(textState);
    }

    private void combineSimilarPhraseMappings(TextState textState) {

        ImmutableList<PhraseMapping> phraseMappings = textState.getPhraseMappings();

        for (PhraseMapping phraseMapping : phraseMappings) {
            ImmutableList<PhraseMapping> similarPhraseMappings = phraseMappings.select(p -> SimilarityUtils.getPhraseMappingSimilarity(textState, phraseMapping,
                    p, SimilarityUtils.PhraseMappingAggregatorStrategy.MAX_SIMILARITY) > MIN_COSINE_SIMILARITY);
            ImmutableList<NounMapping> nounMappingsOfPhraseMapping = textState.getNounMappingsByPhraseMapping(phraseMapping);

            for (PhraseMapping similarPhraseMapping : similarPhraseMappings) {

                ImmutableList<NounMapping> nounMappingsOfSimilarPhraseMapping = textState.getNounMappingsByPhraseMapping(similarPhraseMapping);

                if (similarPhraseMapping == phraseMapping) {
                    for (NounMapping nounMappingOfSimilarPhraseMapping : nounMappingsOfSimilarPhraseMapping) {
                        for (NounMapping nounMapping : textState.getNounMappingsByPhraseMapping(phraseMapping)) {
                            if (nounMapping.isTheSameAs(nounMappingOfSimilarPhraseMapping) || !textState.getNounMappings()
                                    .contains(nounMappingOfSimilarPhraseMapping) || !textState.getNounMappings().contains(nounMapping)) {
                                continue;
                            }
                            if (SimilarityUtils.areNounMappingsSimilar(nounMapping, nounMappingOfSimilarPhraseMapping)) {
                                textState.mergeNounMappings(nounMapping, nounMappingOfSimilarPhraseMapping, this);
                            }
                        }
                    }
                    continue;
                }

                if (nounMappingsOfPhraseMapping.size() != nounMappingsOfSimilarPhraseMapping.size()) {
                    continue;
                }

                MutableList<Pair<NounMapping, NounMapping>> similarNounMappings = Lists.mutable.empty();

                for (NounMapping nounMapping : nounMappingsOfPhraseMapping) {
                    NounMapping similarNounMapping = getMostSimilarNounMappingOverThreshold(nounMapping, nounMappingsOfSimilarPhraseMapping);
                    if (similarNounMapping != null) {
                        similarNounMappings.add(new Pair<>(nounMapping, similarNounMapping));
                    }
                }

                if (similarNounMappings.size() != nounMappingsOfPhraseMapping.size()) {
                    continue;
                }

                if (similarPhraseMappings.size() != similarNounMappings.collect(Pair::second).distinct().size() * 2) {
                    continue;
                }

                textState.mergePhraseMappingsAndNounMappings(phraseMapping, similarPhraseMapping, similarNounMappings, this);

            }
        }

    }

    private NounMapping getMostSimilarNounMappingOverThreshold(NounMapping nounMapping, ImmutableList<NounMapping> nounMappingsOfSimilarPhraseMapping) {

        MutableList<NounMapping> similarNounMappings = Lists.mutable.empty();

        for (NounMapping nounMappingOfSimilarPhraseMapping : nounMappingsOfSimilarPhraseMapping) {
            if (SimilarityUtils.areNounMappingsSimilar(nounMapping, nounMappingOfSimilarPhraseMapping)) {
                similarNounMappings.add(nounMappingOfSimilarPhraseMapping);
            }
        }

        if (similarNounMappings.size() != 1) {
            return null;
        }

        return similarNounMappings.get(0);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // No Delegates
    }
}
