package edu.kit.kastel.mcse.ardoco.tests.integration

import edu.kit.kastel.mcse.ardoco.core.api.InputDiagramData
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramGS
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType
import edu.kit.kastel.mcse.ardoco.core.common.util.Comparators
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.core.execution.runner.AnonymousRunner
import edu.kit.kastel.mcse.ardoco.core.models.agents.ArCoTLModelProviderAgent
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep
import edu.kit.kastel.mcse.ardoco.lissa.DiagramRecognition
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject
import edu.kit.kastel.mcse.ardoco.tests.eval.GoldStandardDiagrams
import edu.kit.kastel.mcse.ardoco.tests.eval.StageTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Order
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.SortedMap

internal class DiagramRecognitionTest :
    StageTest<DiagramRecognition, GoldStandardDiagrams, DiagramRecognitionTest.DiagramRecognitionResult>(
        DiagramRecognition(DataRepository()),
        DiagramProject.entries.toTypedArray()
    ) {
    @DisplayName("Evaluate Diagram Recognition")
    @ParameterizedTest(name = "{0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getNonHistoricalProjects")
    @Order(1)
    fun evaluateNonHistoricalDiagramRecognition(project: DiagramProject) {
        val result = runComparable(project)
        Assertions.assertTrue(
            Comparators.collectionsEqualsAnyOrder(
                result!!.diagrams.map { obj: Diagram -> obj.getShortResourceName() }.toList(),
                project.diagramsGoldStandard
                    .stream()
                    .map { obj: DiagramGS -> obj.getShortResourceName() }
                    .toList()
            )
        )
    }

    @DisplayName("Evaluate Diagram Recognition (Historical)")
    @ParameterizedTest(name = "{0}")
    @MethodSource("edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject#getHistoricalProjects")
    @Order(2)
    fun evaluateHistoricalDiagramRecognition(project: DiagramProject) {
        val result = runComparable(project)
        Assertions.assertTrue(
            Comparators.collectionsEqualsAnyOrder(
                result!!.diagrams.stream().map { obj: Diagram -> obj.getShortResourceName() }.toList(),
                project.diagramsGoldStandard
                    .stream()
                    .map { obj: DiagramGS -> obj.getShortResourceName() }
                    .toList()
            )
        )
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
                dataRepository.addData(InputDiagramData.ID, InputDiagramData(project.getDiagramData()))
                pipelineSteps.add(DiagramRecognition(dataRepository))
                return pipelineSteps
            }
        }.runWithoutSaving()
    }

    @JvmRecord
    data class DiagramRecognitionResult(val diagrams: List<Diagram>)
}
