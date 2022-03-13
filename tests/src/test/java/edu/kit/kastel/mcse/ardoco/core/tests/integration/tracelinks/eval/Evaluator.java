package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.tests.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.files.TLBinResultsFile;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.files.TLLogFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class Evaluator {

    public static void main(String[] args) throws Throwable {
        CommonTextToolsConfig.INITIAL_LOAD_ENABLED = false;

        var plans = providePlans();

        executePlans(plans);
    }

    private static List<EvalPlan> providePlans() {
        var plans = new ArrayList<EvalPlan>();

        var base2Plan = new EvalPlan("base", "2").with("levenshtein_Enabled", "true").with("jaroWinkler_Enabled", "true");

        for (int b = 1; b <= 2; b++) {
            for (int t = 10; t <= 90; t += 5) {
                var threshold = ((double) t) / 100.0;

                // JARO WINKLER
                if (b == 2) { continue; }
                var group = "jarowinkler";
                var plan = new EvalPlan(group, t)
                        .with("jaroWinkler_Enabled", "true")
                        .with("jaroWinkler_SimilarityThreshold", String.valueOf(threshold));

                // BIGRAM
                //                var group = String.format("bigram_b%s", b);
                //                var threshold = ((double) t) / 100.0;
                //                var plan = new EvalPlan(group, t)
                //                        .with("ngram_Enabled", "true")
                //                        .with("ngram_SimilarityThreshold", String.valueOf(threshold));

                // TRIGRAM
                //                var group = String.format("trigram_b%s", b);
                //                var threshold = ((double) t) / 100.0;
                //                var plan = new EvalPlan(group, t)
                //                        .with("ngram_Enabled", "true")
                //                        .with("ngram_NgramLength", "3")
                //                        .with("ngram_SimilarityThreshold", String.valueOf(threshold));

                // SEWordSimDB
                //                var group = String.format("sewsim_b%s", b);
                //                var threshold = ((double) t) / 100.0;
                //                var plan = new EvalPlan(group, t)
                //                        .with("sewordsim_Enabled", "true")
                //                        .with("sewordsim_SimilarityThreshold", String.valueOf(threshold));

                //                // FASTTEXT
                //                if (t < 40) { continue; } // memory limit
                //                var model = "crawl-300d-2M-subword";
                //                var group = String.format("fastText_b%s_%s", b, model);
                //                var threshold = ((double) t) / 100.0;
                //                var plan = new EvalPlan(group, t)
                //                        .with("fastText_Enabled", "true")
                //                        .with("fastText_SimilarityThreshold", String.valueOf(threshold))
                //                        .with("fastText_ModelPath", model + ".bin");

                //                // FASTTEXT
                //                if (t < 40) { continue; } // memory limit
                //                var model = "cc.en.300";
                //                var group = String.format("fastText_b%s_%s", b, model);
                //                var threshold = ((double) t) / 100.0;
                //                var plan = new EvalPlan(group, t)
                //                        .with("fastText_Enabled", "true")
                //                        .with("fastText_SimilarityThreshold", String.valueOf(threshold))
                //                        .with("fastText_ModelPath", model + ".bin");

                // FASTTEXT
                //              if (t < 40) { continue; } // memory limit
                //              var model = "wiki-news-300d-1M-subword";
                //               var group = String.format("fastText_b%s_%s", b, model);
                //              var threshold = ((double) t) / 100.0;
                //              var plan = new EvalPlan(group, t)
                //                      .with("fastText_Enabled", "true")
                //                      .with("fastText_SimilarityThreshold", String.valueOf(threshold))
                //                      .with("fastText_ModelPath", model + ".bin");

                // WORDNET - LEACOCK & CHODOROW
                //             var group = String.format("wordNet_LC_b%s", b);
                //             var plan = new EvalPlan(group, t)
                //                     .with("wordNet_Enabled", "true")
                //                     .with("wordNet_Algo_LeacockChodorow_Enabled", "true")
                //                     .with("wordNet_Algo_LeacockChodorow_Threshold", String.valueOf(threshold));

                // WORDNET - WU & PALMER
                //var group = String.format("wordNet_WP_b%s", b);
                //var plan = new EvalPlan(group, t).with("wordNet_Enabled", "true")
                //        .with("wordNet_Algo_WuPalmer_Enabled", "true")
                //        .with("wordNet_Algo_WuPalmer_Threshold", String.valueOf(threshold));

                // WORDNET - JIANG & CONRATH
                //              var group = String.format("wordNet_JC_b%s", b);
                //              var plan = new EvalPlan(group, t)
                //                      .with("wordNet_Enabled", "true")
                //                      .with("wordNet_Algo_JiangConrath_Enabled", "true")
                //                      .with("wordNet_Algo_JiangConrath_Threshold", String.valueOf(threshold));

                // WORDNET - ExtenedLesk
                //              var group = String.format("wordNet_Lesk_b%s", b);
                //              var plan = new EvalPlan(group, t)
                //                      .with("wordNet_Enabled", "true")
                //                      .with("wordNet_Algo_ExtendedLesk_Enabled", "true")
                //                      .with("wordNet_Algo_ExtendedLesk_Threshold", String.valueOf(threshold));

                // WORDNET - Ezzikouri
                //              var group = String.format("wordNet_EZ_b%s", b);
                //              var plan = new EvalPlan(group, t)
                //                      .with("wordNet_Enabled", "true")
                //                      .with("wordNet_Algo_Ezzikouri_Enabled", "true")
                //                      .with("wordNet_Algo_Ezzikouri_Threshold", String.valueOf(threshold));

                if (b == 2) {
                    plan.with(base2Plan);
                }

                plans.add(plan);
            }
        }

        return plans;
    }

    private static void executePlans(List<EvalPlan> plans) throws InterruptedException, IOException {
        Path logFile = Path.of("src/test/resources/testout").resolve("tl_eval").resolve("log.md");
        Path binFile = Path.of("src/test/resources/testout").resolve("tl_eval").resolve("results.bin");

        var builder = new ProcessBuilder().command(
                        "mvn.cmd", "test",
                        "-Dtest=\"TracelinksIT\"", "-DfailIfNoTests=false", "-Dorg.bytedeco.javacpp.maxBytes=0"
                )
                .directory(Path.of("C:\\dev\\uni\\ardoco").toFile())
                .inheritIO();
                //.redirectError(ProcessBuilder.Redirect.INHERIT);

        var startTime = System.currentTimeMillis();

        for (EvalPlan plan : plans) {
            prepare(plan);

            System.out.print("Evaluating: " + plan.getId());
            var start = System.currentTimeMillis();

            var process = builder.start();

            process.waitFor(120, TimeUnit.SECONDS);

            process.destroy();
            process.destroyForcibly();

            var duration = System.currentTimeMillis() - start;
            System.out.println(" (" + (duration / 1000) + "s) ✔");

            Thread.sleep(1000);

            TLLogFile.prefixLastLine(logFile, plan.getId());

            reportResult(plan, TLBinResultsFile.read(binFile));

            Thread.sleep(1000);
        }

        var duration = System.currentTimeMillis() - startTime;
        System.out.println("\n\nEntire evaluation process took " + (duration /  1000) + " seconds!\n\n");
    }

    private static void prepare(EvalPlan plan) throws IOException {
        Path targetFile = Path.of("C:\\dev\\uni\\ardoco\\util\\src\\main\\resources\\configs\\CommonTextToolsConfig.properties");
        Path sourceFile = targetFile.getParent().resolve("all_disabled.properties");

        var properties = new Properties();

        try (var in = Files.newInputStream(sourceFile)) {
            properties.load(in);
        }

        var planProps = plan.getProperties();

        for (Object key : planProps.keySet()) {
            properties.setProperty(key.toString(), planProps.getProperty(key.toString()));
        }

        try (var out = Files.newOutputStream(targetFile)) {
            properties.store(out, "");
        }
    }

    private static void reportResult(EvalPlan plan, Map<Project, EvaluationResults> resultMap) throws IOException {
        var resultDir = Path.of("eval_results");
        Files.createDirectories(resultDir);

        var precFile = resultDir.resolve(plan.getGroup() + "_precision.dat");
        var recallFile = resultDir.resolve(plan.getGroup() + "_recall.dat");
        var f1File = resultDir.resolve(plan.getGroup() + "_f1.dat");

        if (!Files.exists(precFile)) { Files.writeString(precFile, "x y\n", CREATE, APPEND); }
        if (!Files.exists(recallFile)) { Files.writeString(recallFile, "x y\n", CREATE, APPEND); }
        if (!Files.exists(f1File)) { Files.writeString(f1File, "x y\n", CREATE, APPEND); }

        double avgPrec = resultMap.values().stream().mapToDouble(EvaluationResults::getPrecision).average().getAsDouble();
        double avgRecall = resultMap.values().stream().mapToDouble(EvaluationResults::getRecall).average().getAsDouble();
        double avgF1 = resultMap.values().stream().mapToDouble(EvaluationResults::getF1).average().getAsDouble();

        int precision = (int) (avgPrec * 100);
        int recall = (int) (avgRecall * 100);
        int f1 = (int) (avgF1 * 100);

        Files.writeString(precFile, plan.getX() + " " + precision + "\n", CREATE, APPEND);
        Files.writeString(recallFile, plan.getX() + " " + recall + "\n", CREATE, APPEND);
        Files.writeString(f1File, plan.getX() + " " + f1 + "\n", CREATE, APPEND);
    }

}
