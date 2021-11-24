package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.text;

import java.io.PrintStream;

import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.tests.Projects;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.AbstractEvalStrategy;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.EvaluationResult;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.GoldStandard;
import edu.kit.kastel.mcse.ardoco.core.text.IText;

public class AppendSomeOtherText extends AbstractEvalStrategy {

    @Override
    public EvaluationResult evaluate(Projects project, IModelConnector originalModel, IText originalText, GoldStandard gs, PrintStream os) {
        var otherProjects = Lists.mutable.with(Projects.values()).select(p -> p != project);

        for (var otherProject : otherProjects) {
            appendOtherProjectText(project, otherProject, originalModel, gs);
        }
        return null;
    }

    private void appendOtherProjectText(Projects project, Projects otherProject, IModelConnector originalModel, GoldStandard originalGS) {
        // not yet done
    }

}
