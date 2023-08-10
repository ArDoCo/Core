package edu.kit.kastel.mcse.ardoco.erid.tests.integration;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject;

import java.io.File;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.models.informants.ModelProviderInformant;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.inconsistencyhelper.HoldBackRunResultsProducer;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.inconsistencyhelper.HoldElementsBackModelConnector;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;
import edu.kit.kastel.mcse.ardoco.erid.api.diagramrecognitionmock.InputDiagramDataMock;
import edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.DiagramConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.DiagramInconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognitionmock.DiagramRecognitionMock;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;

public class HoldBackRunResultsProducerERID extends HoldBackRunResultsProducer {
    @Override
    protected void addMiddleSteps(HoldElementsBackModelConnector holdElementsBackModelConnector, ArDoCo arDoCo, DataRepository dataRepository,
            Map<String, String> additionalConfigs) {
        arDoCo.addPipelineStep(new DiagramRecognitionMock(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(new ModelProviderInformant(dataRepository, holdElementsBackModelConnector));
        arDoCo.addPipelineStep(TextExtraction.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(RecommendationGenerator.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(new DiagramConnectionGenerator(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(new DiagramInconsistencyChecker(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(ConnectionGenerator.get(additionalConfigs, dataRepository));
    }

    @Override
    protected void addPreSteps(ArDoCoResult precomputedResults, DataRepository dataRepository) {
        super.addPreSteps(precomputedResults, dataRepository);
        dataRepository.addData(InputDiagramDataMock.ID,
                new InputDiagramDataMock(DiagramProject.getFromName(precomputedResults.getProjectName()).orElseThrow()));
    }

    @Override
    protected void addPreSteps(File inputText, GoldStandardProject goldStandardProject, ArDoCo arDoCo, DataRepository dataRepository, Map<String, String> additionalConfigs) {
        super.addPreSteps(inputText, goldStandardProject, arDoCo, dataRepository, additionalConfigs);
        dataRepository.addData(InputDiagramDataMock.ID, new InputDiagramDataMock(DiagramProject.getFromName(goldStandardProject.getProjectName()).orElseThrow()));
    }
}
