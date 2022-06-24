/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.model;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.IInconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.types.MissingModelInstanceInconsistency;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.baseline.SimpleMissingModelInstanceInconsistency;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.AbstractEvalStrategy;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.EvaluationResult;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.GoldStandard;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.PRF1Evaluator;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.mod.IModificationStrategy;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.mod.ModifiedElement;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.mod.model.DeleteOneElementEach;

public class DeleteOneModelElementEval extends AbstractEvalStrategy {

    public DeleteOneModelElementEval() {
        // empty
    }

    @Override
    public EvaluationResult evaluate(Project project, IModelConnector originalModel, IText originalText, GoldStandard gs, PrintStream os) {
        IModificationStrategy strategy = new DeleteOneElementEach(originalModel);
        var result = process(originalModel, originalText, strategy);

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

    private Map<ModifiedElement<IModelConnector, IModelInstance>, DataStructure> process(IModelConnector pcmModel, IText annotatedText,
            IModificationStrategy strategy) {
        var configurations = new HashMap<String, String>();
        Map<ModifiedElement<IModelConnector, IModelInstance>, DataStructure> results = new HashMap<>();

        var originalData = new DataStructure(annotatedText, Map.of(pcmModel.getModelId(), runModelExtractor(pcmModel, configurations)));

        runTextExtractor(originalData, configurations);
        var original = runRecommendationConnectionInconsistency(originalData);
        results.put(null, original);

        var iter = strategy.getModifiedModelInstances();
        while (iter.hasNext()) {
            var modification = iter.next();
            var model = modification.getArtifact();
            var data = new DataStructure(annotatedText, Map.of(model.getModelId(), runModelExtractor(model, configurations)));
            runTextExtractor(data, configurations);
            var result = runRecommendationConnectionInconsistency(data);
            results.put(modification, result);
        }

        return results;
    }

    private void evaluate(Entry<ModifiedElement<IModelConnector, IModelInstance>, DataStructure> r, String modelId, GoldStandard gs, PRF1Evaluator evaluator,
            PrintStream os) {
        os.println("-----------------------------------");

        if (r.getKey() == null) {
            // For original, just put put the number of false positives (assuming original
            // has no missing instances)
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

    private ImmutableList<Integer> foundSentences(IInconsistency inconsistency) {
        if (inconsistency instanceof SimpleMissingModelInstanceInconsistency simpleMissingModelInstanceInconsistency) {
            return Lists.immutable.of(simpleMissingModelInstanceInconsistency.sentenceNo());
        } else if (inconsistency instanceof MissingModelInstanceInconsistency missingModelInstanceInconsistency) {
            return Lists.immutable.of(missingModelInstanceInconsistency.sentence());
        }
        return Lists.immutable.empty();
    }

}
