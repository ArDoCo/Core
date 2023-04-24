package edu.kit.kastel.ardoco.lissa.swa.documentation.recognition

import edu.kit.kastel.ardoco.lissa.swa.documentation.recognition.model.SketchRecognitionResult
import edu.kit.kastel.ardoco.lissa.swa.documentation.recognition.services.OCRService
import edu.kit.kastel.ardoco.lissa.swa.documentation.recognition.services.ObjectDetectionService
import edu.kit.kastel.mcse.ardoco.core.api.data.diagramrecognition.Box
import edu.kit.kastel.mcse.ardoco.core.api.data.diagramrecognition.TextBox
import edu.kit.kastel.mcse.ardoco.docker.DockerManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.stream.IntStream.range
import javax.imageio.ImageIO

class SketchRecognitionService {
    companion object {
        const val MINIMUM_AREA_IN_PXPX = 20
        const val EXPANSION_IN_PX = 5
        val logger: Logger = LoggerFactory.getLogger(SketchRecognitionService::class.java)
    }

    private val docker = DockerManager("SketchRecognitionService")
    private val objectDetectionService: ObjectDetectionService = ObjectDetectionService(docker)
    private val ocrService: OCRService = OCRService(docker)

    fun start() {
        objectDetectionService.start()
        ocrService.start()
    }

    fun stop() {
        objectDetectionService.stop()
        ocrService.stop()
        this.docker.shutdownAll()
    }

    fun recognize(input: InputStream): SketchRecognitionResult {
        val byteData = input.readBytes()
        val boxes = objectDetectionService.recognize(byteData)
        val textsWithHints = ocrService.recognize(byteData, boxes)
        val textsWithoutHints = ocrService.recognize(byteData, listOf())

        val texts = mergeTexts(textsWithHints, textsWithoutHints)
        val nodes = boxes.filter { it.classification != "Label" }
        // TODO Merge texts
        combineBoxesAndText(nodes, texts)
        calculateDominatingColors(byteData, nodes)
        // TODO Extract Edges
        return SketchRecognitionResult(nodes, texts, listOf())
    }

    private fun mergeTexts(textsWithHints: List<TextBox>, textsWithoutHints: List<TextBox>): List<TextBox> {
        logger.debug("Merging ${textsWithHints.size} TextsWithHint and ${textsWithoutHints.size} TextsWithoutHint")
        // TODO Impl

        return textsWithHints
    }

    private fun calculateDominatingColors(imageData: ByteArray, boxes: List<Box>) {
        val image = ImageIO.read(ByteArrayInputStream(imageData))
        boxes.forEach { calculateDominatingColorForBox(image, it) }
    }

    private fun calculateDominatingColorForBox(image: BufferedImage, box: Box) {
        val pixels = getPixels(image, box.box.toTypedArray())

        val count = pixels.size
        if (count == 0) return

        val pixelCount = pixels.groupingBy { it }.eachCount().toList().sortedByDescending { it.second }
        val mostPixel = pixelCount[0]
        if (mostPixel.second <= count / 2) return

        box.dominatingColor = mostPixel.first
        setColorsOfTexts(image, box)
    }

    private fun getPixels(image: BufferedImage, box: Array<Int>): List<Int> {
        val result = mutableListOf<Int>()
        for (x in range(box[0], box[2])) for (y in range(box[1], box[3])) result.add(
            image.getRGB(x, y),
        )
        return result
    }

    private fun setColorsOfTexts(image: BufferedImage, box: Box) {
        for (text in box.texts) {
            val pixels = getPixels(image, text.absoluteBox().toTypedArray())
            val count = pixels.size
            if (count == 0) continue
            val pixelCount = pixels.groupingBy { it }.eachCount().toList().sortedByDescending { it.second }
            val textColor = pixelCount.find { (rgba, _) -> rgba != box.dominatingColor }
            if (textColor != null) text.dominatingColor = textColor.first
        }
    }

    private fun combineBoxesAndText(boxes: List<Box>, texts: List<TextBox>) {
        for (text in texts) {
            if (text.text.length < 3) continue

            val intersects = boxes.map { it to it.box.boundingBox().iou(text.absoluteBox().boundingBox()) }

            val results = intersects.filter { it.second.areaIntersect / text.area() > 0.9 }
            if (results.isEmpty()) continue
            logger.info("Found {} intersects with {}", intersects.size, text.text)
            results.forEach { it.first.addTextBox(text) }
        }
    }
}
