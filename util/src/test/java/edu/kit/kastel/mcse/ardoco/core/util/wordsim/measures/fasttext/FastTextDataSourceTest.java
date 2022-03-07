package edu.kit.kastel.mcse.ardoco.core.util.wordsim.measures.fasttext;

import edu.kit.kastel.mcse.ardoco.core.common.util.VectorUtils;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fastText.FastTextCLI;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fastText.RetrieveVectorException;
import org.deeplearning4j.models.fasttext.FastText;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

public class FastTextDataSourceTest {

    String first = "BigBlueButton HTML5";
    String second = "BigBlueButton server";

    @Test
    @Disabled
    public void manualDL4JTest() {
        Path modelPath = Path.of("C:\\dev\\uni\\ardoco\\tests\\crawl-300d-2M-subword.bin");

        var fastText = FastText.builder().build();
        fastText.loadBinaryModel(modelPath.toString());

        var firstVec = fastText.getWordVector(first);
        var secondVec = fastText.getWordVector(second);

        var similarity = VectorUtils.cosineSimilarity(firstVec, secondVec);

        System.out.println(Arrays.toString(firstVec));
        System.out.println(Arrays.toString(secondVec));
        System.out.println(similarity);
    }

    @Test
    @Disabled
    public void manualCLITest() throws IOException, RetrieveVectorException {
        Path modelPath = Path.of("crawl-300d-2M-subword.bin");
        Path exePath = Path.of("fasttext.exe");
        var cli = new FastTextCLI(exePath, modelPath);

        var firstVec = cli.getWordsVector(first.split(" ")).orElse(null);
        var secondVec = cli.getWordsVector(second.split(" ")).orElse(null);

        var similarity = VectorUtils.cosineSimilarity(firstVec, secondVec);

        System.out.println(Arrays.toString(firstVec));
        System.out.println(Arrays.toString(secondVec));
        System.out.println(similarity);
    }

    @Test
    @Disabled
    public void manualSqliteTest() {
//        Path dbPath = Path.of("C:\\dev\\uni\\fastText-0.9.2\\vectors.sqlite");
//        var db = new SqliteFastTextDataSource(dbPath);
//
//        db.close();
    }

}
