/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.informants;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.DiagramProject;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.Refactoring;

class ExaminationDescriptionBuilder {
    private DiagramProject project;
    private GeneralModelType generalModelType;
    private int refactoringIndex = -1;
    private double epsilon = DiagramModelLinkInformant.DEFAULT_EPSILON;
    private int maxAlgorithmIterations = DiagramModelLinkInformant.DEFAULT_MAX_ITERATIONS;
    private double textSimilarityThreshold = DiagramModelLinkInformant.DEFAULT_TEXT_SIMILARITY_THRESHOLD;
    private Refactoring<Box, ArchitectureItem> archPreRefactoring = null;
    private Refactoring<Box, CodeItem> codePreRefactoring = null;

    ExaminationDescriptionBuilder setProject(DiagramProject project) {
        this.project = project;
        return this;
    }

    ExaminationDescriptionBuilder setGeneralModelType(GeneralModelType generalModelType) {
        this.generalModelType = generalModelType;
        return this;
    }

    ExaminationDescriptionBuilder setRefactoringIndex(int refactoringIndex) {
        this.refactoringIndex = refactoringIndex;
        return this;
    }

    ExaminationDescriptionBuilder setEpsilon(double epsilon) {
        this.epsilon = epsilon;
        return this;
    }

    ExaminationDescriptionBuilder setMaxAlgorithmIterations(int maxAlgorithmIterations) {
        this.maxAlgorithmIterations = maxAlgorithmIterations;
        return this;
    }

    ExaminationDescriptionBuilder setTextSimilarityThreshold(double textSimilarityThreshold) {
        this.textSimilarityThreshold = textSimilarityThreshold;
        return this;
    }

    ExaminationDescriptionBuilder setArchPreRefactoring(Refactoring<Box, ArchitectureItem> archPreRefactoring) {
        this.archPreRefactoring = archPreRefactoring;
        return this;
    }

    ExaminationDescriptionBuilder setCodePreRefactoring(Refactoring<Box, CodeItem> codePreRefactoring) {
        this.codePreRefactoring = codePreRefactoring;
        return this;
    }

    ExaminationDescription createEvaluationDescription() {
        return new ExaminationDescription(this.project, this.generalModelType, this.refactoringIndex, this.epsilon, this.maxAlgorithmIterations,
                this.textSimilarityThreshold, this.archPreRefactoring, this.codePreRefactoring);
    }
}
