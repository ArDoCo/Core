/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.baseline;

import java.util.Map;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.model.DeleteOneModelElementEval;

public class DeleteOneModelElementBaselineEval extends DeleteOneModelElementEval {

    @Override
    protected void runInconsistencyChecker(DataRepository dataRepository, Map<String, String> configs) {
        var inconsistencyChecker = new InconsistencyBaseline(dataRepository);
        inconsistencyChecker.applyConfiguration(configs);
        inconsistencyChecker.run();
    }

}
