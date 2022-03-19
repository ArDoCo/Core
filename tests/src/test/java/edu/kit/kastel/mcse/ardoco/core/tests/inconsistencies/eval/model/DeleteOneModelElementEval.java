/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.model;

import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.types.MissingModelInstanceInconsistency;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.AbstractEvalStrategy;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.EvaluationResult;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.GoldStandard;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.PRF1Evaluator;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.mod.IModificationStrategy;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.mod.ModifiedElement;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.mod.model.DeleteOneElementEach;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class DeleteOneModelElementEval extends AbstractEvalStrategy {

	public DeleteOneModelElementEval() {
	}

	@Override
	public EvaluationResult evaluate(Project project, IModelConnector originalModel, IText originalText, GoldStandard gs, PrintStream os) {
		IModificationStrategy strategy = new DeleteOneElementEach(originalModel);
		var result = process(project, originalModel, originalText, strategy);

		var evaluator = new PRF1Evaluator();

		var modelId = originalModel.getModelId();
		var originalInconsistencyState = result.get(null).getInconsistencyState(modelId);

		for (var r : result.entrySet()) {
			this.evaluate(originalInconsistencyState, r, modelId, gs, evaluator, os);
		}

		os.println("Overall: ");
		os.println("Weighted: " + evaluator.getWeightedAveragePRF1());
		os.println("Average:  " + evaluator.getAveragePRF1());
		return evaluator.getWeightedAveragePRF1();
	}

	private static Map<ModifiedElement<IModelConnector, IModelInstance>, DataStructure> process(Project project, IModelConnector pcmModel, IText annotatedText, IModificationStrategy strategy) {
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

	private void evaluate(IInconsistencyState originalState, Entry<ModifiedElement<IModelConnector, IModelInstance>, DataStructure> r, String modelId, GoldStandard gs, PRF1Evaluator evaluator,
			PrintStream os) {
		os.println("-----------------------------------");

		if (r.getKey() == null) {
			// For original, just put put the number of false positives (assuming original
			// has no missing instances)
			var inconsistencySentences = r.getValue().getInconsistencyState(modelId).getInconsistencies().select(MissingModelInstanceInconsistency.class::isInstance)
					.collect(MissingModelInstanceInconsistency.class::cast).flatCollect(this::foundSentences).toSet();
			var outputString = "ORIGINAL: Number of False Positives (assuming consistency for original): " + inconsistencySentences.size();
			os.println(outputString);
			return;
		}

		var deletedElement = r.getKey().getElement();
		os.println("DEL " + deletedElement);

		var sentencesAnnotatedWithElement = gs.getSentencesWithElement(deletedElement).toSortedSet().toImmutable();

		var newInconsistencies = r.getValue().getInconsistencyState(modelId).getInconsistencies();
		var newMissingModelInstanceInconsistencies = newInconsistencies //
				.select(MissingModelInstanceInconsistency.class::isInstance) //
				.collect(MissingModelInstanceInconsistency.class::cast);

		os.println("Stats: New: " + newInconsistencies.size() + ", New MissingModelInstanceInconsistencies: " + newMissingModelInstanceInconsistencies.size());

		var foundSentencesWithDuplicatesOverInconsistencies = newMissingModelInstanceInconsistencies.flatCollect(this::foundSentences).toSortedSet();

		os.println("Is   : " + foundSentencesWithDuplicatesOverInconsistencies);
		os.println("Shall: " + sentencesAnnotatedWithElement);

		var tp = foundSentencesWithDuplicatesOverInconsistencies.select(sentencesAnnotatedWithElement::contains);
		var fp = foundSentencesWithDuplicatesOverInconsistencies.select(i -> !sentencesAnnotatedWithElement.contains(i));
		var fn = sentencesAnnotatedWithElement.select(i -> !foundSentencesWithDuplicatesOverInconsistencies.contains(i));

		var result = evaluator.nextEvaluation(tp.size(), fp.size(), fn.size());

		os.println(result);
		os.println("-----------------------------------");
	}

	private ImmutableList<Integer> foundSentences(MissingModelInstanceInconsistency newImportantInconsistency) {
		MutableList<Integer> sentences = Lists.mutable.empty();
		for (var nouns : newImportantInconsistency.getTextualInstance().getNameMappings()) {
			sentences.addAll(nouns.getMappingSentenceNo().castToCollection());
		}
		return sentences.distinct().toImmutable();
	}

}
