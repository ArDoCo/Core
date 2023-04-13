package edu.kit.kastel.ardoco.lissa.swa.documentation

import edu.kit.kastel.ardoco.lissa.swa.documentation.recognition.SketchRecognitionService
import edu.kit.kastel.ardoco.lissa.swa.documentation.recognition.visualize
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.awt.Color
import java.awt.Desktop
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SketchRecognitionServiceTest {
    companion object {
        const val PATH = "src/test/resources/bbb-arch-overview.png"
        const val PATH_TO_HL_ARCHITECTURE = "src/test/resources/highlevelArchitecture.png"
    }

    private lateinit var service: SketchRecognitionService

    @BeforeAll
    fun setup() {
        service = SketchRecognitionService()
        service.start()
        File("target/testout").mkdirs()
    }

    @AfterAll
    fun tearDown() {
        service.stop()
    }

    @Test
    fun testSimpleRecognitionWithColors() {
        val file = FileInputStream(File(PATH_TO_HL_ARCHITECTURE))
        val response = service.recognize(file)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(8, response.boxes.size)
        Assertions.assertEquals(35, response.textBoxes.size)

        val testDriver = response.boxes.filter { it.texts.any { tb -> tb.text.lowercase().contains("driver") } }
        Assertions.assertEquals(1, testDriver.size)
        val testColor = testDriver[0].dominatingColor
        Assertions.assertEquals(Color(217, 150, 148).rgb, testColor)

        val logic = response.boxes.filter { it.texts.any { tb -> tb.text.lowercase().contains("logic") } }
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
            response,
            FileOutputStream(destination),
        )
        if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(destination)
    }

    @Test
    fun simpleRecognition() {
        val file = FileInputStream(File(PATH))
        val response = service.recognize(file)
        Assertions.assertNotNull(response)

        val destination = File("target/testout/result_testSimpleRecognition.png")
        visualize(
            FileInputStream(File(PATH)),
            response,
            FileOutputStream(destination),
        )
        if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(destination)
    }
}
