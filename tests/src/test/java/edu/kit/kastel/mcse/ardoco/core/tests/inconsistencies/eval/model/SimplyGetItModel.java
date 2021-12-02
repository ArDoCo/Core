/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.model;

import java.io.PrintStream;

import edu.kit.kastel.mcse.ardoco.core.common.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.MissingModelInstanceInconsistency;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.AbstractEvalStrategy;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.EvaluationResult;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.GoldStandard;
import edu.kit.kastel.mcse.ardoco.core.text.IText;

public class SimplyGetItModel extends AbstractEvalStrategy {

    @Override
    public EvaluationResult evaluate(Project p, IModelConnector originalModel, IText originalText, GoldStandard gs, PrintStream os) {
        var originalData = new AgentDatastructure(originalText, null, runModelExtractor(originalModel), null, null, null);
        runTextExtractor(originalData, getTextExtractionConfigurations(p));
        var original = runRecommendationConnectionInconsistency(originalData);

        var inconsistencies = original.getInconsistencyState().getInconsistencies();
        var models = inconsistencies.select(MissingModelInstanceInconsistency.class::isInstance).collect(MissingModelInstanceInconsistency.class::cast);

        for (var me : models) {
            os.println(me.getReason());
        }
        return null;
    }

}
