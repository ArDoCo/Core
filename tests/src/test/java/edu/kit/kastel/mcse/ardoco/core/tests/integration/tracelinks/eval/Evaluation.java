/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.jarowinkler.JaroWinklerMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.ngram.NgramMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.sewordsim.SEWordSimDataSource;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.sewordsim.SEWordSimMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.wordnet.WordNetMeasure;
import edu.mit.jwi.IRAMDictionary;
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.data.ILoadPolicy;
import edu.uniba.di.lacam.kdde.lexical_db.ILexicalDatabase;
import edu.uniba.di.lacam.kdde.lexical_db.MITWordNet;
import edu.uniba.di.lacam.kdde.ws4j.RelatednessCalculator;
import edu.uniba.di.lacam.kdde.ws4j.similarity.JiangConrath;
import edu.uniba.di.lacam.kdde.ws4j.similarity.LeacockChodorow;
import edu.uniba.di.lacam.kdde.ws4j.similarity.Lesk;
import edu.uniba.di.lacam.kdde.ws4j.similarity.WuPalmer;
import edu.uniba.di.lacam.kdde.ws4j.util.WS4JConfiguration;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Evaluation {

    public static void main(String[] args) throws IOException, SQLException {
        var plans = getPlans();
        var resultDir = Path.of("eval_results");
        var evaluator = new Evaluator(plans, resultDir);

        evaluator.execute();
    }

    public static List<EvalPlan> getPlans() throws IOException, SQLException {
        var plans = new ArrayList<EvalPlan>();

        boolean jaroWinkler = false;
        boolean levenshtein = false;
        boolean ngram = false;
        boolean sewordsim = false;
        boolean wordNetWP = false, wordNetLC = true, wordNetJC = true, wordNetLesk = true, wordNetEzzikouri = false;

        for (int b = 1; b <= 2; b++) {
            for (int t = 10; t <= 90; t += 10) {
                double threshold = t / 100.0;

                // Jaro Winkler
                if (jaroWinkler && b == 1) {
                    plans.add(new EvalPlan("jaroWinkler", b, t, new JaroWinklerMeasure(threshold)));
                }

                // Levenshtein
                if (levenshtein) {
                    throw new UnsupportedOperationException();
                }

                // Ngram
                if (ngram) {
                    plans.add(new EvalPlan("bigram", b, t, new NgramMeasure(NgramMeasure.Variant.LUCENE, 2, threshold)));
                    // plans.add(new EvalPlan("trigram", b, t, new NgramMeasure(NgramMeasure.Variant.LUCENE, 3,
                    // threshold)));
                }

                // SEWordSim
                if (sewordsim) {
                    var dataSource = new SEWordSimDataSource(Path.of(CommonTextToolsConfig.SEWORDSIM_DB_FILE_PATH));
                    plans.add(new EvalPlan("sewsim", b, t, new SEWordSimMeasure(dataSource, threshold)));
                }

                // WordNet
                if (wordNetWP || wordNetLC || wordNetJC || wordNetLesk || wordNetEzzikouri) {
                    WS4JConfiguration.getInstance().setCache(true);
                    WS4JConfiguration.getInstance().setStem(true);

                    Path wordNetDirPath = Path.of(CommonTextToolsConfig.WORDNET_DATA_DIR_PATH);
                    IRAMDictionary dictionary = new RAMDictionary(wordNetDirPath.toFile(), ILoadPolicy.BACKGROUND_LOAD);
                    dictionary.open();

                    ILexicalDatabase db = new MITWordNet(dictionary);

                    String group = "wordNet_";
                    var calcThresholdMap = new HashMap<RelatednessCalculator, Double>();

                    if (wordNetWP) {
                        group += "WP";
                        calcThresholdMap.put(new WuPalmer(db), threshold);
                    }
                    if (wordNetLC) {
                        group += "LC";
                        calcThresholdMap.put(new LeacockChodorow(db), threshold);
                    }
                    if (wordNetJC) {
                        group += "JC";
                        calcThresholdMap.put(new JiangConrath(db), threshold);
                    }
                    if (wordNetLesk) {
                        group += "Lesk";
                        calcThresholdMap.put(new Lesk(db), threshold);
                    }
                    if (wordNetEzzikouri) {
                        throw new UnsupportedOperationException();
                    }

                    plans.add(new EvalPlan(group, b, t, new WordNetMeasure(calcThresholdMap)));
                }
            }
        }

        return plans;
    }

}
