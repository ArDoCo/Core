package edu.kit.kastel.mcse.ardoco.erid.tests.integration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.AnonymousRunner;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.core.models.ModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.SadSamTraceabilityLinkRecoveryEvaluation;
import edu.kit.kastel.mcse.ardoco.core.text.providers.TextPreprocessingAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.DiagramBackedTextStateStrategy;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextStateImpl;
import edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.DiagramConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.DiagramInconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.DiagramRecognitionMock;
import edu.kit.kastel.mcse.ardoco.tests.eval.GoldStandardDiagramsWithTLR;

public class SadSamTraceabilityLinkRecoveryEvaluationERID extends SadSamTraceabilityLinkRecoveryEvaluation<GoldStandardDiagramsWithTLR> {
    @Override
    protected ArDoCoResult runTraceLinkEvaluation(GoldStandardDiagramsWithTLR project) {
        return super.runTraceLinkEvaluation(project);
    }

    @Override
    protected ArDoCoRunner getAndSetupRunner(GoldStandardDiagramsWithTLR project) {
        var additionalConfigs = ConfigurationHelper.loadAdditionalConfigs(project.getAdditionalConfigurationsFile());

        String name = project.getProjectName().toLowerCase();
        File inputModelArchitecture = project.getModelFile();
        File inputText = project.getTextFile();
        File outputDir = new File(TraceLinkEvaluationERID.OUTPUT);

        var runner = new AnonymousRunner(name) {
            @Override
            public List<AbstractPipelineStep> initializePipelineSteps(DataRepository dataRepository) throws IOException {
                var pipelineSteps = new ArrayList<AbstractPipelineStep>();

                var text = CommonUtilities.readInputText(inputText);
                if (text.isBlank()) {
                    throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
                }
                DataRepositoryHelper.putInputText(dataRepository, text);
                pipelineSteps.add(TextPreprocessingAgent.get(additionalConfigs, dataRepository));

                pipelineSteps.add(ModelProviderAgent.get(inputModelArchitecture, ArchitectureModelType.PCM, dataRepository));
                pipelineSteps.add(new DiagramRecognitionMock(project, additionalConfigs, dataRepository));

                var textState = new TextStateImpl();
                var textStrategy = new DiagramBackedTextStateStrategy(textState, dataRepository);
                textState.setTextStateStrategy(textStrategy);
                dataRepository.addData(TextState.ID, textState);
                pipelineSteps.add(TextExtraction.get(additionalConfigs, dataRepository));

                pipelineSteps.add(RecommendationGenerator.get(additionalConfigs, dataRepository));
                pipelineSteps.add(new DiagramConnectionGenerator(additionalConfigs, dataRepository));
                pipelineSteps.add(new DiagramInconsistencyChecker(additionalConfigs, dataRepository));
                pipelineSteps.add(ConnectionGenerator.get(additionalConfigs, dataRepository));

                return pipelineSteps;
            }
        };
        runner.setOutputDirectory(outputDir);
        return runner;
    }
}
