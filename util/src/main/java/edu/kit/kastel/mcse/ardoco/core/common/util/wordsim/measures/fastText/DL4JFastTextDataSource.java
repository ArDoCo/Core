/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fastText;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.vector.VectorCache;
import org.deeplearning4j.models.fasttext.FastText;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import static edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.vector.VectorUtils.isZero;

/**
 * Provides the functionality of fastText using the DL4J (JFastText) native wrapper. This data source also has
 * additional caching functionality to improve lookup speeds. Instances of this class keep a loaded fastText binary
 * model active until {@link #close()} is called.
 */
public class DL4JFastTextDataSource implements AutoCloseable {

    private final FastText fastText;
    private final VectorCache cache;
    private final double[] zeroVector;

    /**
     * Instantiates the {@link DL4JFastTextDataSource}.
     *
     * @param modelPath the path to the binary fastText model
     */
    public DL4JFastTextDataSource(Path modelPath) {
        if (!Files.exists(modelPath)) {
            throw new IllegalArgumentException("modelPath does not exist: " + modelPath);
        }

        this.fastText = FastText.builder().build();
        this.fastText.loadBinaryModel(modelPath.toString());
        this.cache = new VectorCache();
        this.zeroVector = new double[this.fastText.getDimension()];
    }

    /**
     * Attempts to retrieve the word vector for the given word.
     *
     * @param word the word
     * @return the word vector, or {@link Optional#empty()} if no vector was found.
     */
    public Optional<double[]> getWordVector(String word) {
        Objects.requireNonNull(word);

        double[] vector;

        if (this.cache.contains(word)) {
            vector = this.cache.getOrDefault(word, zeroVector);
        } else {
            vector = this.fastText.getWordVector(word);
            this.cache.store(word, vector);
        }

        return isZero(vector) ? Optional.empty() : Optional.of(vector);
    }

    /**
     * Unloads the binary model from this data source and clears the cache.
     */
    @Override
    public void close() {
        this.fastText.unloadBinaryModel();
        this.cache.clear();
    }

}
