/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fastText;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

/**
 * Provides the fastText word vector embeddings by actively communicating with a compiled fastText binary. Instances of
 * this class keep a fastText CLI process alive until {@link #close()} is called.
 */
public class FastTextCLI implements FastTextDataSource {

    private static final float[] ZERO_VECTOR = new float[0];

    // --- REMOVE --------------------------------------------------------------------------
    public static void main(String[] args) throws RetrieveVectorException, IOException {
        var exePath = Path.of("C:\\dev\\uni\\ArDoCo\\fastText-0.9.2\\fasttext.exe");
        var modelPath = Path.of("C:\\dev\\uni\\ArDoCo\\fastText-0.9.2\\dbpedia.bin");

        try (var cli = new FastTextCLI(exePath, modelPath)) {
            System.out.println(Arrays.toString(cli.getWordVector("tree").orElse(ZERO_VECTOR)));
            System.out.println(Arrays.toString(cli.getWordVector("apple").orElse(ZERO_VECTOR)));
            System.out.println(cli.getSimilarity("tree", "apple"));

        }
    }
    // ------------------------------------------------------------------------------------

    private final CLIProcess process;

    /**
     * Constructs a new fastText CLI process.
     *
     * @param executablePath the path to an executable fastText executable
     * @param modelPath      the path to the model that the binary should use for word embeddings
     * @throws IOException if launching the fastText CLI process fails
     */
    public FastTextCLI(Path executablePath, Path modelPath) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder().command(executablePath.toAbsolutePath().toString(), "print-word-vectors",
                modelPath.toAbsolutePath().toString());

        this.process = new CLIProcess(processBuilder);
    }

    /**
     * Attempts to retrieve the vector representation of the given word.
     *
     * @param word the word
     * @return the vector representation, or {@link Optional#empty()} if no representation for the given word is found
     * @throws RetrieveVectorException if the retrieval process fails
     */
    public Optional<float[]> getWordVector(String word) throws RetrieveVectorException {
        try {
            this.process.sendToInput("a " + word + "\n");
            // ^ Prepend the word 'a' to the input to give fastText some other word first
            // since the output line for the first word is for some reason scrambled and cannot be read properly

            this.process.readLineFromOutput(); // first output is always garbage

            String[] output = this.process.readLineFromOutput().split(" ");
            float[] vector = new float[output.length - 1];

            for (int i = 0; i < vector.length; i++) {
                vector[i] = Float.parseFloat(output[i + 1]);
            }

            return isZero(vector) ? Optional.empty() : Optional.of(vector);
        } catch (IOException e) {
            throw new RetrieveVectorException(word, e);
        }
    }

    @Override
    public Optional<Double> getSimilarity(String firstWord, String secondWord) throws RetrieveVectorException {
        float[] firstVec = getWordVector(firstWord).orElse(ZERO_VECTOR);
        float[] secondVec = getWordVector(secondWord).orElse(ZERO_VECTOR);

        if (isZero(firstVec) || isZero(secondVec)) {
            return Optional.empty();
        }

        return Optional.of(cosineSimilarity(firstVec, secondVec));
    }

    private double cosineSimilarity(float[] firstVector, float[] secondVector) {
        double dotProduct = 0.0, firstNorm = 0.0, secondNorm = 0.0;

        for (int i = 0; i < firstVector.length; i++) {
            dotProduct += firstVector[i] * secondVector[i];
            firstNorm += Math.pow(firstVector[i], 2);
            secondNorm += Math.pow(secondVector[i], 2);
        }

        return dotProduct / (Math.sqrt(firstNorm) * Math.sqrt(secondNorm));
    }

    private boolean isZero(float[] vector) {
        if (vector.length <= 0) {
            return true;
        }

        for (float entry : vector) {
            if (entry != 0.0f) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void close() {
        this.process.close();
    }

}
