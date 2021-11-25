package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.text;

import java.io.PrintStream;

import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.AbstractEvalStrategy;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.EvaluationResult;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.GoldStandard;
import edu.kit.kastel.mcse.ardoco.core.text.IText;

public class AppendSomeOtherText extends AbstractEvalStrategy {

    @Override
    public EvaluationResult evaluate(Project project, IModelConnector originalModel, IText originalText, GoldStandard gs, PrintStream os) {
        var otherProjects = Lists.mutable.with(Project.values()).select(p -> p != project);

        for (var otherProject : otherProjects) {
            appendOtherProjectText(project, otherProject, originalModel, gs);
        }
        return null;
    }

    private void appendOtherProjectText(Project project, Project otherProject, IModelConnector originalModel, GoldStandard originalGS) {
        // not yet done
    }

}
