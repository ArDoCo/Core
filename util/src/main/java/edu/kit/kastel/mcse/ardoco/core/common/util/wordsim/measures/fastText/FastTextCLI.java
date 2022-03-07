/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fastText;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static edu.kit.kastel.mcse.ardoco.core.common.util.VectorUtils.cosineSimilarity;
import static edu.kit.kastel.mcse.ardoco.core.common.util.VectorUtils.isZero;

/**
 * Provides the fastText word vector embeddings by actively communicating with a compiled fastText binary. Instances of
 * this class keep a fastText CLI process alive until {@link #close()} is called.
 */
public class FastTextCLI implements FastTextDataSource {

    private final CLIProcess process;
    private final int dimension;
    private final double[] zeroVector;

    /**
     * Constructs a new fastText CLI process.
     *
     * @param executablePath the path to an executable fastText executable
     * @param modelPath      the path to the model that the binary should use for word embeddings
     * @throws IOException if launching the fastText CLI process fails
     */
    public FastTextCLI(Path executablePath, Path modelPath) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder().command(executablePath.toString(), "print-word-vectors", modelPath.toString());

        this.process = new CLIProcess(processBuilder);

        // Determine dimension
        this.process.sendLineToInput("a");
        this.dimension = this.process.readLineFromOutput().split(" ").length - 1;
        this.zeroVector = new double[this.dimension];
    }

    /**
     * Attempts to retrieve the vector representation of the given word.
     *
     * @param word the word
     * @return the vector representation, or {@link Optional#empty()} if no representation for the given word is found
     * @throws RetrieveVectorException  if the retrieval process fails
     * @throws IllegalArgumentException if the given string contains spaces (not a single word)
     */
    public Optional<double[]> getWordVector(String word) throws RetrieveVectorException, IllegalArgumentException {
        try {
            if (word.contains(" ")) {
                throw new IllegalArgumentException("given string is not a single word: " + word);
            }

            this.process.sendLineToInput("a " + word);
            // ^ Prepend the word 'a' to the input to give fastText some other word first
            // since the output line for the first word is for some reason scrambled and cannot be read properly

            this.process.readLineFromOutput(); // first output is always garbage

            String[] output = this.process.readLineFromOutput().split(" ");
            double[] vector = new double[output.length - 1];

            for (int i = 0; i < vector.length; i++) {
                vector[i] = Double.parseDouble(output[i + 1]);
            }

            return isZero(vector) ? Optional.empty() : Optional.of(vector);
        } catch (IOException e) {
            throw new RetrieveVectorException(word, e);
        }
    }

    /**
     * Attempts to retrieve the average vector representation for the given words.
     *
     * @param words the words
     * @return the vector representation, or {@link Optional#empty()} if no representation for the given words are found
     * @throws RetrieveVectorException if the retrieval process fails
     */
    public Optional<double[]> getWordsVector(String[] words) throws RetrieveVectorException {
        double[] resultVector = new double[this.dimension];

        for (String word : words) {
            var wordVector = getWordVector(word).orElse(zeroVector);
            add(resultVector, wordVector);
        }

        scale(resultVector, 1.0 / words.length);

        return isZero(resultVector) ? Optional.empty() : Optional.of(resultVector);
    }

    @Override public Optional<Double> getSimilarity(String firstWord, String secondWord) throws RetrieveVectorException {
        double[] firstVec = getWordsVector(firstWord.split(" ")).orElse(null);
        double[] secondVec = getWordsVector(secondWord.split(" ")).orElse(null);

        if (firstVec == null || secondVec == null) {
            return Optional.empty();
        }

        return Optional.of(cosineSimilarity(firstVec, secondVec));
    }

    @Override public void close() {
        this.process.close();
    }

    private void add(double[] target, double[] toAdd) {
        for (int i = 0; i < target.length; i++) {
            target[i] += toAdd[i];
        }
    }

    private void scale(double[] target, double scalar) {
        for (int i = 0; i < target.length; i++) {
            target[i] = target[i] * scalar;
        }
    }

}
