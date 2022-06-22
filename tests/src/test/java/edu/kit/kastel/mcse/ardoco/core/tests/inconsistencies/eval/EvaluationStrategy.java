/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval;

import java.io.PrintStream;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelConnector;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Text;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;

public interface EvaluationStrategy {
    EvaluationResult evaluate(Project p, ModelConnector originalModel, Text originalText, GoldStandard gs, PrintStream os);
}
