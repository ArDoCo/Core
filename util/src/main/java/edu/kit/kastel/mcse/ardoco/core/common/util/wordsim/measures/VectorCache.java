package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class VectorCache {

    private final Map<String, double[]> map = new HashMap<>();

    public void store(String word, double[] vector) {
        this.map.put(word, vector);
    }

    public Optional<double[]> get(String word) {
        return Optional.ofNullable(this.map.get(word));
    }

    public double[] getOrDefault(String word, double[] alternative) {
        var stored = this.map.get(word);
        return stored == null ? alternative : stored;
    }

    public boolean contains(String word) {
        return this.map.containsKey(word);
    }

}
