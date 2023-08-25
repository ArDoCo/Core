package edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.services

import edu.kit.kastel.mcse.ardoco.core.api.InputDiagramData
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Classification
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.lissa.DiagramRecognition
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.visualize
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.Desktop
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisabledIfEnvironmentVariable(named = "NO_DOCKER", matches = "true")
class SketchRecognitionServiceTest {
    companion object {
        const val PATH = "src/test/resources/bbb-arch-overview.png"
        const val PATH_TO_HL_ARCHITECTURE = "src/test/resources/highlevelArchitecture.png"

        private val logger: Logger = LoggerFactory.getLogger(SketchRecognitionServiceTest::class.java)
    }

    private lateinit var dataRepository: DataRepository

    @BeforeAll
    fun setup() {
        assumeDocker()
        dataRepository = DataRepository()
        dataRepository.addData(InputDiagramData.ID, InputDiagramData("src/test/resources/"))
        val stage = DiagramRecognition(dataRepository)
        stage.run()
        assertNotNull(getState())
        File("target/testout").mkdirs()
    }

    private fun assumeDocker() {
        val remoteDocker = System.getenv("REMOTE_DOCKER_IP") != null && System.getenv("REMOTE_DOCKER_PORT") != null
        var localDocker = true
        try {
            val result = Runtime.getRuntime().exec("docker ps")
            result.waitFor(3, TimeUnit.SECONDS)
            localDocker = result.exitValue() == 0
        } catch (e: Exception) {
            localDocker = false
            logger.error(e.message, e)
        }
        Assumptions.assumeTrue(remoteDocker || localDocker, "Docker is not available")
    }

    private fun getState() = dataRepository.getData(DiagramRecognitionState.ID, DiagramRecognitionState::class.java).orElse(null)

    @Test
    fun testSimpleRecognitionWithColors() {
        val file = File(PATH_TO_HL_ARCHITECTURE)
        val diagram = getState().diagrams.find { it.location == file }!!
        Assertions.assertEquals(8, diagram.boxes.filter { it.classification != Classification.LABEL }.size)
        Assertions.assertEquals(35, diagram.textBoxes.size)

        val testDriver = diagram.boxes.filter { it.texts.any { tb -> tb.text.lowercase().contains("driver") } }
        Assertions.assertEquals(1, testDriver.size)
        val testColor = testDriver[0].dominatingColor
        Assertions.assertEquals(Color(217, 150, 148).rgb, testColor)

        val logic = diagram.boxes.filter { it.texts.any { tb -> tb.text.lowercase().contains("logic") } }
        Assertions.assertEquals(1, logic.size)
        val logicColor = logic[0].dominatingColor
        Assertions.assertEquals(Color(179, 162, 199).rgb, logicColor)

        val texts = logic[0].texts
        Assertions.assertEquals(2, texts.size)
        val javaText = texts.find { it.text.lowercase().contains("java") }!!
        val logicText = texts.find { it.text.lowercase().contains("logic") }!!
        Assertions.assertEquals(Color(255, 255, 255).rgb, javaText.dominatingColor)
        Assertions.assertEquals(Color(96, 74, 123).rgb, logicText.dominatingColor)

        val destination = File("target/testout/result_testSimpleRecognitionWithColors.png")
        visualize(
            FileInputStream(File(PATH_TO_HL_ARCHITECTURE)),
            diagram,
            FileOutputStream(destination)
        )
        if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(destination)
    }

    @Test
    fun simpleRecognition() {
        val file = File(PATH_TO_HL_ARCHITECTURE)
        val diagram = getState().diagrams.find { it.location == file }!!

        val destination = File("target/testout/result_testSimpleRecognition.png")
        visualize(
            FileInputStream(file),
            diagram,
            FileOutputStream(destination)
        )
        if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(destination)
    }
}
