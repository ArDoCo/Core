/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.InputDiagramData;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ParameterizedRunner;
import edu.kit.kastel.mcse.ardoco.core.models.agents.ArCoTLModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.text.providers.TextPreprocessingAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.DiagramBackedTextStateStrategy;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextStateImpl;
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.DiagramRecognitionMock;
import edu.kit.kastel.mcse.ardoco.lissa.DiagramRecognition;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;

public class PreTestRunner extends ParameterizedRunner<PreTestRunner.Parameters> {
    public PreTestRunner(String projectName, Parameters parameters) {
        super(projectName, parameters);
    }

    public record Parameters(DiagramProject diagramProject, File outputDir, boolean useMockDiagrams) {
    }

    @Override
    public List<AbstractPipelineStep> initializePipelineSteps(Parameters p) throws IOException {
        var pipelineSteps = new ArrayList<AbstractPipelineStep>();

        ArDoCo arDoCo = getArDoCo();
        var dataRepository = arDoCo.getDataRepository();
        dataRepository.getGlobalConfiguration().getWordSimUtils().setConsiderAbbreviations(true);

        var text = CommonUtilities.readInputText(p.diagramProject.getTextFile());
        if (text.isBlank()) {
            throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an " + "error reading the file.");
        }
        DataRepositoryHelper.putInputText(dataRepository, text);
        pipelineSteps.add(TextPreprocessingAgent.get(p.diagramProject.getAdditionalConfigurations(), dataRepository));

        ArCoTLModelProviderAgent arCoTLModelProviderAgent = ArCoTLModelProviderAgent.get(p.diagramProject.getModelFile(), ArchitectureModelType.PCM, null,
                p.diagramProject.getAdditionalConfigurations(), dataRepository);
        pipelineSteps.add(arCoTLModelProviderAgent);

        if (p.useMockDiagrams) {
            pipelineSteps.add(new DiagramRecognitionMock(p.diagramProject, p.diagramProject.getAdditionalConfigurations(), dataRepository));
        } else {
            dataRepository.addData(InputDiagramData.ID, new InputDiagramData(p.diagramProject.getDiagramData()));
            pipelineSteps.add(DiagramRecognition.get(p.diagramProject.getAdditionalConfigurations(), dataRepository));
        }

        var diagramBackedTextStateStrategy = new DiagramBackedTextStateStrategy(dataRepository);
        var textState = new TextStateImpl(diagramBackedTextStateStrategy);
        dataRepository.addData(TextState.ID, textState);
        pipelineSteps.add(TextExtraction.get(p.diagramProject.getAdditionalConfigurations(), dataRepository));

        pipelineSteps.add(RecommendationGenerator.get(p.diagramProject.getAdditionalConfigurations(), dataRepository));
        return pipelineSteps;
    }
}
