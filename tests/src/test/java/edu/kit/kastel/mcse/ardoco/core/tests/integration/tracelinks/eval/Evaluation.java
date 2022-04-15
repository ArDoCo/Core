/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fastText.DL4JFastTextDataSource;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fastText.FastTextMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.glove.GloveMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.glove.GloveSqliteDataSource;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.jarowinkler.JaroWinklerMeasure;
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
        var plans = getPlans();
        var resultDir = Path.of("eval_results");
        var evaluator = new Evaluator(plans, resultDir);

        evaluator.execute();
    }

    public static List<EvalPlan> getPlans() throws IOException, SQLException {
        var plans = new ArrayList<EvalPlan>();

        // TODO:
        // - Levenshtein

        boolean jaroWinkler = false;
        boolean levenshtein = false;
        boolean ngram = false;
        boolean sewordsim = false;
        boolean fastText = false;
        boolean wordNetWP = false, wordNetLC = false, wordNetJC = false, wordNetLesk = false, wordNetEzzikouri = false;
        boolean glove = false;
		boolean nasari = false;

        String fastTextModelName = "";
        DL4JFastTextDataSource fastTextDataSource = null;
        if (fastText) {
            var modelPath = Path.of(CommonTextToolsConfig.FASTTEXT_MODEL_FILE_PATH);
            fastTextModelName = modelPath.getFileName().toString().replace(".bin", "");
            fastTextDataSource = new DL4JFastTextDataSource(modelPath);
        }

        ILexicalDatabase wordNetDB = null;
        if (wordNetWP || wordNetLC || wordNetJC || wordNetLesk || wordNetEzzikouri) {
            WS4JConfiguration.getInstance().setCache(true);
            WS4JConfiguration.getInstance().setStem(true);

            Path wordNetDirPath = Path.of(CommonTextToolsConfig.WORDNET_DATA_DIR_PATH);
            IRAMDictionary dictionary = new RAMDictionary(wordNetDirPath.toFile(), ILoadPolicy.BACKGROUND_LOAD);
            dictionary.open();

            wordNetDB = new MITWordNet(dictionary);
        }

        SEWordSimDataSource seWordSimDataSource = null;
        if (sewordsim) {
            seWordSimDataSource = new SEWordSimDataSource(Path.of(CommonTextToolsConfig.SEWORDSIM_DB_FILE_PATH));
        }

        GloveSqliteDataSource gloveDataSource = null;
        if (glove) {
            gloveDataSource = new GloveSqliteDataSource(Path.of(CommonTextToolsConfig.GLOVE_DB_FILE_PATH));
        }

	    BabelNetDataSource babelNetDataSource = null;
	    VectorSqliteDatabase nasariVectorDatabase = null;
		if (nasari) {
			babelNetDataSource = new BabelNetDataSource(
				CommonTextToolsConfig.BABELNET_API_KEY, Path.of(CommonTextToolsConfig.BABELNET_CACHE_FILE_PATH)
			);
			nasariVectorDatabase = new VectorSqliteDatabase(Path.of(CommonTextToolsConfig.NASARI_DB_FILE_PATH));
		}

	    for (Baseline b : Baseline.values()) {
		    for (int t = 0; t <= 100; t += 5) {
			    double threshold = t / 100.0;

			    // Jaro Winkler
			    if (jaroWinkler && b == Baseline.FIRST) {
				    plans.add(new EvalPlan("jaroWinkler", b, t, new JaroWinklerMeasure(threshold)));
			    }

			    // Levenshtein
			    if (levenshtein && b == Baseline.FIRST) {
				    throw new UnsupportedOperationException();
			    }

			    // Ngram
			    if (ngram) {
				    plans.add(new EvalPlan("bigram", b, t, new NgramMeasure(NgramMeasure.Variant.LUCENE, 2, threshold)));
				    plans.add(new EvalPlan("trigram", b, t, new NgramMeasure(NgramMeasure.Variant.LUCENE, 3, threshold)));
				    plans.add(new EvalPlan("4gram", b, t, new NgramMeasure(NgramMeasure.Variant.LUCENE, 4, threshold)));
				    plans.add(new EvalPlan("5gram", b, t, new NgramMeasure(NgramMeasure.Variant.LUCENE, 5, threshold)));
				    plans.add(new EvalPlan("6gram", b, t, new NgramMeasure(NgramMeasure.Variant.LUCENE, 6, threshold)));
				    plans.add(new EvalPlan("7gram", b, t, new NgramMeasure(NgramMeasure.Variant.LUCENE, 7, threshold)));
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
                    plans.add(new EvalPlan("glove_cc_300d", b, t, new GloveMeasure(gloveDataSource, threshold)));
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
