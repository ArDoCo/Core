package edu.kit.kastel.ardoco.lissa.diagramrecognition.services

import edu.kit.kastel.ardoco.lissa.DiagramRecognition
import edu.kit.kastel.ardoco.lissa.diagramrecognition.visualize
import edu.kit.kastel.mcse.ardoco.core.api.data.InputDiagramData
import edu.kit.kastel.mcse.ardoco.core.api.data.diagramrecognition.DiagramRecognitionState
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.awt.Color
import java.awt.Desktop
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SketchRecognitionServiceTest {
    companion object {
        const val PATH = "src/test/resources/bbb-arch-overview.png"
        const val PATH_TO_HL_ARCHITECTURE = "src/test/resources/highlevelArchitecture.png"
    }

    private lateinit var dataRepository: DataRepository

    @BeforeAll
    fun setup() {
        dataRepository = DataRepository()
        dataRepository.addData(InputDiagramData.ID, InputDiagramData("src/test/resources/"))
        val stage = DiagramRecognition(dataRepository)
        stage.run()
        assertNotNull(getState())
        File("target/testout").mkdirs()
    }

    private fun getState() = dataRepository.getData(DiagramRecognitionState.ID, DiagramRecognitionState::class.java).orElse(null)

    @Test
    fun testSimpleRecognitionWithColors() {
        val file = File(PATH_TO_HL_ARCHITECTURE)
        val diagram = getState().diagrams.find { it.location == file }!!
        Assertions.assertEquals(8, diagram.boxes.size)
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
            FileOutputStream(destination),
        )
        if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(destination)
    }

    @Test
    fun simpleRecognition() {
        val file = File(PATH)
        val diagram = getState().diagrams.find { it.location == file }!!

        val destination = File("target/testout/result_testSimpleRecognition.png")
        visualize(
            FileInputStream(File(PATH)),
            diagram,
            FileOutputStream(destination),
        )
        if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(destination)
    }
}
