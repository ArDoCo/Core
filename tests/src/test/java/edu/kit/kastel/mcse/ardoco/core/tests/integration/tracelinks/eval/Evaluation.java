/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fastText.DL4JFastTextDataSource;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fastText.FastTextMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.glove.GloveMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.jarowinkler.JaroWinklerMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.levenshtein.LevenshteinMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.NasariMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet.BabelNetDataSource;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.ngram.NgramMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.sewordsim.SEWordSimDataSource;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.sewordsim.SEWordSimMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.wordnet.Ezzikouri;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.wordnet.WordNetMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.vector.VectorSqliteDatabase;
import edu.mit.jwi.IRAMDictionary;
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.data.ILoadPolicy;
import edu.uniba.di.lacam.kdde.lexical_db.ILexicalDatabase;
import edu.uniba.di.lacam.kdde.lexical_db.MITWordNet;
import edu.uniba.di.lacam.kdde.ws4j.similarity.JiangConrath;
import edu.uniba.di.lacam.kdde.ws4j.similarity.LeacockChodorow;
import edu.uniba.di.lacam.kdde.ws4j.similarity.Lesk;
import edu.uniba.di.lacam.kdde.ws4j.similarity.WuPalmer;
import edu.uniba.di.lacam.kdde.ws4j.util.WS4JConfiguration;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Evaluation {

    @Test
    @Disabled
    public void run() throws IOException, SQLException {
        var overwriteExistingResults = false;
        var plans = getPlans();
        var resultDir = Path.of("eval_results/");
        var latexDir = Path.of("latex_outputs/");

        new Evaluator(plans, resultDir, overwriteExistingResults).execute();

        new LatexOutputGenerator(resultDir, latexDir).run();
    }

    public static List<EvalPlan> getPlans() throws IOException, SQLException {
        var plans = new ArrayList<EvalPlan>();

        boolean jaroWinkler = false;
        boolean levenshtein = true;
        boolean ngram = false;
        boolean sewordsim = false;
        boolean fastText = false;
        boolean wordNetWP = false, wordNetLC = false, wordNetJC = false, wordNetLesk = false, wordNetEzzikouri = false;
        boolean glove = false;
		boolean nasari = false;

        // 1.) Load models/resources

        // fastText
        String fastTextModelName = "";
        DL4JFastTextDataSource fastTextDataSource = null;
        if (fastText) {
            var modelPath = Path.of(CommonTextToolsConfig.FASTTEXT_MODEL_FILE_PATH);
            fastTextModelName = modelPath.getFileName().toString().replace(".bin", "");
            fastTextDataSource = new DL4JFastTextDataSource(modelPath);
        }

        // WordNet
        ILexicalDatabase wordNetDB = null;
        if (wordNetWP || wordNetLC || wordNetJC || wordNetLesk || wordNetEzzikouri) {
            WS4JConfiguration.getInstance().setCache(true);
            WS4JConfiguration.getInstance().setStem(true);

            Path wordNetDirPath = Path.of(CommonTextToolsConfig.WORDNET_DATA_DIR_PATH);
            IRAMDictionary dictionary = new RAMDictionary(wordNetDirPath.toFile(), ILoadPolicy.BACKGROUND_LOAD);
            dictionary.open();

            wordNetDB = new MITWordNet(dictionary);
        }

        // SEWordSim
        SEWordSimDataSource seWordSimDataSource = null;
        if (sewordsim) {
            seWordSimDataSource = new SEWordSimDataSource(Path.of(CommonTextToolsConfig.SEWORDSIM_DB_FILE_PATH));
        }

        // GloVe
        VectorSqliteDatabase gloveDataSource = null;
        String gloveDataSourceName = "";
        if (glove) {
            var dbPath = Path.of(CommonTextToolsConfig.GLOVE_DB_FILE_PATH);
            gloveDataSourceName = dbPath.getFileName().toString().replace(".sqlite", "");
            gloveDataSource = new VectorSqliteDatabase(dbPath);
        }

        // Nasari
	    BabelNetDataSource babelNetDataSource = null;
	    VectorSqliteDatabase nasariVectorDatabase = null;
		if (nasari) {
			babelNetDataSource = new BabelNetDataSource(
				CommonTextToolsConfig.BABELNET_API_KEY, Path.of(CommonTextToolsConfig.BABELNET_CACHE_FILE_PATH)
			);
			nasariVectorDatabase = new VectorSqliteDatabase(Path.of(CommonTextToolsConfig.NASARI_DB_FILE_PATH));
		}

        // 2.) Construct plans

	    for (Baseline b : Baseline.values()) {
		    for (int t = 0; t <= 100; t += 10) {
			    double threshold = t / 100.0;

			    // Jaro Winkler
			    if (jaroWinkler && b == Baseline.FIRST) {
				    plans.add(new EvalPlan("jaroWinkler", b, t, new JaroWinklerMeasure(threshold)));
			    }

			    // Levenshtein
			    if (levenshtein && b == Baseline.FIRST) {
                    for (int minLength = 0; minLength < 13; minLength++) {
                        for (int maxDistance = 0; maxDistance < 13; maxDistance++) {
                            var group = String.format("levenshtein_%sL_%sD", minLength, maxDistance);
                            plans.add(new EvalPlan(group, b, t, new LevenshteinMeasure(minLength, maxDistance, threshold)));
                        }
                   }
			    }

			    // Ngram
			    if (ngram) {
                    for (int n = 2; n <= 3; n++) {
                        var group = String.format("ngram_n%s", n);
                        plans.add(new EvalPlan(group, b, t, new NgramMeasure(NgramMeasure.Variant.LUCENE, n, threshold)));
                    }
			    }

			    // SEWordSim
			    if (sewordsim) {
				    plans.add(new EvalPlan("sewsim", b, t, new SEWordSimMeasure(seWordSimDataSource, threshold)));
			    }

			    // fastText
			    if (fastText) {
				    plans.add(new EvalPlan("fastText_" + fastTextModelName, b, t, new FastTextMeasure(fastTextDataSource, threshold)));
			    }

			    // WordNet
			    if (wordNetWP) {
				    plans.add(new EvalPlan("wordNet_WP", b, t, new WordNetMeasure(Map.of(new WuPalmer(wordNetDB), threshold))));
			    }
			    if (wordNetLC) {
				    plans.add(new EvalPlan("wordNet_LC", b, t, new WordNetMeasure(Map.of(new LeacockChodorow(wordNetDB), threshold))));
			    }
			    if (wordNetJC) {
				    plans.add(new EvalPlan("wordNet_JC", b, t, new WordNetMeasure(Map.of(new JiangConrath(wordNetDB), threshold))));
			    }
			    if (wordNetLesk) {
				    plans.add(new EvalPlan("wordNet_Lesk", b, t, new WordNetMeasure(Map.of(new Lesk(wordNetDB), threshold))));
			    }
			    if (wordNetEzzikouri) {
				    plans.add(new EvalPlan("wordNet_Ezzi", b, t, new WordNetMeasure(Map.of(new Ezzikouri(wordNetDB), threshold))));
			    }

				// GloVe
                if (glove) {
                    plans.add(new EvalPlan("glove_" + gloveDataSourceName, b, t, new GloveMeasure(gloveDataSource, threshold)));
                }

				// Nasari
	            if (nasari) {
					plans.add(new EvalPlan("nasari", b, t, new NasariMeasure(babelNetDataSource, nasariVectorDatabase, threshold)));
	            }
            }
        }

        return plans;
    }

}
