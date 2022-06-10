/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.text;

import java.io.PrintStream;

import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.AbstractEvalStrategy;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.EvaluationResult;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.GoldStandard;

public class AppendSomeOtherText extends AbstractEvalStrategy {

    @Override
    public EvaluationResult evaluate(Project project, IModelConnector originalModel, IText originalText, GoldStandard gs, PrintStream os,
            boolean withDiagrams) {
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
