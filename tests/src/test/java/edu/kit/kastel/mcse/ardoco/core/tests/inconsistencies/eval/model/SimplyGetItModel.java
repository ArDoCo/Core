/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.model;

import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.types.MissingModelInstanceInconsistency;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.AbstractEvalStrategy;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.EvaluationResult;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.GoldStandard;

import java.io.PrintStream;
import java.util.Map;

public class SimplyGetItModel extends AbstractEvalStrategy {

	@Override
	public EvaluationResult evaluate(Project p, IModelConnector originalModel, IText originalText, GoldStandard gs, PrintStream os) {
		var originalData = new DataStructure(originalText, Map.of(originalModel.getModelId(), runModelExtractor(originalModel, Map.of())));
		runTextExtractor(originalData, Map.of());
		var original = runRecommendationConnectionInconsistency(originalData);

		var inconsistencies = original.getInconsistencyState(originalModel.getModelId()).getInconsistencies();
		var models = inconsistencies.select(MissingModelInstanceInconsistency.class::isInstance).collect(MissingModelInstanceInconsistency.class::cast);

		for (var me : models) {
			os.println(me.getReason());
		}
		return null;
	}

}
