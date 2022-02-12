package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.equality.EqualityMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.jarowinkler.JaroWinklerMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.levenshtein.LevenshteinMeasure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WordSimConfig {

    public static final WordSimConfig DEFAULT = new WordSimConfig(CommonTextToolsConfig.JAROWINKLER_SIMILARITY_THRESHOLD,
            List.of(new EqualityMeasure(), new JaroWinklerMeasure(), new LevenshteinMeasure()));

    private final double defaultSimilarityThreshold;
    private final List<WordSimMeasure> measures;

    public WordSimConfig(double defaultSimilarityThreshold, Collection<WordSimMeasure> measures) {
        this.defaultSimilarityThreshold = defaultSimilarityThreshold;
        this.measures = new ArrayList<>(measures);
    }

    public double getDefaultSimilarityThreshold() {
        return defaultSimilarityThreshold;
    }

    public List<WordSimMeasure> getMeasures() {
        return measures;
    }

    @Override public String toString() {
        return "WordSimConfig{" + "defaultSimilarityThreshold=" + defaultSimilarityThreshold + ", measures=" + measures + '}';
    }

}
