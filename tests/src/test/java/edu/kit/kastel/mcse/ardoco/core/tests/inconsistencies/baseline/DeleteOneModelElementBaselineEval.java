/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.baseline;

import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.api.stage.IExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.model.DeleteOneModelElementEval;

public class DeleteOneModelElementBaselineEval extends DeleteOneModelElementEval {

    @Override
    protected DataStructure runInconsistencyChecker(DataStructure data, Map<String, String> configs) {
        IExecutionStage inconsistencyChecker = new InconsistencyBaseline();
        inconsistencyChecker.execute(data, configs);
        return data;
    }

}
