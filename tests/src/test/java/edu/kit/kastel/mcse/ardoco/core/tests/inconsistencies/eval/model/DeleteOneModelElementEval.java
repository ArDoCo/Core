/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.model;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.api.data.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelConnector;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Text;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.types.MissingModelInstanceInconsistency;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.AbstractEvalStrategy;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.EvaluationResult;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.GoldStandard;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.PRF1Evaluator;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.mod.ModificationStrategy;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.mod.ModifiedElement;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.mod.model.DeleteOneElementEach;

public class DeleteOneModelElementEval extends AbstractEvalStrategy {
    private static final Logger logger = LoggerFactory.getLogger(DeleteOneModelElementEval.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String OUTPUT = "src/test/resources/testout";

    private static final boolean detailedDebug = true;

    public DeleteOneModelElementEval() {
        // empty
    }

    @Override
    public EvaluationResult evaluate(Project project, ModelConnector originalModel, Text originalText, GoldStandard gs, PrintStream os) {
        var strategy = new DeleteOneElementEach(originalModel);
        var result = process(originalModel, originalText, strategy);

        if (detailedDebug) {
            writeOutResultInfo(project, result);
        }

        var evaluator = new PRF1Evaluator();

        var modelId = originalModel.getModelId();

        for (var r : result.entrySet()) {
            this.evaluate(r, modelId, gs, evaluator, os);
        }

        os.println("Overall: ");
        os.println("Weighted: " + evaluator.getWeightedAveragePRF1());
        os.println("Average:  " + evaluator.getMacroAveragePRF1());
        return evaluator.getWeightedAveragePRF1();
    }

    private void writeOutResultInfo(Project project, Map<ModifiedElement<ModelConnector, ModelInstance>, DataStructure> result) {
        var evalDir = Path.of(OUTPUT).resolve("id_eval");
        Path targetFile;
        try {
            Files.createDirectories(evalDir);
            var filename = "results.md";
            targetFile = evalDir.resolve(filename);
            if (project == Project.MEDIASTORE) {
                Files.writeString(targetFile, "# Inconsistency Detection\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (IOException e) {
            logger.warn(e.getMessage(), e.getCause());
            return;
        }

        var outputBuilder = new StringBuilder(DATE_FORMATTER.format(LocalDateTime.now(ZoneId.systemDefault())));
        outputBuilder.append("\n## ");
        outputBuilder.append(project.name());
        outputBuilder.append("\n");
        // TODO What info is helpful to log to file?
        for (var resultEntry : result.entrySet()) {
            var modifiedElement = resultEntry.getKey();
            if (modifiedElement != null) {
                // only look at the base version, not when an element was deleted
                return;
            }
            var data = resultEntry.getValue();
            for (var modelId : data.getModelIds()) {
                outputBuilder.append("### Model-ID: ").append(modelId).append("\n");

                var text = data.getText();
                var sentences = text.getSentences();
                ImmutableList<Inconsistency> inconsistencies = data.getInconsistencyState(modelId).getInconsistencies();
                var mmInconsistencies = inconsistencies.select(MissingModelInstanceInconsistency.class::isInstance)
                        .collect(MissingModelInstanceInconsistency.class::cast)
                        .toSortedList(Comparator.comparingInt(MissingModelInstanceInconsistency::sentence));
                for (var mmi : mmInconsistencies) {
                    var sentenceNo = mmi.sentence();
                    var sentenceText = sentences.get(sentenceNo - 1).getText();
                    var name = mmi.name();

                    outputBuilder.append("s").append(String.format("%02d", sentenceNo));
                    outputBuilder.append("; name=\"").append(name).append("\"");
                    outputBuilder.append("; sentence=\"").append(sentenceText).append("\"").append("\n");
                }
                outputBuilder.append("\n");
            }

            try {
                Files.writeString(targetFile, outputBuilder.toString(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Map<ModifiedElement<ModelConnector, ModelInstance>, DataStructure> process(ModelConnector pcmModel, Text annotatedText,
            ModificationStrategy strategy) {
        var configurations = new HashMap<String, String>();
        Map<ModifiedElement<ModelConnector, ModelInstance>, DataStructure> results = new HashMap<>();

        DataRepository dataRepository = new DataRepository();
        DataStructure originalData = new DataStructure(dataRepository);
        PreprocessingData preprocessingData = new PreprocessingData(annotatedText);
        dataRepository.addData(PreprocessingData.ID, preprocessingData);

        runModelExtractor(dataRepository, pcmModel, configurations);
        runTextExtractor(dataRepository, configurations);

        var original = runRecommendationConnectionInconsistency(originalData);
        results.put(null, original);

        var modifiedElementIterator = strategy.getModifiedModelInstances();
        while (modifiedElementIterator.hasNext()) {
            var modification = modifiedElementIterator.next();

            DataRepository modDataRepository = new DataRepository();
            modDataRepository.addData(PreprocessingData.ID, preprocessingData);

            runModelExtractor(modDataRepository, pcmModel, configurations);
            runTextExtractor(modDataRepository, configurations);
            var data = new DataStructure(modDataRepository);
            var result = runRecommendationConnectionInconsistency(data);
            results.put(modification, result);
        }

        return results;
    }

    private void evaluate(Entry<ModifiedElement<ModelConnector, ModelInstance>, DataStructure> r, String modelId, GoldStandard gs, PRF1Evaluator evaluator,
            PrintStream os) {
        os.println("-----------------------------------");

        if (r.getKey() == null) {
            // For original, just put the number of false positives (assuming original has no missing instances)
            var inconsistencySentences = r.getValue().getInconsistencyState(modelId).getInconsistencies().flatCollect(this::foundSentences).toSet();
            var outputString = "ORIGINAL: Number of False Positives (assuming consistency for original): " + inconsistencySentences.size();
            os.println(outputString);
            return;
        }

        var deletedElement = r.getKey().getElement();
        os.println("DEL " + deletedElement);

        var sentencesAnnotatedWithElement = gs.getSentencesWithElement(deletedElement).toSortedSet().toImmutable();

        var newInconsistencies = r.getValue().getInconsistencyState(modelId).getInconsistencies();
        var newMissingModelInstanceInconsistencies = newInconsistencies.flatCollect(this::foundSentences).toSet();

        os.println("Stats: New: " + newInconsistencies.size() + ", New MissingModelInstanceInconsistencies: " + newMissingModelInstanceInconsistencies.size());

        var foundSentencesWithDuplicatesOverInconsistencies = newMissingModelInstanceInconsistencies.toSortedSet();

        os.println("Is   : " + foundSentencesWithDuplicatesOverInconsistencies);
        os.println("Shall: " + sentencesAnnotatedWithElement);

        var tp = foundSentencesWithDuplicatesOverInconsistencies.select(sentencesAnnotatedWithElement::contains);
        var fp = foundSentencesWithDuplicatesOverInconsistencies.select(i -> !sentencesAnnotatedWithElement.contains(i));
        var fn = sentencesAnnotatedWithElement.select(i -> !foundSentencesWithDuplicatesOverInconsistencies.contains(i));

        var result = evaluator.nextEvaluation(tp.size(), fp.size(), fn.size());

        os.println(result);
        os.println("-----------------------------------");
    }

    private ImmutableList<Integer> foundSentences(Inconsistency inconsistency) {
        if (inconsistency instanceof MissingModelInstanceInconsistency missingModelInstanceInconsistency) {
            return Lists.immutable.of(missingModelInstanceInconsistency.sentence());
        }
        return Lists.immutable.empty();
    }

}
