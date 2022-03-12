package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fastText;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.VectorCache;
import org.deeplearning4j.models.fasttext.FastText;

import java.nio.file.Path;
import java.util.Optional;

import static edu.kit.kastel.mcse.ardoco.core.common.util.VectorUtils.cosineSimilarity;
import static edu.kit.kastel.mcse.ardoco.core.common.util.VectorUtils.isZero;

public class DL4JFastTextDataSource implements FastTextDataSource {

    private final FastText fastText;
    private final VectorCache cache;
    private final double[] zeroVector;

    public DL4JFastTextDataSource(Path modelPath) {
        this.fastText = FastText.builder().build();
        this.fastText.loadBinaryModel(modelPath.toString());
        this.cache = new VectorCache();
        this.zeroVector = new double[this.fastText.getDimension()];
    }

    public Optional<double[]> getWordVector(String word) {
        double[] vector;

        if (this.cache.contains(word)) {
            vector = this.cache.getOrDefault(word, zeroVector);
        }
        else {
            vector = this.fastText.getWordVector(word);
            this.cache.store(word, vector);
        }

        return isZero(vector) ? Optional.empty() : Optional.of(vector);
    }

    @Override public Optional<Double> getSimilarity(String firstWord, String secondWord) {
        var firstVec = getWordVector(firstWord).orElse(null);
        if (firstVec == null) { return Optional.empty(); }

        var secondVec = getWordVector(secondWord).orElse(null);
        if (secondVec == null) { return Optional.empty(); }

        return Optional.of(cosineSimilarity(firstVec, secondVec));
    }

    @Override public void close() {
        this.fastText.unloadBinaryModel();
    }

}
