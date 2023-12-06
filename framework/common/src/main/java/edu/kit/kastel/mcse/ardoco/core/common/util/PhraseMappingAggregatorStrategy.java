/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.common.util;

import static edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils.cosineSimilarity;
import static edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils.uniqueDot;

import java.io.Serializable;
import java.util.function.ToDoubleBiFunction;

import edu.kit.kastel.mcse.ardoco.core.api.textextraction.PhraseMapping;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

@Deterministic
public enum PhraseMappingAggregatorStrategy implements ToDoubleBiFunction<PhraseMapping, PhraseMapping> {
    MAX_SIMILARITY((a, b) -> uniqueDot(a.getPhrases().toImmutableList(), b.getPhrases().toImmutableList()).stream()
            .mapToDouble(p -> cosineSimilarity(p.first().getPhraseVector().toSortedMap(), p.second().getPhraseVector().toSortedMap()))
            .max()
            .orElse(Double.NaN)), //

    MIN_SIMILARITY((a, b) -> uniqueDot(a.getPhrases().toImmutableList(), b.getPhrases().toImmutableList()).stream()
            .mapToDouble(p -> cosineSimilarity(p.first().getPhraseVector().toSortedMap(), p.second().getPhraseVector().toSortedMap()))
            .min()
            .orElse(Double.NaN)), //

    AVG_SIMILARITY((a, b) -> uniqueDot(a.getPhrases().toImmutableList(), b.getPhrases().toImmutableList()).stream()
            .mapToDouble(p -> cosineSimilarity(p.first().getPhraseVector().toSortedMap(), p.second().getPhraseVector().toSortedMap()))
            .average()
            .orElse(Double.NaN));

    private final ToDoubleBiFunctionSerializable<PhraseMapping, PhraseMapping> mapper;

    PhraseMappingAggregatorStrategy(ToDoubleBiFunctionSerializable<PhraseMapping, PhraseMapping> mapper) {
        this.mapper = mapper;
    }

    @Override
    public double applyAsDouble(PhraseMapping phraseMapping, PhraseMapping phraseMapping2) {
        return this.mapper.applyAsDouble(phraseMapping, phraseMapping2);
    }

    public interface ToDoubleBiFunctionSerializable<T, U> extends ToDoubleBiFunction<T, U>, Serializable {
    }
}
