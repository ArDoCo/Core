package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A cache for storing word-to-vector mappings.
 */
public class VectorCache {

    private final Map<String, double[]> map = new HashMap<>();

    /**
     * Stores the given word with the given vector.
     * Replaces any previous word-to-vector mapping that had the given word.
     *
     * @param word   the word
     * @param vector the vector
     */
    public void store(String word, double[] vector) {
        this.map.put(word, vector);
    }

    /**
     * Attempts to retrieve the vector that has been stored for the given word.
     *
     * @param word the word
     * @return the vector that the given word was mapped to, or {@link Optional#empty()} if no vector has been associated with the given word
     */
    public Optional<double[]> get(String word) {
        return Optional.ofNullable(this.map.get(word));
    }

    /**
     * Attempts to retrieve the vector that has been stored for the given word.
     *
     * @param word        the word
     * @param alternative an alternative vector
     * @return the vector that the given word was mapped to, or the alternative vector if no vector has been associated with the given word
     */
    public double[] getOrDefault(String word, double[] alternative) {
        var stored = this.map.get(word);
        return stored == null ? alternative : stored;
    }

    /**
     * Checks whether the given word has a mapping in this cache.
     *
     * @param word the word
     * @return {@code true} if this cache contains a mapping for the given word
     */
    public boolean contains(String word) {
        return this.map.containsKey(word);
    }

    /**
     * Removes all entries from this cache.
     */
    public void clear() {
        this.map.clear();
    }

}
