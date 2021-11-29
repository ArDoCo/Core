package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.model;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.common.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.MissingModelInstanceInconsistency;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.AbstractEvalStrategy;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.EvaluationResult;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.GoldStandard;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.InconsistencyHelper;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.PRF1Evaluator;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.mod.IModificationStrategy;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.mod.ModifiedElement;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.mod.model.DeleteOneElementEach;
import edu.kit.kastel.mcse.ardoco.core.text.IText;

public class DeleteOneModelElementEval extends AbstractEvalStrategy {

    public DeleteOneModelElementEval() {
        super();
    }

    @Override
    public EvaluationResult evaluate(Project p, IModelConnector originalModel, IText originalText, GoldStandard gs, PrintStream os) {
        IModificationStrategy strategy = new DeleteOneElementEach(originalModel);
        Map<ModifiedElement<IModelConnector, IModelInstance>, AgentDatastructure> result = process(originalModel, originalText, strategy);

        PRF1Evaluator evaluator = new PRF1Evaluator();

        for (var r : result.entrySet()) {
            this.evaluate(result.get(null).getInconsistencyState(), r, gs, evaluator, os);
        }

        os.println("Overall: " + evaluator.getOverallPRF1());
        return evaluator.getOverallPRF1();
    }

    private static Map<ModifiedElement<IModelConnector, IModelInstance>, AgentDatastructure> process(IModelConnector pcmModel, IText annotatedText,
            IModificationStrategy strategy) {
        Map<ModifiedElement<IModelConnector, IModelInstance>, AgentDatastructure> results = new HashMap<>();

        var originalData = new AgentDatastructure(annotatedText, null, runModelExtractor(pcmModel), null, null, null);
        runTextExtractor(originalData);
        var original = runRecommendationConnectionInconsistency(originalData);
        results.put(null, original);

        var iter = strategy.getModifiedModelInstances();
        while (iter.hasNext()) {
            var modification = iter.next();
            var model = modification.getArtifact();
            var data = new AgentDatastructure(annotatedText, null, runModelExtractor(model), null, null, null);
            runTextExtractor(data);
            var result = runRecommendationConnectionInconsistency(data);
            results.put(modification, result);
        }

        return results;
    }

    private void evaluate(IInconsistencyState originalState, Entry<ModifiedElement<IModelConnector, IModelInstance>, AgentDatastructure> r, GoldStandard gs,
            PRF1Evaluator evaluator, PrintStream os) {

        if (r.getKey() == null) {
            // Skip Original ..
            return;
        }

        os.println("-----------------------------------");
        IModelInstance deletedElement = r.getKey().getElement();
        os.println("DEL " + deletedElement);

        var inconsistencyDiff = InconsistencyHelper.getDiff(originalState, r.getValue().getInconsistencyState());
        ImmutableList<Integer> sentencesAnnotatedWithElement = gs.getSentencesWithElement(deletedElement).toSortedList().toImmutable();

        var newInconsistencies = inconsistencyDiff.getNewInconsistencies();
        var newImportantInconsistencies = newInconsistencies //
                .select(MissingModelInstanceInconsistency.class::isInstance) //
                .collect(MissingModelInstanceInconsistency.class::cast);

        if (newInconsistencies.size() >= 2) {
            os.println("----- IMPORTANT CASE --> Manual Check ------");
        }

        os.println("Stats: New: " + newInconsistencies.size() + ", Important (New): " + newImportantInconsistencies.size());

        var foundSentencesWithDuplicatesOverInconsistencies = newImportantInconsistencies.flatCollect(this::foundSentences).toSortedList();

        os.println("Instances: " + String.join(", ", newImportantInconsistencies.collect(i -> i.getTextualInstance().getName())));
        os.println("Is   : " + foundSentencesWithDuplicatesOverInconsistencies);
        os.println("Shall: " + sentencesAnnotatedWithElement);

        var tp = foundSentencesWithDuplicatesOverInconsistencies.select(sentencesAnnotatedWithElement::contains);
        var fp = foundSentencesWithDuplicatesOverInconsistencies.select(i -> !sentencesAnnotatedWithElement.contains(i));
        var fn = sentencesAnnotatedWithElement.select(i -> !foundSentencesWithDuplicatesOverInconsistencies.contains(i));

        EvaluationResult eval = evaluator.nextEvaluation(tp.size(), fp.size(), fn.size());

        os.println(eval);
        os.println("-----------------------------------");
    }

    private ImmutableList<Integer> foundSentences(MissingModelInstanceInconsistency newImportantInconsistency) {
        MutableList<Integer> sentences = Lists.mutable.empty();
        for (var nouns : newImportantInconsistency.getTextualInstance().getNameMappings()) {
            sentences.addAll(nouns.getMappingSentenceNo().castToCollection());
        }
        return sentences.toSet().toList().toImmutable();
    }

}
