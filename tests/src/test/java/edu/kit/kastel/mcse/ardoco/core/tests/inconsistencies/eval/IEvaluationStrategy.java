/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval;


import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
import java.io.PrintStream;

public interface IEvaluationStrategy {
    EvaluationResult evaluate(Project p, IModelConnector originalModel, IText originalText, GoldStandard gs, PrintStream os);
}
