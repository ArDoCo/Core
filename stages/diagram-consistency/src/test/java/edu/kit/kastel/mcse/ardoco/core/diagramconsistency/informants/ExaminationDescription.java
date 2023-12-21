/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.informants;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.DiagramProject;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.Refactoring;

record ExaminationDescription(DiagramProject project, GeneralModelType generalModelType, int refactoringIndex, double epsilon, int maxAlgorithmIterations,
                              double textSimilarityThreshold, Refactoring<Box, ArchitectureItem> archPreRefactoring,
                              Refactoring<Box, CodeItem> codePreRefactoring) {
    ExaminationDescription withProject(DiagramProject project) {
        return new ExaminationDescription(project, this.generalModelType, this.refactoringIndex, this.epsilon, this.maxAlgorithmIterations,
                this.textSimilarityThreshold, this.archPreRefactoring, this.codePreRefactoring);
    }
}
