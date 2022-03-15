/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.util.wordsim.measures;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fastText.DL4JFastTextDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

/**
 * Tests for the {@link FastTextDataSourceTest}.
 */
public class FastTextDataSourceTest {

    private DL4JFastTextDataSource dataSource;

    @BeforeAll
    public void beforeAll() {
        this.dataSource = new DL4JFastTextDataSource(Path.of(CommonTextToolsConfig.FASTTEXT_MODEL_FILE_PATH));
    }

    @AfterAll
    public void afterAll() {
        this.dataSource.close();
    }

    @Test
    @Disabled
    public void manualTest() {
        var similarity = dataSource.getSimilarity("tree", "cloud").orElse(Double.NaN);
        System.out.println(similarity);
    }

}
