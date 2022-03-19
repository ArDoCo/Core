/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.text;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.types.NameInconsistency;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.AbstractEvalStrategy;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.EvaluationResult;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.GoldStandard;

public class SimplyGetItNaming extends AbstractEvalStrategy {

    @Override
    public EvaluationResult evaluate(Project p, IModelConnector originalModel, IText originalText, GoldStandard gs, PrintStream os) {
        var modelState = runModelExtractor(originalModel);
        var originalData = new DataStructure(originalText, Map.of(originalModel.getModelId(), modelState));
        var configs = new HashMap<String, String>();
        runTextExtractor(originalData, configs);
        var original = runRecommendationConnectionInconsistency(originalData);

        var inconsistencies = original.getInconsistencyState(originalModel.getModelId()).getInconsistencies();
        var namings = inconsistencies.select(NameInconsistency.class::isInstance).collect(NameInconsistency.class::cast);

        for (var naming : namings) {
            os.println(naming.getReason());
        }

        return null;
    }

}
