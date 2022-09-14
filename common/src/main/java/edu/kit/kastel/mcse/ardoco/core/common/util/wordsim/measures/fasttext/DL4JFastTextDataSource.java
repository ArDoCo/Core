/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fasttext;

import static edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.vector.VectorUtils.isZero;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import org.deeplearning4j.models.fasttext.FastText;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.vector.WordVectorDataSource;

/**
 * Provides the functionality of fastText using the DL4J (JFastText) native wrapper. Instances of this class keep a
 * loaded fastText binary model active until {@link #close()} is called.
 */
public class DL4JFastTextDataSource implements WordVectorDataSource, AutoCloseable {

    private final FastText fastText;

    /**
     * Instantiates the {@link DL4JFastTextDataSource}. Once instantiated, the loaded fastText model will be kept open
     * until {@link #close()} is called.
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
    @Override
    public Optional<float[]> getWordVector(String word) {
        Objects.requireNonNull(word);

        double[] doubleVector = this.fastText.getWordVector(word);

        if (isZero(doubleVector)) {
            return Optional.empty();
        }

        // Convert double vector to float vector
        float[] floatVector = new float[doubleVector.length];

        for (int i = 0; i < floatVector.length; i++) {
            floatVector[i] = (float) doubleVector[i];
        }

        return Optional.of(floatVector);
    }

    /**
     * Unloads the binary model from this data source.
     */
    @Override
    public void close() {
        this.fastText.unloadBinaryModel();
    }

}
