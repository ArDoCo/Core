package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval;

import java.io.PrintStream;

import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.text.IText;

public interface IEvaluationStrategy {
    EvaluationResult evaluate(Project p, IModelConnector originalModel, IText originalText, GoldStandard gs, PrintStream os);
}
