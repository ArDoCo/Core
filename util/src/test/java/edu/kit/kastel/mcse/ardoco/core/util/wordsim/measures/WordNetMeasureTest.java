/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.util.wordsim.measures;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.wordnet.WordNetMeasure;
import edu.mit.jwi.IRAMDictionary;
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.data.ILoadPolicy;
import edu.uniba.di.lacam.kdde.lexical_db.ILexicalDatabase;
import edu.uniba.di.lacam.kdde.lexical_db.MITWordNet;
import edu.uniba.di.lacam.kdde.ws4j.RelatednessCalculator;
import edu.uniba.di.lacam.kdde.ws4j.similarity.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the {@link WordNetMeasureTest}.
 */
public class WordNetMeasureTest {

    private IRAMDictionary dict;
    private ILexicalDatabase db;

    @BeforeEach
    public void beforeEach() throws IOException {
        Path wordNetDirPath = Path.of(CommonTextToolsConfig.WORDNET_DATA_DIR_PATH);
        if (!Files.exists(wordNetDirPath)) {
            throw new IllegalStateException("wordNetDir does not exist: " + wordNetDirPath);
        }

        dict = new RAMDictionary(wordNetDirPath.toFile(), ILoadPolicy.BACKGROUND_LOAD);
        dict.open();

        db = new MITWordNet(dict);
    }

    @AfterEach
    public void afterEach() {
        dict.close();
    }

    @Test
    @Disabled
    public void manualTest() {
        List<RelatednessCalculator> list = List.of(new edu.uniba.di.lacam.kdde.ws4j.similarity.Path(db), // [0.0, 1.0]
                new WuPalmer(db), // [0.0, 1.0]
                new JiangConrath(db), // [0.0, Double.MAX_VALUE]
                new LeacockChodorow(db), // [0.0, Double.MAX_VALUE]
                new Lin(db), // [0.0, 1.0]
                new Resnik(db), // [0.0, Double.MAX_VALUE]
                new Lesk(db), // [0.0, MAX_VALUE]
                new HirstStOnge(db) // [0.0, 16.0]
        );

        for (RelatednessCalculator calculator : list) {
            double similarity = calculator.calcRelatednessOfWords("dog", "hound dog");

            double normalizedSimilarity = (similarity - calculator.getMin()) / calculator.getMax();

            System.out.print(calculator.getClass().getSimpleName() + ": ");
            System.out.print(normalizedSimilarity);
            System.out.print(" (" + similarity + ") ");

            if (similarity == Double.MAX_VALUE) {
                System.out.print("<- MAX_VALUE");
            }

            System.out.println();
        }
    }

    @Test
    @Disabled
    public void testEquals() {
        Map<RelatednessCalculator, Double> calcThresholdMap = Map.of(new LeacockChodorow(db), 1.0, new WuPalmer(db), 0.95, new JiangConrath(db), 1.0,
                new Lesk(db), 1.0);

        var measure = new WordNetMeasure(calcThresholdMap);

        assertTrue(measure.areWordsSimilar(new ComparisonContext("dog", "Canis familiaris")));
        assertFalse(measure.areWordsSimilar(new ComparisonContext("dog", "hound dog")));
    }

}
