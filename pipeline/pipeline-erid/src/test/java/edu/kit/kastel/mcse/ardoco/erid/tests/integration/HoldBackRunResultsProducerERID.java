package edu.kit.kastel.mcse.ardoco.erid.tests.integration;

import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.InputDiagramData;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.models.informants.ModelProviderInformant;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.inconsistencyhelper.HoldBackRunResultsProducer;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.inconsistencyhelper.HoldElementsBackModelConnector;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;
import edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.DiagramConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.DiagramInconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.DiagramRecognitionMock;
import edu.kit.kastel.mcse.ardoco.lissa.DiagramRecognition;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;

public class HoldBackRunResultsProducerERID extends HoldBackRunResultsProducer {
    boolean useDiagramRecognitionMock;

    public HoldBackRunResultsProducerERID(boolean useDiagramRecognitionMock) {
        this.useDiagramRecognitionMock = useDiagramRecognitionMock;
    }

    @Override
    protected void addMiddleSteps(GoldStandardProject goldStandardProject, HoldElementsBackModelConnector holdElementsBackModelConnector, ArDoCo arDoCo,
            DataRepository dataRepository, SortedMap<String, String> additionalConfigs) {
        var diagramProject = DiagramProject.getFromName(goldStandardProject.getProjectName()).orElseThrow();
        //arDoCo.addPipelineStep(TextExtraction.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(new ModelProviderInformant(dataRepository, holdElementsBackModelConnector));
        if (useDiagramRecognitionMock) {
            arDoCo.addPipelineStep(new DiagramRecognitionMock(diagramProject, additionalConfigs, dataRepository));
        } else {
            dataRepository.addData(InputDiagramData.ID, new InputDiagramData(diagramProject.getDiagramData()));
            arDoCo.addPipelineStep(DiagramRecognition.get(diagramProject.getAdditionalConfigurations(), dataRepository));
        }
        //arDoCo.addPipelineStep(TextExtraction.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(RecommendationGenerator.get(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(new DiagramConnectionGenerator(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(new DiagramInconsistencyChecker(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(ConnectionGenerator.get(additionalConfigs, dataRepository));
    }
}
