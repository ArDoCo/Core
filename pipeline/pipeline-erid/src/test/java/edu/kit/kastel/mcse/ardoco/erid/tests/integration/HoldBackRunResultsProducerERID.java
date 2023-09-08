package edu.kit.kastel.mcse.ardoco.erid.tests.integration;

import edu.kit.kastel.mcse.ardoco.core.api.InputDiagramData;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.AnonymousRunner;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.models.informants.ModelProviderInformant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.baseline.InconsistencyBaseline;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.inconsistencyhelper.HoldBackRunResultsProducer;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.inconsistencyhelper.HoldElementsBackModelConnector;
import edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.DiagramConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.DiagramInconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.DiagramRecognitionMock;
import edu.kit.kastel.mcse.ardoco.lissa.DiagramRecognition;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;
import java.util.ArrayList;
import java.util.List;

public class HoldBackRunResultsProducerERID extends HoldBackRunResultsProducer {
    boolean useDiagramRecognitionMock;

    public HoldBackRunResultsProducerERID(boolean useDiagramRecognitionMock) {
        this.useDiagramRecognitionMock = useDiagramRecognitionMock;
    }

    @Override
    protected DataRepository runUnshared(GoldStandardProject goldStandardProject, HoldElementsBackModelConnector holdElementsBackModelConnector,
                                         DataRepository preRunDataRepository,
                                         boolean useInconsistencyBaseline) {
        var diagramProject = DiagramProject.getFromName(goldStandardProject.getProjectName()).orElseThrow();
        return new AnonymousRunner(goldStandardProject.getProjectName(), preRunDataRepository) {
            @Override
            public List<AbstractPipelineStep> initializePipelineSteps(DataRepository dataRepository) {
                var pipelineSteps = new ArrayList<AbstractPipelineStep>();

                pipelineSteps.add(new ModelProviderInformant(dataRepository, holdElementsBackModelConnector));
                if (useDiagramRecognitionMock) {
                    pipelineSteps.add(new DiagramRecognitionMock(diagramProject, additionalConfigs, dataRepository));
                } else {
                    dataRepository.addData(InputDiagramData.ID, new InputDiagramData(diagramProject.getDiagramData()));
                    pipelineSteps.add(DiagramRecognition.get(additionalConfigs, dataRepository));
                }
                pipelineSteps.add(RecommendationGenerator.get(additionalConfigs, dataRepository));
                pipelineSteps.add(new DiagramConnectionGenerator(additionalConfigs, dataRepository));
                pipelineSteps.add(new DiagramInconsistencyChecker(additionalConfigs, dataRepository));
                pipelineSteps.add(ConnectionGenerator.get(additionalConfigs, dataRepository));

                if (useInconsistencyBaseline) {
                    pipelineSteps.add(new InconsistencyBaseline(dataRepository));
                } else {
                    pipelineSteps.add(InconsistencyChecker.get(additionalConfigs, dataRepository));
                }

                return pipelineSteps;
            }
        }.runWithoutSaving();
    }
}
