package edu.kit.kastel.mcse.ardoco.core.execution;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ParameterizedRunner;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.models.ModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.text.providers.TextPreprocessingAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.DiagramBackedTextStateStrategy;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextStateImpl;
import edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.DiagramConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.DiagramInconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognitionmock.DiagramRecognitionMock;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;

public class ArDoCoForERID extends ParameterizedRunner<ArDoCoForERID.Parameters> {
    public record Parameters(DiagramProject diagramProject, File inputText, File inputModelArchitecture, ArchitectureModelType inputArchitectureModelType,
                             Map<String, String> additionalConfigs, File outputDir) {
    }

    public ArDoCoForERID(String projectName, Parameters parameters) {
        super(projectName, parameters);
    }

    @Override
    public List<AbstractPipelineStep> initializePipelineSteps(Parameters p) throws IOException {
        var pipelineSteps = new ArrayList<AbstractPipelineStep>();

        ArDoCo arDoCo = getArDoCo();
        var dataRepository = arDoCo.getDataRepository();
        pipelineSteps.add(new DiagramRecognitionMock(p.diagramProject, p.additionalConfigs, dataRepository));

        var text = CommonUtilities.readInputText(p.inputText);
        if (text.isBlank()) {
            throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
        }
        DataRepositoryHelper.putInputText(dataRepository, text);
        pipelineSteps.add(TextPreprocessingAgent.get(p.additionalConfigs, dataRepository));

        pipelineSteps.add(ModelProviderAgent.get(p.inputModelArchitecture, p.inputArchitectureModelType, dataRepository));

        var textState = new TextStateImpl();
        var textStrategy = new DiagramBackedTextStateStrategy(textState, dataRepository);
        textState.setTextStateStrategy(textStrategy);
        dataRepository.addData(TextState.ID, textState);
        pipelineSteps.add(TextExtraction.get(p.additionalConfigs, dataRepository));

        pipelineSteps.add(RecommendationGenerator.get(p.additionalConfigs, dataRepository));
        pipelineSteps.add(new DiagramConnectionGenerator(p.additionalConfigs, dataRepository));
        pipelineSteps.add(new DiagramInconsistencyChecker(p.additionalConfigs, dataRepository));
        pipelineSteps.add(ConnectionGenerator.get(p.additionalConfigs, dataRepository));
        pipelineSteps.add(InconsistencyChecker.get(p.additionalConfigs, dataRepository));

        return pipelineSteps;
    }
}
