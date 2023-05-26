package edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.informants

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Classification
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.TextBox
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.boundingBox
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.util.stream.IntStream
import javax.imageio.ImageIO

class RecognitionCombinatorInformant(dataRepository: DataRepository) : Informant(ID, dataRepository) {
    companion object {
        const val ID = "RecognitionCombinatorInformant"
    }

    override fun delegateApplyConfigurationToInternalObjects(additionalConfiguration: MutableMap<String, String>?) {
        // Not needed
    }

    override fun run() {
        val diagramRecognitionState = DataRepositoryHelper.getDiagramRecognitionState(dataRepository)
        for (diagram in diagramRecognitionState.diagrams) {
            val entities = diagram.boxes.filter { it.classification != Classification.LABEL }
            val texts = diagram.textBoxes
            combineBoxesAndText(entities, texts)
            calculateDominatingColors(diagram.location.readBytes(), entities)
        }
    }

    private fun combineBoxesAndText(entities: List<Box>, texts: List<TextBox>) {
        for (text in texts) {
            if (text.text.length < 3) continue

            val intersects = entities.map { it to it.box.boundingBox().iou(text.absoluteBox().boundingBox()) }

            val results = intersects.filter { it.second.areaIntersect / text.area() > 0.9 }
            if (results.isEmpty()) continue
            logger.info("Found {} intersects with {}", intersects.size, text.text)
            results.forEach { it.first.addTextBox(text) }
        }
    }

    private fun calculateDominatingColors(imageData: ByteArray, boxes: List<Box>) {
        val image = ByteArrayInputStream(imageData).use { ImageIO.read(it) }
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
        for (x in IntStream.range(box[0], box[2])) for (y in IntStream.range(box[1], box[3])) result.add(
            image.getRGB(x, y)
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
}
