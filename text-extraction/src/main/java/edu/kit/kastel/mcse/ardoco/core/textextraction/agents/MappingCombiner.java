package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;

import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.informalin.framework.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.IPhraseMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

public class MappingCombiner extends TextAgent {

    public static final double MIN_COSINE_SIMILARITY = 0.4;

    @Override
    public void execute(TextAgentData data) {

        combineSimilarPhraseMappings(data.getTextState());
    }

    private void combineSimilarPhraseMappings(ITextState textState) {

        ImmutableList<IPhraseMapping> phraseMappings = textState.getPhraseMappings();

        for (IPhraseMapping phraseMapping : phraseMappings) {
            ImmutableList<IPhraseMapping> similarPhraseMappings = phraseMappings.select(p -> SimilarityUtils.getPhraseMappingSimilarity(textState,
                    phraseMapping, p, SimilarityUtils.PhraseMappingAggregatorStrategy.MAX_SIMILARITY) > MIN_COSINE_SIMILARITY);
            ImmutableList<INounMapping> nounMappingsOfPhraseMapping = textState.getNounMappingsByPhraseMapping(phraseMapping);

            for (IPhraseMapping similarPhraseMapping : similarPhraseMappings) {

                ImmutableList<INounMapping> nounMappingsOfSimilarPhraseMapping = textState.getNounMappingsByPhraseMapping(similarPhraseMapping);

                if (similarPhraseMapping == phraseMapping) {
                    for (INounMapping nounMappingOfSimilarPhraseMapping : nounMappingsOfSimilarPhraseMapping) {
                        for (INounMapping nounMapping : textState.getNounMappingsByPhraseMapping(phraseMapping)) {
                            if (nounMapping.isTheSameAs(nounMappingOfSimilarPhraseMapping)) {
                                continue;
                            }
                            if (SimilarityUtils.areNounMappingsSimilar(nounMapping, nounMappingOfSimilarPhraseMapping)) {
                                textState.mergeNounMappings(nounMapping, nounMappingOfSimilarPhraseMapping);
                            }
                        }
                    }
                    continue;
                }

                if (nounMappingsOfPhraseMapping.size() != nounMappingsOfSimilarPhraseMapping.size()) {
                    continue;
                }

                MutableList<Pair<INounMapping, INounMapping>> similarNounMappings = Lists.mutable.empty();

                for (INounMapping nounMapping : nounMappingsOfPhraseMapping) {
                    INounMapping similarNounMapping = getMostSimilarNounMappingOverThreshold(nounMapping, nounMappingsOfSimilarPhraseMapping);
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

                textState.mergePhraseMappingsAndNounMappings(phraseMapping, similarPhraseMapping, similarNounMappings);

            }
        }

    }

    private INounMapping getMostSimilarNounMappingOverThreshold(INounMapping nounMapping, ImmutableList<INounMapping> nounMappingsOfSimilarPhraseMapping) {

        MutableList<INounMapping> similarNounMappings = Lists.mutable.empty();

        for (INounMapping nounMappingOfSimilarPhraseMapping : nounMappingsOfSimilarPhraseMapping) {
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
