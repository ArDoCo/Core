package edu.kit.kastel.mcse.ardoco.tests.integration

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.core.execution.runner.AnonymousRunner
import edu.kit.kastel.mcse.ardoco.core.models.agents.ArCoTLModelProviderAgent
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.DiagramRecognitionMock
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject
import edu.kit.kastel.mcse.ardoco.tests.eval.GoldStandardDiagrams
import edu.kit.kastel.mcse.ardoco.tests.eval.StageTest
import org.eclipse.collections.impl.factory.SortedMaps
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.SortedMap

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class DiagramRecognitionMockTest : StageTest<DiagramRecognitionMock, GoldStandardDiagrams, DiagramRecognitionMockTest.DiagramRecognitionResult>(
    DiagramRecognitionMock(
        null,
        SortedMaps.mutable.empty(),
        DataRepository()
    ),
    DiagramProject.entries.toTypedArray()
) {
    @DisplayName("Evaluate Diagram Recognition")
    @ParameterizedTest(name = "{0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getNonHistoricalProjects")
    @Order(1)
    @Disabled
    fun evaluateNonHistoricalDiagramRecognition(project: DiagramProject?) {
        run(project)
    }

    @DisplayName("Evaluate Diagram Recognition (Historical)")
    @ParameterizedTest(name = "{0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getHistoricalProjects")
    @Order(2)
    @Disabled
    fun evaluateHistoricalDiagramRecognition(project: DiagramProject?) {
        run(project)
    }

    protected override fun runComparable(
        project: GoldStandardDiagrams,
        additionalConfigurations: SortedMap<String, String>,
        cachePreRun: Boolean
    ): DiagramRecognitionResult {
        val result = run(project, additionalConfigurations, cachePreRun)
        val diagramRecognition = result.getData(DiagramRecognitionState.ID, DiagramRecognitionState::class.java).orElseThrow()
        val diagrams = diagramRecognition.getDiagrams()
        return DiagramRecognitionResult(diagrams)
    }

    protected override fun runPreTestRunner(project: GoldStandardDiagrams): DataRepository {
        return object : AnonymousRunner(project.getProjectName()) {
            override fun initializePipelineSteps(dataRepository: DataRepository): List<AbstractPipelineStep> {
                dataRepository.globalConfiguration.wordSimUtils.considerAbbreviations = true
                val pipelineSteps = ArrayList<AbstractPipelineStep>()
                val arCoTLModelProviderAgent =
                    ArCoTLModelProviderAgent.get(
                        project.modelFile,
                        ArchitectureModelType.PCM,
                        null,
                        project.additionalConfigurations,
                        dataRepository
                    )
                pipelineSteps.add(arCoTLModelProviderAgent)
                return pipelineSteps
            }
        }.runWithoutSaving()
    }

    protected override fun runTestRunner(
        project: GoldStandardDiagrams,
        additionalConfigurations: SortedMap<String, String>,
        preRunDataRepository: DataRepository
    ): DataRepository {
        return object : AnonymousRunner(project.getProjectName(), preRunDataRepository) {
            override fun initializePipelineSteps(dataRepository: DataRepository): List<AbstractPipelineStep> {
                dataRepository.globalConfiguration.wordSimUtils.considerAbbreviations = true
                val pipelineSteps = ArrayList<AbstractPipelineStep>()
                pipelineSteps.add(DiagramRecognitionMock(project, project.additionalConfigurations, dataRepository))
                return pipelineSteps
            }
        }.runWithoutSaving()
    }

    @JvmRecord
    data class DiagramRecognitionResult(val diagrams: List<Diagram>)
}
