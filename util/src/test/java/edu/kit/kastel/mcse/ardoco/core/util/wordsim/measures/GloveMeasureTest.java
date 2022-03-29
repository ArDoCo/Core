package edu.kit.kastel.mcse.ardoco.core.util.wordsim.measures;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.glove.GloveSqliteDataSource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Arrays;

public class GloveMeasureTest {

    @Test
    @Disabled
    public void manualTest() throws SQLException {
        var glove = new GloveSqliteDataSource(Path.of(CommonTextToolsConfig.GLOVE_DB_FILE_PATH));
        var m = glove.getWordVector("man").orElseThrow();
        var w = glove.getWordVector("woman").orElseThrow();

        System.out.println(Arrays.toString(m));
        System.out.println(Arrays.toString(w));
    }

}
