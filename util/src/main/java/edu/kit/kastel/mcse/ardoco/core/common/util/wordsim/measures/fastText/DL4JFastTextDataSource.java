/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fastText;

import org.deeplearning4j.models.fasttext.FastText;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import static edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.vector.VectorUtils.isZero;

/**
 * Provides the functionality of fastText using the DL4J (JFastText) native wrapper.
 * Instances of this class keep a loaded fastText binary model active until {@link #close()} is called.
 */
public class DL4JFastTextDataSource implements AutoCloseable {

    private final FastText fastText;

    /**
     * Instantiates the {@link DL4JFastTextDataSource}.
     * Once instantiated, the loaded fastText model will be kept open until {@link #close()} is called.
     *
     * @param modelPath the path to the binary fastText model
     */
    public DL4JFastTextDataSource(Path modelPath) {
        if (!Files.exists(modelPath)) {
            throw new IllegalArgumentException("modelPath does not exist: " + modelPath);
        }

        this.fastText = FastText.builder().build();
        this.fastText.loadBinaryModel(modelPath.toString());
    }

    /**
     * Attempts to retrieve the word vector for the given word.
     *
     * @param word the word
     * @return the word vector, or {@link Optional#empty()} if no vector representation for the given word exists.
     */
    public Optional<double[]> getWordVector(String word) {
        Objects.requireNonNull(word);

        double[] vector = this.fastText.getWordVector(word);

        return isZero(vector) ? Optional.empty() : Optional.of(vector);
    }

    /**
     * Unloads the binary model from this data source.
     */
    @Override public void close() {
        this.fastText.unloadBinaryModel();
    }

}
