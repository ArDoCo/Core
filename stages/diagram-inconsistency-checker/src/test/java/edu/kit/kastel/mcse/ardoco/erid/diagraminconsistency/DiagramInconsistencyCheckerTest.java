/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.collections.impl.factory.SortedMaps;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.InputDiagramData;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.AnonymousRunner;
import edu.kit.kastel.mcse.ardoco.core.models.agents.ArCoTLModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.text.providers.TextPreprocessingAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;
import edu.kit.kastel.mcse.ardoco.erid.api.diagraminconsistency.DiagramInconsistencyStates;
import edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.DiagramConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.types.MDEInconsistency;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.types.MTDEInconsistency;
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.DiagramRecognitionMock;
import edu.kit.kastel.mcse.ardoco.lissa.DiagramRecognition;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;
import edu.kit.kastel.mcse.ardoco.tests.eval.StageTest;

class DiagramInconsistencyCheckerTest extends StageTest<DiagramInconsistencyChecker, DiagramProject, DiagramInconsistencyCheckerTest.Results> {
    private static final Logger logger = LoggerFactory.getLogger(DiagramInconsistencyCheckerTest.class);
    private static final boolean useMockDiagrams = true;

    public record Results(SortedSet<MDEInconsistency> mdeInconsistencies, SortedSet<MTDEInconsistency> mtdeInconsistencies) {
        @Override
        public String toString() {
            return String.format("MissingDiagramElements: %d, MissingTextForDiagramElements: %d", mdeInconsistencies().size(), mtdeInconsistencies().size());
        }
    }

    public DiagramInconsistencyCheckerTest() {
        super(new DiagramInconsistencyChecker(SortedMaps.mutable.empty(), new DataRepository()), DiagramProject.values());
    }

    @Override
    protected Results runComparable(DiagramProject project, SortedMap<String, String> additionalConfigurations, boolean cachePreRun) {
        var dataRepository = run(project, additionalConfigurations, cachePreRun);
        var diagramInconsistencyStates = dataRepository.getData(DiagramInconsistencyStates.ID, DiagramInconsistencyStates.class).orElseThrow();
        var diagramInconsistencyState = diagramInconsistencyStates.getDiagramInconsistencyState(project.getMetamodel());

        var mdeInconsistencies = new TreeSet<>(diagramInconsistencyState.getInconsistencies(MDEInconsistency.class));
        var mtdeInconsistencies = new TreeSet<>(diagramInconsistencyState.getInconsistencies(MTDEInconsistency.class));

        var result = new DiagramInconsistencyCheckerTest.Results(mdeInconsistencies, mtdeInconsistencies);

        logger.info(result.toString());

        return result;
    }

    @Override
    protected DataRepository runPreTestRunner(DiagramProject project) {
        logger.info("Run PreTestRunner for {}", project.name());
        return new AnonymousRunner(project.name()) {
            @Override
            public List<AbstractPipelineStep> initializePipelineSteps(DataRepository dataRepository) throws IOException {
                dataRepository.getGlobalConfiguration().getWordSimUtils().setConsiderAbbreviations(true);
                var pipelineSteps = new ArrayList<AbstractPipelineStep>();

                var text = CommonUtilities.readInputText(project.getTextFile());
                if (text.isBlank()) {
                    throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
                }
                DataRepositoryHelper.putInputText(dataRepository, text);
                pipelineSteps.add(TextPreprocessingAgent.get(project.getAdditionalConfigurations(), dataRepository));

                ArCoTLModelProviderAgent arCoTLModelProviderAgent = ArCoTLModelProviderAgent.get(project.getModelFile(), project.getArchitectureModelType(),
                        null, project.getAdditionalConfigurations(), dataRepository);
                pipelineSteps.add(arCoTLModelProviderAgent);

                if (useMockDiagrams) {
                    pipelineSteps.add(new DiagramRecognitionMock(project, project.getAdditionalConfigurations(), dataRepository));
                } else {
                    dataRepository.addData(InputDiagramData.ID, new InputDiagramData(project.getDiagramData()));
                    pipelineSteps.add(DiagramRecognition.get(project.getAdditionalConfigurations(), dataRepository));
                }

                pipelineSteps.add(TextExtraction.get(project.getAdditionalConfigurations(), dataRepository));
                pipelineSteps.add(RecommendationGenerator.get(project.getAdditionalConfigurations(), dataRepository));
                pipelineSteps.add(new DiagramConnectionGenerator(project.getAdditionalConfigurations(), dataRepository));

                return pipelineSteps;
            }
        }.runWithoutSaving();
    }

    @Override
    protected DataRepository runTestRunner(DiagramProject project, SortedMap<String, String> additionalConfigurations, DataRepository preRunDataRepository) {
        logger.info("Run TestRunner for {}", project.name());
        var combinedConfigs = new TreeMap<>(project.getAdditionalConfigurations());
        combinedConfigs.putAll(additionalConfigurations);
        return new AnonymousRunner(project.name(), preRunDataRepository) {
            @Override
            public List<AbstractPipelineStep> initializePipelineSteps(DataRepository dataRepository) {
                dataRepository.getGlobalConfiguration().getWordSimUtils().setConsiderAbbreviations(true);
                var pipelineSteps = new ArrayList<AbstractPipelineStep>();
                pipelineSteps.add(new DiagramInconsistencyChecker(combinedConfigs, dataRepository));
                return pipelineSteps;
            }
        }.runWithoutSaving();
    }

    @Disabled
    @Test
    void teammatesTest() {
        runComparable(DiagramProject.TEAMMATES);
    }

    @Disabled
    @Test
    void teammatesHistTest() {
        runComparable(DiagramProject.TEAMMATES_HISTORICAL);
    }

    @Disabled
    @Test
    void teastoreTest() {
        runComparable(DiagramProject.TEASTORE);
    }

    @Disabled
    @Test
    void teastoreHistTest() {
        runComparable(DiagramProject.TEASTORE_HISTORICAL);
    }

    @Disabled
    @Test
    void bbbTest() {
        runComparable(DiagramProject.BIGBLUEBUTTON);
    }

    @Disabled
    @Test
    void bbbHistTest() {
        runComparable(DiagramProject.BIGBLUEBUTTON_HISTORICAL);
    }

    @Disabled
    @Test
    void msTest() {
        runComparable(DiagramProject.MEDIASTORE);
    }
}
