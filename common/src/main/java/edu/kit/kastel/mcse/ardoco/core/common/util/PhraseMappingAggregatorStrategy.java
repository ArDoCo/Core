/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util;

import static edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils.cosineSimilarity;
import static edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils.uniqueDot;

import java.util.function.ToDoubleBiFunction;

import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.PhraseMapping;

public enum PhraseMappingAggregatorStrategy implements ToDoubleBiFunction<PhraseMapping, PhraseMapping> {
    MAX_SIMILARITY((a, b) -> uniqueDot(a.getPhrases(), b.getPhrases()).stream()
            .mapToDouble(p -> cosineSimilarity(p.first().getPhraseVector().toMap(), p.second().getPhraseVector().toMap()))
            .max()
            .orElse(Double.NaN)), //

    MIN_SIMILARITY((a, b) -> uniqueDot(a.getPhrases(), b.getPhrases()).stream()
            .mapToDouble(p -> cosineSimilarity(p.first().getPhraseVector().toMap(), p.second().getPhraseVector().toMap()))
            .min()
            .orElse(Double.NaN)), //

    AVG_SIMILARITY((a, b) -> uniqueDot(a.getPhrases(), b.getPhrases()).stream()
            .mapToDouble(p -> cosineSimilarity(p.first().getPhraseVector().toMap(), p.second().getPhraseVector().toMap()))
            .average()
            .orElse(Double.NaN));

    private final ToDoubleBiFunction<PhraseMapping, PhraseMapping> mapper;

    PhraseMappingAggregatorStrategy(ToDoubleBiFunction<PhraseMapping, PhraseMapping> mapper) {
        this.mapper = mapper;
    }

    @Override
    public double applyAsDouble(PhraseMapping phraseMapping, PhraseMapping phraseMapping2) {
        return this.mapper.applyAsDouble(phraseMapping, phraseMapping2);
    }
}
